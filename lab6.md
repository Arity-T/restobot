# Lab 6: Полный pipeline Terraform + Ansible через Jenkins

## Цель работы

Для этой лабораторной работы в проекте собран полный Jenkins pipeline, который:

- получает исходный код из репозитория;
- подготавливает секреты и переменные окружения;
- запускает Terraform для создания или удаления инфраструктуры в Yandex Cloud;
- ждёт готовности SSH на созданной VM;
- запускает Ansible для настройки сервера и деплоя приложения;
- поднимает PostgreSQL, автоматически накатывает Flyway-миграции и запускает приложение в Docker Compose.

Pipeline поддерживает два режима:

- `TF_ACTION=apply` - создать или обновить инфраструктуру и выполнить деплой;
- `TF_ACTION=destroy` - удалить инфраструктуру Terraform.

## Что есть в репозитории

- `Jenkinsfile` - основной Jenkins pipeline.
- `terraform/` - инфраструктура в Yandex Cloud.
- `ansible/playbook.yml` - настройка VM и деплой Docker Compose.
- `ansible/ansible.cfg` - Ansible-конфиг для Jenkins.
- `docker-compose.yml` - PostgreSQL, Flyway и приложение.
- `terraform.tfrc` - mirror-конфиг Terraform для провайдеров.
- `lab5.md` - документация по Terraform, Ansible и Yandex Cloud.

## Как работает pipeline

При `TF_ACTION=apply` Jenkins выполняет следующий сценарий:

1. делает `checkout scm`;
2. получает `.env`, `tfvars`, IAM token и SSH-ключ из Jenkins Credentials;
3. генерирует публичный SSH-ключ из приватного ключа Jenkins;
4. выполняет `terraform init`;
5. выполняет `terraform fmt -check` и `terraform validate`;
6. выполняет `terraform plan`;
7. выполняет `terraform apply`;
8. ждёт, пока VM начнёт принимать SSH-соединения;
9. запускает `ansible-playbook`;
10. Ansible ставит Docker, копирует `docker-compose.yml`, `.env` и SQL-миграции;
11. Docker Compose поднимает `postgres`, затем `migrate`, затем `app`;
12. Jenkins выводит `terraform output`.

При `TF_ACTION=destroy` Jenkins:

1. получает те же credentials;
2. запускает `terraform destroy`;
3. удаляет ресурсы, которые созданы Terraform.

## Что важно в Compose-деплое

В `docker-compose.yml` сейчас три сервиса:

- `postgres` - база данных;
- `migrate` - контейнер `flyway/flyway`, который накатывает SQL-миграции;
- `app` - контейнер приложения.

`app` зависит от:

- готовности `postgres`;
- успешного завершения `migrate`.

Это исправляет ситуацию, когда приложение стартует на пустой базе и падает с ошибкой вида `relation "public.city" does not exist`.

## Что должно быть на Jenkins agent

На Linux-агенте Jenkins должны быть установлены:

- Git;
- Terraform `>= 1.6.3`;
- Ansible;
- OpenSSH client;
- `ssh-keygen`;
- `perl`;
- `bash`;
- JDK 23, если используется `RUN_BUILD=true`;
- Docker и Docker Compose, если используется `RUN_BUILD=true`.

Если `RUN_BUILD=false`, Docker на Jenkins agent не нужен. Если `RUN_BUILD=true`, pipeline поднимает локальный контейнер PostgreSQL на Jenkins-машине перед `./gradlew build`.

## Какие Jenkins plugins нужны

Минимально нужны:

- `Pipeline`;
- `Git`;
- `Credentials Binding`;
- `SSH Credentials`.

Плагин `AnsiColor` полезен, но не обязателен. В текущем `Jenkinsfile` он больше не требуется, чтобы локальный Jenkins запускался без лишней настройки.

## Запуск Jenkins локально

### 1. Убедитесь, что есть Java 21+:

```bash
java -version
```

### 2. Скачайте Jenkins LTS и задайте отдельный каталог для данных:

```bash
mkdir -p "$HOME/jenkins-local"
cd "$HOME/jenkins-local"

curl -L -o jenkins.war https://get.jenkins.io/war-stable/latest/jenkins.war

```

### 3. Запустите Jenkins:

```bash
JENKINS_HOME="$HOME/.jenkins-local" java -jar jenkins.war --httpPort=9081 --enable-future-java
```

### 4. Откройте:

```bash
http://localhost:9081
```


## Как запустить Jenkins локально

Для этой лабораторной локальный Jenkins проще всего поднимать не в Docker, а нативно на той же машине, где уже установлены:

- Git;
- Terraform;
- Ansible;
- OpenSSH client;
- JDK 23, если нужен `RUN_BUILD=true`.

Причина простая: текущий pipeline запускает локальные бинарники `terraform`, `ansible-playbook`, `ssh` и `ssh-keygen`. На одной машине это настраивается заметно проще, чем в контейнеризированном Jenkins.

### Практический локальный сценарий

1. Установить Jenkins локально на своей машине.
2. Открыть Jenkins в браузере по адресу:

```text
http://localhost:8080
```

3. Пройти initial setup:

- вставить initial admin password;
- выбрать `Install suggested plugins`;
- создать первого администратора.

4. Установить на той же машине инструменты для pipeline:

- Git;
- Terraform `>= 1.6.3`;
- Ansible;
- OpenSSH client;
- `ssh-keygen`;
- `perl`;
- `bash`;
- JDK 23, если нужен `RUN_BUILD=true`;
- Docker и Docker Compose, если нужен `RUN_BUILD=true`.

5. Убедиться, что команды доступны из shell того пользователя, под которым работает Jenkins:

```bash
git --version
terraform version
ansible-playbook --version
ssh -V
java -version
```

### Как подключить локальный репозиторий

Есть два рабочих варианта.

Вариант A: использовать удалённый Git-репозиторий.

Это самый простой и надёжный способ:

- Jenkins будет делать обычный `checkout scm`;
- не будет путаницы с локальными путями;
- проще повторять запуск.

Вариант B: использовать локальный Git-репозиторий на той же машине.

Для `Pipeline script from SCM` можно указать локальный Git-репозиторий как источник SCM, например через локальный путь или `file:///...`.

Важно:

- Jenkins будет видеть состояние Git-репозитория, а не произвольные несохранённые файлы в редакторе;
- если вы хотите, чтобы job точно использовала последние изменения, лучше предварительно сделать commit.

### Как создать локальную Pipeline job

Порядок:

1. создать новую job типа `Pipeline`;
2. выбрать `Pipeline script from SCM`;
3. выбрать `Git`;
4. указать либо URL удалённого репозитория, либо локальный путь к этому репозиторию;
5. указать `Script Path`:

```text
Jenkinsfile
```

6. сохранить job.

### Какие credentials нужны локально

Даже при локальном Jenkins credentials всё равно настраиваются внутри Jenkins UI.

Нужно создать:

- `restobot_env`
- `restobot_tfvars`
- `restobot_vm_ssh`
- `yc_iam_token`

Их формат полностью совпадает с обычным Jenkins-сценарием:

- `.env` и `tfvars` как `Secret file`;
- IAM token как `Secret text`;
- SSH-ключ как `SSH Username with private key`.

### Как запускать локально

После настройки built-in node, credentials и job запуск такой же, как на отдельном Jenkins-сервере:

1. открыть job;
2. выбрать `TF_ACTION=apply`;
3. при необходимости включить `RUN_BUILD=true`;
4. нажать `Build`.

Для удаления инфраструктуры:

1. открыть job;
2. выбрать `TF_ACTION=destroy`;
3. запустить build.

### Что важно именно для локального Jenkins

- Если Jenkins запущен локально на macOS, built-in node должен иметь доступ к установленным `terraform`, `ansible-playbook` и `ssh`.
- Если `RUN_BUILD=true`, у Jenkins должен быть доступ к Java 23 через `PATH`.
- Если вы используете локальный Git-репозиторий как SCM-источник, лучше не полагаться на незакоммиченные изменения.
- Если IAM token истёк, локальный Jenkins тоже не сможет выполнить Terraform.
- Docker на локальной машине Jenkins не обязателен для самого pipeline, потому что Docker ставится Ansible на удалённую VM.

### Когда Docker-версия Jenkins неудобна

Технически Jenkins можно поднять и в Docker, но для этой лабораторной это менее удобно, потому что тогда придётся дополнительно решать:

- установку `terraform`, `ansible`, `ssh` и `perl` внутрь Jenkins-контейнера или внутрь отдельного agent-контейнера;
- доступ контейнера к SSH-ключам;
- доступ контейнера к локальному Git-репозиторию;
- сетевой доступ к Yandex Cloud и к VM.

Поэтому для локального стенда проще использовать нативный Jenkins на одной машине.

## Подготовка Yandex Cloud

Для pipeline нужны:

- `cloud_id`;
- `folder_id`;
- IAM token пользовательской учётной записи;
- SSH-ключ для доступа на VM;
- права на создание VPC, subnet, security group и VM в выбранном folder.

Если service account не используется, Jenkins работает через переменную `YC_TOKEN`, которая берётся из Jenkins credential `yc_iam_token`.

Важно:

- IAM token имеет ограниченный срок жизни;
- если token истёк, Jenkins не сможет выполнить `plan/apply/destroy`;
- credential `yc_iam_token` нужно периодически обновлять вручную.

## Какие credentials нужны в Jenkins

Ниже ожидаемые credentials IDs:

- `restobot_env` - `Secret file` с содержимым `.env`;
- `restobot_tfvars` - `Secret file` с содержимым Terraform variables;
- `restobot_vm_ssh` - `SSH Username with private key`;
- `yc_iam_token` - `Secret text` с IAM token.

### 1. `restobot_env`

Это файл `.env`, который потом будет скопирован:

- в workspace Jenkins;
- затем на VM в `/opt/restobot/.env`.

Минимально в нём должны быть:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `MAIN_DB_USER`
- `MAIN_DB_PASSWORD`
- `TRIPADVISOR_API_KEY`
- `TRIPADVISOR_API_HOST`
- `TRIPADVISOR_API_LANGUAGE`
- `API_SERVER_HOST`
- `API_SERVER_PORT`

Рекомендуемое значение:

```env
API_SERVER_HOST=0.0.0.0
API_SERVER_PORT=8089
```

### 2. `restobot_tfvars`

Это файл с содержимым наподобие `terraform.auto.tfvars`.

Минимальный пример:

```hcl
cloud_id  = "b1gxxxxxxxxxxxxxxx"
folder_id = "b1gxxxxxxxxxxxxxxx"

zone         = "ru-central1-a"
project_name = "restobot"

instance_count = 1
subnet_cidr    = "10.10.10.0/24"
allowed_cidrs  = ["0.0.0.0/0"]

platform_id       = "standard-v3"
vm_cores          = 2
vm_memory_gb      = 2
vm_core_fraction  = 100
boot_disk_size_gb = 20
boot_disk_type    = "network-hdd"
preemptible       = true

image_family = "ubuntu-2204-lts"
app_port     = 8089
```

Если нужно использовать существующую сеть или подсеть, можно добавить:

```hcl
existing_network_id = "enpxxxxxxxxxxxxxxx"
# existing_subnet_id  = "e9bxxxxxxxxxxxxxxx"
```

или:

```hcl
existing_subnet_id = "e9bxxxxxxxxxxxxxxx"
```

### 3. `restobot_vm_ssh`

Это credential типа `SSH Username with private key`.

Оно должно содержать:

- username, например `restobot`;
- приватный ключ.

Jenkins сам генерирует из него публичный ключ и передаёт его в Terraform через:

- `TF_VAR_ssh_public_key_path`
- `TF_VAR_ssh_user`

### 4. `yc_iam_token`

Это credential типа `Secret text`.

Получить token можно так:

```bash
yc init
yc iam create-token
```

Скопируйте полученное значение в Jenkins credential `yc_iam_token`.

## Как создать job в Jenkins

Самый удобный вариант - `Pipeline` job из SCM.

Порядок:

1. создать новую job типа `Pipeline`;
2. выбрать `Pipeline script from SCM`;
3. указать ваш Git-репозиторий;
4. выбрать нужную ветку;
5. в поле `Script Path` указать:

```text
Jenkinsfile
```

После этого Jenkins будет использовать pipeline прямо из репозитория.

## Параметры Jenkins job

В pipeline есть два параметра:

- `TF_ACTION`
- `RUN_BUILD`

### `TF_ACTION`

Возможные значения:

- `apply` - развернуть инфраструктуру и приложение;
- `destroy` - удалить инфраструктуру.

### `RUN_BUILD`

Если `true`, Jenkins перед инфраструктурой выполнит:

```bash
./gradlew build
```

Это полезно, если вы хотите дополнительно проверять сборку проекта перед деплоем.

Если `false`, pipeline будет работать только как infrastructure/deploy job.

## Что делает Jenkinsfile по стадиям

### Checkout

```text
Checkout
```

Jenkins получает исходный код.

### Prepare Credentials

```text
Prepare Credentials
```

На этой стадии Jenkins:

- копирует `.env` из credential `restobot_env` в workspace;
- копирует `tfvars` из credential `restobot_tfvars` в `terraform/jenkins.auto.tfvars`;
- генерирует публичный SSH-ключ из приватного ключа Jenkins.

### Validate Tooling

```text
Validate Tooling
```

Проверяются версии:

- Terraform;
- Ansible;
- SSH.

### Gradle Prep / Build

Эти стадии выполняются только если `RUN_BUILD=true`.

### Prepare Local Database

```text
Prepare Local Database
```

Эта стадия выполняется только если `RUN_BUILD=true`.

Jenkins:

1. поднимает локальный `postgres` через:

```bash
docker compose up -d postgres
```

2. ждёт, пока контейнер станет `healthy`;
3. запускает:

```bash
./gradlew :logic:flywayMigrate
```

Это нужно потому, что во время `build` задача `:logic:generateJooq` подключается к базе данных из `.env`, и без живой локальной PostgreSQL-схемы сборка падает.

### Terraform Init

```text
Terraform Init
```

Jenkins запускает:

```bash
terraform init
```

Для загрузки провайдеров используется `terraform.tfrc`, который подключён через:

```text
TF_CLI_CONFIG_FILE=${WORKSPACE}/terraform.tfrc
```

Это нужно, чтобы Terraform использовал mirror Yandex Cloud и не упирался в проблемы с `registry.terraform.io`.

### Terraform Validate

```text
Terraform Validate
```

Выполняются:

```bash
terraform fmt -check
terraform validate
```

### Terraform Plan

```text
Terraform Plan
```

На этой стадии Jenkins передаёт:

- `YC_TOKEN`;
- `TF_VAR_ssh_public_key_path`;
- `TF_VAR_ssh_user`.

И выполняет:

```bash
terraform plan -var-file=jenkins.auto.tfvars -out=tfplan
```

### Terraform Apply

```text
Terraform Apply
```

Выполняется:

```bash
terraform apply -auto-approve tfplan
```

### Wait For SSH

```text
Wait For SSH
```

Jenkins ждёт, пока созданная VM начнёт отвечать по SSH.

### Configure Hosts With Ansible

```text
Configure Hosts With Ansible
```

Запускается:

```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/playbook.yml --private-key "$SSH_KEY_FILE" -u "$SSH_USER"
```

Ansible:

- ставит Docker;
- копирует compose;
- копирует `.env`;
- копирует SQL-миграции;
- запускает стек.

### Terraform Output

```text
Terraform Output
```

Jenkins выводит итоговые `terraform output`, включая IP-адреса и использованные `network_id`/`subnet_id`.

### Terraform Destroy

Если выбрано `TF_ACTION=destroy`, выполняется:

```bash
terraform destroy -auto-approve -var-file=jenkins.auto.tfvars
```

## Как запускать pipeline

### Полное развертывание

1. открыть Jenkins job;
2. выбрать `TF_ACTION=apply`;
3. при необходимости включить `RUN_BUILD=true`;
4. нажать `Build`.

После успешного выполнения:

- Terraform создаст инфраструктуру;
- Ansible настроит VM;
- Compose поднимет PostgreSQL, миграции и приложение.

### Удаление инфраструктуры

1. открыть Jenkins job;
2. выбрать `TF_ACTION=destroy`;
3. запустить build.

## Как проверить результат

После `apply` можно:

1. посмотреть `Terraform Output` в Jenkins log;
2. открыть:

```text
http://<public_ip>:8089/healthcheck
```

3. при необходимости зайти на сервер:

```bash
ssh -i ~/.ssh/restobot_vm restobot@<public_ip>
cd /opt/restobot
sudo docker compose ps
sudo docker compose logs migrate --tail=100
sudo docker compose logs app --tail=100
```

## Что делать, если приложение не поднялось

### 1. Истёк `yc_iam_token`

Симптом:

- Terraform не может выполнить `plan`, `apply` или `destroy`.

Что делать:

```bash
yc iam create-token
```

Обновить Jenkins credential `yc_iam_token` и запустить job снова.

### 2. Terraform не может скачать провайдеры

Симптом:

- ошибки, связанные с `registry.terraform.io`.

Что делать:

- убедиться, что Jenkins использует `terraform.tfrc`;
- проверить сетевой доступ Jenkins agent;
- убедиться, что `terraform init` запускается из pipeline, а не вручную без mirror-конфига.

### 3. Исчерпана квота на VPC-сети

Симптом:

- ошибка `Quota limit vpc.networks.count exceeded`.

Что делать:

- удалить старые учебные сети;
- либо передать `existing_network_id`;
- либо передать `existing_subnet_id`.

### 4. Jenkins не может дождаться SSH

Симптом:

- pipeline падает на `Wait For SSH`.

Что проверить:

- открыт ли `22/tcp` в security group;
- совпадает ли `ssh_user` с пользователем в Jenkins SSH credential;
- корректно ли передаётся публичный ключ в Terraform.

### 5. Локальная сборка падает на `generateJooq`

Симптом:

- ошибка подключения к `localhost:5435`;
- `Task :logic:generateJooq FAILED`.

Причина:

- при `RUN_BUILD=true` Gradle использует `.env` из workspace;
- `generateJooq` и `flywayMigrate` ожидают локальную БД Jenkins-машины;
- без локального `postgres` сборка не проходит.

Что проверить:

- включён ли Docker на Jenkins agent;
- существует ли сервис `postgres` в `docker-compose.yml`;
- содержит ли `.env` значение `MAIN_DB_URL=jdbc:postgresql://localhost:5435/main`.

### 6. Приложение отдаёт `502` или не отвечает

Симптом:

- `/healthcheck` недоступен;
- `app` контейнер падает.

Что проверить на VM:

```bash
cd /opt/restobot
sudo docker compose ps
sudo docker compose logs migrate --tail=100
sudo docker compose logs app --tail=100
```

Если раньше база уже была создана без миграций и остался старый volume, можно пересоздать стек:

```bash
sudo docker compose down -v
sudo docker compose up -d
```

### 7. Jenkins запускается не на том agent

Симптом:

- job висит в очереди и не стартует.

Что проверить:

- существует ли agent с label `Gubkovskiy_agent`;
- совпадает ли label в `Jenkinsfile` с реальным Jenkins agent.

## Что говорить на защите

Коротко суть решения:

- Jenkins выступает единой точкой запуска полного CI/CD-сценария;
- Terraform отвечает за инфраструктуру в Yandex Cloud;
- Ansible отвечает за конфигурацию VM;
- Docker Compose разворачивает PostgreSQL и приложение;
- Flyway автоматически накатывает схему базы и начальные данные;
- pipeline умеет как развернуть инфраструктуру, так и удалить её.

## Итог

В рамках Lab 6 в проекте получен полноценный Jenkins pipeline для автоматического развертывания приложения в Yandex Cloud через Terraform и Ansible.

Это решение покрывает:

- подготовку секретов;
- создание инфраструктуры;
- настройку VM;
- деплой приложения;
- миграции базы данных;
- удаление инфраструктуры.

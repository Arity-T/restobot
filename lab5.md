# Lab 5: Terraform + Ansible + Jenkins + Yandex Cloud

## Что сделано

Для лабораторной работы в проект добавлены:

- `terraform/` - описание инфраструктуры в Yandex Cloud.
- `terraform/templates/cloud-init.yaml.tftpl` - YAML-шаблон `cloud-init`, который создаёт пользователя для Ansible и готовит VM к дальнейшей настройке.
- `ansible/playbook.yml` - playbook, который ставит системное ПО и разворачивает приложение через `docker compose`.
- `Jenkinsfile` - pipeline, который запускает Terraform и Ansible из Jenkins.

Итоговый сценарий такой:

1. Jenkins получает исходники и секреты.
2. Terraform создаёт сеть, подсеть, security group и виртуальную машину в Yandex Cloud.
3. Через `cloud-init` на VM создаётся пользователь для SSH/Ansible и ставится Python.
4. Terraform генерирует `ansible/inventory/hosts.ini`.
5. Ansible подключается к созданной VM, ставит Docker Engine и Compose plugin.
6. Ansible копирует `docker-compose.yml` и `.env` на сервер и запускает стек.
7. Приложение и PostgreSQL работают в контейнерах.

## Почему выбран Docker Compose

В проекте уже есть `docker-compose.yml`, поэтому отдельная ручная установка PostgreSQL и Java на VM не нужна. На хост ставится только базовый системный софт:

- `python3` - чтобы Ansible мог работать на VM;
- Docker Engine;
- Docker Compose plugin.

Сама база поднимается как контейнер `postgres:16-alpine`, а приложение - как контейнер `thearity/restobot-app:latest`.

## Структура решения

- `terraform/main.tf` - сеть, подсеть, security group, VM, генерация inventory.
- `terraform/variables.tf` - все переменные Terraform.
- `terraform/outputs.tf` - IP-адреса и служебные output-переменные.
- `terraform/templates/cloud-init.yaml.tftpl` - YAML-шаблон начальной настройки VM.
- `terraform/templates/hosts.ini.tftpl` - шаблон inventory для Ansible.
- `ansible/playbook.yml` - установка Docker и деплой проекта.
- `Jenkinsfile` - запуск инфраструктуры из Jenkins.

## Как работает Terraform

Terraform создаёт:

- отдельную VPC-сеть;
- подсеть;
- security group;
- одну или несколько VM;
- локальный inventory-файл для Ansible.

### Что создаётся на VM

При создании VM Terraform передаёт в Yandex Cloud шаблон `cloud-init` из `terraform/templates/cloud-init.yaml.tftpl`.

Этот шаблон:

- создаёт пользователя `ssh_user`;
- добавляет ему `sudo` без пароля;
- прописывает публичный SSH-ключ;
- ставит `python3` и `python3-apt`;
- создаёт каталог `/opt/restobot`.

Это и есть требуемый YAML-шаблон для создания и первичной подготовки сервера.

### Важное ограничение по количеству серверов

Переменная `instance_count` позволяет поднять несколько VM. Но текущий playbook разворачивает на каждой VM один и тот же `docker compose` стек, а значит:

- на каждой VM будет своя копия приложения;
- на каждой VM будет свой контейнер PostgreSQL.

Для лабораторной работы это допустимо. Для реальной production-схемы с несколькими узлами лучше выносить БД отдельно.

## Как работает Ansible

Ansible запускает `ansible/playbook.yml` и выполняет:

1. обновление `apt`;
2. установку базовых системных пакетов;
3. подключение официального Docker-репозитория;
4. установку `docker-ce`, `docker-ce-cli`, `containerd.io`, `docker-buildx-plugin`, `docker-compose-plugin`;
5. запуск и включение сервиса Docker;
6. создание каталога `/opt/restobot`;
7. копирование `docker-compose.yml`;
8. копирование `.env`;
9. `docker compose pull`;
10. `docker compose up -d`.

То есть системное и прикладное ПО настраивается через Ansible, как и требуется в задании.

## Как работает Jenkins pipeline

`Jenkinsfile` теперь умеет:

1. забирать репозиторий;
2. получать секреты из Jenkins Credentials;
3. подготавливать `.env` и `terraform` variables;
4. генерировать публичный ключ из Jenkins SSH credentials;
5. выполнять `terraform init`;
6. выполнять `terraform validate`;
7. выполнять `terraform plan` и `terraform apply` или `terraform destroy`;
8. ждать готовности SSH;
9. запускать `ansible-playbook`.

Параметры Jenkins job:

- `TF_ACTION=apply` - создать или обновить инфраструктуру.
- `TF_ACTION=destroy` - удалить инфраструктуру.
- `RUN_BUILD=true` - дополнительно прогнать `./gradlew build` перед инфраструктурой.

## Подготовка Yandex Cloud

Ниже приведён практический порядок подготовки Yandex Cloud под этот проект.

### 1. Создать облако и каталог

Нужно:

- включить биллинг;
- создать cloud;
- создать folder, в котором будет жить инфраструктура.

### 2. Выдать права своей пользовательской учётной записи

Если не использовать service account, Terraform можно запускать от вашей пользовательской учётной записи в Yandex Cloud.

Для этого достаточно, чтобы у вашей учётной записи были права на каталог, где создаётся инфраструктура. Для лабораторной работы обычно хватает роли `editor` на folder.

Почему этого достаточно:

- Terraform будет создавать сеть;
- Terraform будет создавать подсеть;
- Terraform будет создавать VM;
- Terraform будет назначать публичный IP;
- Terraform будет управлять security group.

Для production лучше использовать service account и более узкие роли, но для лабораторной пользовательская учётная запись тоже подходит.

### 3. Авторизоваться в `yc` CLI и получить IAM token

Сначала нужно авторизоваться в Yandex Cloud CLI:

```bash
yc init
```

После этого можно выпустить IAM token для Terraform:

```bash
export YC_TOKEN=$(yc iam create-token)
```

Важно:

- `YC_TOKEN` не хранится в Terraform-файлах;
- токен живёт ограниченное время, обычно не более 12 часов;
- если токен истёк, достаточно выпустить новый той же командой.

### 4. Создать SSH-ключ для входа на VM

Например:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/restobot_vm
```

Нужны оба файла:

- приватный ключ - для Jenkins/Ansible;
- публичный ключ - для Terraform, чтобы положить его в VM.

### 5. Подготовить Terraform variables

Скопируй пример:

```bash
cp terraform/terraform.tfvars.example terraform/terraform.auto.tfvars
```

Пример содержимого:

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

Отдельно нужно передать:

- `ssh_public_key_path`;
- `ssh_user`.

Локально это можно сделать через `-var`, через `TF_VAR_*` или дописать в отдельный `.tfvars`.

### 6. Подготовить `.env`

Создай `.env` в корне проекта на основе `.env.example`.

Минимально нужно заполнить:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `MAIN_DB_USER`
- `MAIN_DB_PASSWORD`
- `TRIPADVISOR_API_KEY`
- `TRIPADVISOR_API_HOST`
- `TRIPADVISOR_API_LANGUAGE`
- `API_SERVER_HOST`
- `API_SERVER_PORT`

Для деплоя через Compose на сервере `MAIN_DB_URL` внутри контейнера приложения переопределяется значением `jdbc:postgresql://postgres:5432/main`.

## Локальный запуск без Jenkins

Если нужно сначала проверить всё руками, порядок такой.

### 1. Инициализация Terraform

```bash
cd terraform
terraform init
```

Если переменные передаются через environment:

```bash
export YC_TOKEN=$(yc iam create-token)
export TF_VAR_ssh_public_key_path="$HOME/.ssh/restobot_vm.pub"
export TF_VAR_ssh_user="restobot"
```

### 2. Просмотр плана

```bash
terraform plan -var-file=terraform.auto.tfvars
```

### 3. Создание инфраструктуры

```bash
terraform apply -var-file=terraform.auto.tfvars
```

После этого Terraform:

- создаст инфраструктуру;
- выведет публичные IP-адреса;
- сгенерирует `ansible/inventory/hosts.ini`.

### 4. Запуск Ansible

Из корня репозитория:

```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/playbook.yml --private-key ~/.ssh/restobot_vm -u restobot
```

### 5. Проверка результата

Проверить output Terraform:

```bash
cd terraform
terraform output
```

Затем открыть:

```text
http://<public_ip>:8089
```

Также можно проверить контейнеры на VM:

```bash
ssh -i ~/.ssh/restobot_vm restobot@<public_ip>
cd /opt/restobot
docker compose ps
```

## Как разворачивать через Jenkins

### Что должно быть установлено на Jenkins agent

На Linux-агенте Jenkins должны быть:

- Terraform;
- Ansible;
- OpenSSH client;
- `ssh-keygen`;
- при использовании `RUN_BUILD=true` ещё и JDK 23.

### Какие Credentials нужны в Jenkins

Ниже ожидаемые credentials IDs:

- `restobot_env` - Secret file с содержимым `.env`.
- `restobot_tfvars` - Secret file с содержимым `terraform.auto.tfvars`.
- `yc_iam_token` - Secret text с IAM token пользовательской учётной записи.
- `restobot_vm_ssh` - SSH Username with private key.

`restobot_vm_ssh` должен содержать:

- username, например `restobot`;
- приватный ключ, соответствующий публичному ключу для VM.

### Как запустить job

Для создания инфраструктуры:

1. открыть job в Jenkins;
2. выбрать `TF_ACTION=apply`;
3. при необходимости включить `RUN_BUILD=true`;
4. запустить job.

Что произойдёт:

1. Jenkins соберёт файлы и секреты.
2. Terraform создаст инфраструктуру в Yandex Cloud, используя `YC_TOKEN`.
3. Terraform сгенерирует inventory для Ansible.
4. Jenkins дождётся готовности SSH.
5. Ansible настроит VM и поднимет контейнеры.

Для удаления инфраструктуры:

1. выбрать `TF_ACTION=destroy`;
2. запустить job.

В этом случае Jenkins выполнит `terraform destroy`.

## Remote state для Terraform

Для Jenkins лучше хранить Terraform state не локально в workspace, а в Yandex Object Storage. Для этого в репозиторий добавлен шаблон `terraform/backend.tf.example`.

Порядок:

1. создать bucket в Object Storage;
2. скопировать:

```bash
cp terraform/backend.tf.example terraform/backend.tf
```

3. заполнить `bucket` и `key`;
4. задать переменные окружения для доступа к S3-совместимому backend:

```bash
export AWS_ACCESS_KEY_ID="<static_access_key_id>"
export AWS_SECRET_ACCESS_KEY="<static_secret_key>"
```

5. переинициализировать Terraform:

```bash
cd terraform
terraform init -reconfigure
```

Если используется Jenkins, эти переменные лучше хранить в Jenkins Credentials или Jenkins environment.

При использовании пользовательского IAM token в Jenkins есть ограничение: такой токен надо периодически обновлять вручную, потому что он истекает. Для постоянного CI-сценария service account надёжнее.

## Какой сетевой доступ открывается

Terraform создаёт security group, которая открывает:

- `22/tcp` - для SSH и Ansible;
- `8089/tcp` - для приложения.

Порт PostgreSQL наружу security group не открывает. Это значит:

- контейнер PostgreSQL внутри VM работает;
- снаружи напрямую база недоступна;
- это безопаснее для лабораторной схемы.

## Полный сценарий лабораторной работы

Итоговый порядок выполнения лабораторной можно описать так:

1. Подготовить Yandex Cloud: cloud, folder, права для пользовательской учётной записи, SSH-ключи.
2. Подготовить `terraform.auto.tfvars`.
3. Подготовить `.env`.
4. Выпустить `YC_TOKEN` через `yc iam create-token`.
5. Настроить Jenkins credentials.
6. Запустить Jenkins job с `TF_ACTION=apply`.
7. Jenkins вызовет Terraform.
8. Terraform создаст VM и сгенерирует inventory.
9. Jenkins вызовет Ansible.
10. Ansible установит Docker и поднимет проект через Compose.
11. Проверить доступность приложения по публичному IP и порту `8089`.
12. После демонстрации удалить ресурсы через `TF_ACTION=destroy`.

## Что говорить на защите

Коротко суть решения:

- инфраструктура создаётся декларативно через Terraform;
- первичная инициализация VM делается через YAML `cloud-init`;
- системное и прикладное ПО ставится через Ansible;
- Jenkins выступает точкой запуска всей цепочки;
- приложение разворачивается контейнерно через уже существующий `docker-compose.yml`;
- БД не ставится вручную как системный пакет, а запускается как контейнер вместе с приложением.

## Полезные официальные материалы Yandex Cloud

Конфигурация в репозитории опирается на официальные материалы:

- cloud-init для VM: https://yandex.cloud/ru/docs/compute/operations/vm-create/create-with-cloud-init-scripts
- Terraform state в Object Storage: https://yandex.cloud/ru/docs/tutorials/infrastructure-management/terraform-state-storage
- Terraform data sources: https://yandex.cloud/ru/docs/terraform/tutorials/terraform-data-sources

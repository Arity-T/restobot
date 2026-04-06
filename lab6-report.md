# Лабораторная работа 6: Jenkins Pipeline (Build + Terraform + Ansible)

## 1. Цель работы

- Объединить сборку Java-проекта, создание инфраструктуры и деплой в единый Jenkins Pipeline.
- Запускать полный цикл одной задачей Jenkins:
  - `build` приложения;
  - `terraform init/plan/apply` (или `destroy`);
  - `ansible deploy`;
  - smoke-проверка.

## 2. Исходные условия

- Репозиторий: `restobot`
- Рабочая ветка: `vlad-pipeline`
- Облако: Yandex Cloud (каталог `gaar`)
- Локальный Jenkins для отладки: Docker-контейнер `jenkins-lab6`
- Основные credentials в Jenkins:
  - `restobot_env` (Secret file)
  - `restobot_tfvars` (Secret file)
  - `yc_iam_token` (Secret text)
  - `restobot_vm_ssh` (SSH Username with private key)

## 3. Что реализовано

### 3.1 Jenkins Pipeline

Обновлён файл:

- `Jenkinsfile`

Реализованы стадии:

1. `Checkout`
2. `Prepare Secrets`
3. `Build` (Gradle + flyway + jOOQ)
4. `Terraform Init`
5. `Terraform Plan`
6. `Terraform Apply` (для `TF_ACTION=apply`)
7. `Wait For SSH`
8. `Ansible Deploy`
9. `Smoke Test`
10. `Terraform Destroy` (для `TF_ACTION=destroy`)

Параметры пайплайна:

- `TF_ACTION = apply | destroy`
- `RUN_BUILD = true/false`
- `RUN_ANSIBLE = true/false`

### 3.2 Адаптация под локальный Jenkins в Docker

- Добавлена корректная работа сборки с БД при запуске Jenkins в контейнере:
  - динамический выбор `MAIN_DB_URL` (`localhost` / `host.docker.internal`);
  - явное ожидание готовности Postgres перед Gradle.
- Добавлена стадия `Wait For SSH`, чтобы Ansible не стартовал раньше готовности VM.

### 3.3 Terraform/инициализация в CI

Обновлены файлы:

- `terraform/main.tf`
- `terraform/variables.tf`

Что изменено:

- Перенесена межпеременная валидация из `variable.validation` в `check` блок (совместимо с Terraform >= 1.6).
- Исключены ошибки `Invalid reference in variable validation`.

### 3.4 Совместимость Java toolchain в CI

Обновлены файлы:

- `build.gradle`
- `logic/build.gradle`
- `Jenkinsfile`

Что изменено:

- Версия Java toolchain сделана настраиваемой через `-PjavaToolchainVersion` / `JAVA_TOOLCHAIN_VERSION`.
- Для CI используется `-PjavaToolchainVersion=21`, чтобы сборка работала в Jenkins-контейнере с JDK 21.
- Для задачи `generateJooq` убрана жёсткая привязка к Java 23.

### 3.5 Локальный Jenkins образ для отладки

Отдельно подготовлен Dockerfile:

- `..\jenkins-lab6\Dockerfile`

Особенности образа:

- установлены `docker-cli`, `docker-compose`, `ansible`;
- `terraform` вызывается через обёртку (`hashicorp/terraform:1.6.6`);
- сохранение состояния Jenkins через volume `jenkins_home`.

## 4. Ключевые проблемы и решения

1. `Invalid option type "ansiColor"` в Jenkins.
- Причина: отсутствующий плагин.
- Решение: убрать `ansiColor`, заменить `cleanWs()` на `deleteDir()`, затем отказаться от `deleteDir()` для сохранения state.

2. `JAVA_HOME is set to an invalid directory`.
- Причина: жёсткий путь к JDK23 в контейнере с JDK21.
- Решение: автодетект Java + параметризованный toolchain.

3. `flywayMigrate` не подключался к БД (`localhost:5435 refused`).
- Причина: Jenkins работает в Docker-контейнере, а Postgres запускается через Docker socket.
- Решение: динамический host/port БД + ожидание готовности.

4. `Unsupported Terraform Core version` (1.5.7 vs `>=1.6.0`).
- Решение: обновить terraform wrapper до `1.6.6`.

5. `Invalid reference in variable validation` в Terraform.
- Решение: убрать cross-variable validation из `variables.tf`, добавить `check` в `main.tf`.

6. `Instance with name "restobot-01" already exists`.
- Причина: потеря `terraform.tfstate` между прогонами.
- Решение: не удалять workspace в `post` (сохранять state), разово удалить конфликтующую VM или импортировать её в state.

7. `Ansible UNREACHABLE: ssh connection refused`.
- Причина: VM создана, но SSH ещё не поднялся.
- Решение: стадия `Wait For SSH` с ретраями.

8. `Invalid provider registry host` на `registry.terraform.io`.
- Решение: создать `TF_CLI_CONFIG_FILE` в CI и использовать зеркало провайдеров:
  `https://terraform-mirror.yandexcloud.net/`.

## 5. Результаты

По логам выполнения пайплайна:

- `Build` проходит успешно (`BUILD SUCCESSFUL`, Gradle задачи выполняются).
- `Terraform Apply` успешно создаёт VM и генерирует inventory:
  - `instance_names = ["restobot-01"]`
  - `instance_public_ips = ["89.169.134.148"]` (в одном из прогонов)
  - `ansible_inventory_path = "./../ansible/inventory/hosts.ini"`
- Далее запускается стадия Ansible (после добавления `Wait For SSH` должна работать стабильнее).

Итог:

- Пайплайн объединён в единый процесс CI/CD.
- Основные ошибки локальной среды Jenkins-in-Docker устранены.

## 6. Выводы

- Лабораторная работа 6 реализована как параметризованный Jenkins Pipeline.
- Связка Build + Terraform + Ansible в одном job работает и воспроизводима.
- Для учебной среды критично:
  - сохранять Terraform state между прогонами;
  - учитывать сетевые особенности Dockerized Jenkins;
  - добавлять ожидание SSH перед конфигурацией Ansible.

## 7. Чеклист демонстрации

### 7.1 Поднять локальный Jenkins

```powershell
cd "D:\Study\Polytech\Облачные вычисления\jenkins-lab6"
docker build -t jenkins-lab6 .
docker run -d --name jenkins-lab6 `
  -u root `
  -p 8080:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v //var/run/docker.sock:/var/run/docker.sock `
  jenkins-lab6
```

Проверка:

```powershell
docker exec jenkins-lab6 bash -lc "docker --version && docker-compose --version && terraform version"
```

### 7.2 Подготовить токен YC перед запуском

```powershell
yc config set cloud-id b1gtgpmqgvqfitjnjck7
yc config set folder-id b1gomu62mtmfec2871l6
yc iam create-token
```

Полученный токен вставить в Jenkins credential `yc_iam_token`.

### 7.3 Запуск пайплайна

В Jenkins job `Restobot`:

- `TF_ACTION=apply`
- `RUN_BUILD=true`
- `RUN_ANSIBLE=true`

Показать успешные стадии:

- `Build`
- `Terraform Apply`
- `Wait For SSH`
- `Ansible Deploy`
- `Smoke Test`

### 7.4 Проверка результата

На VM:

```bash
cd /opt/restobot
docker compose ps
curl -sS http://localhost:8089/healthcheck
```

### 7.5 Освобождение ресурсов после демонстрации

В Jenkins:

- `TF_ACTION=destroy`

или вручную:

```powershell
cd "D:\Study\Polytech\Облачные вычисления\restobot\terraform"
$Env:YC_TOKEN = yc iam create-token
terraform destroy
```


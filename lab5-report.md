# Лабораторная работа 5: Terraform + Ansible (Yandex Cloud)

## 1. Цель работы

- Создать инфраструктуру в Yandex Cloud через Terraform.
- Настроить системное и прикладное ПО через Ansible.
- Поднять приложение `restobot` на удаленной VM.
- Проверить работоспособность по `healthcheck`.

## 2. Исходные условия

- Репозиторий: `restobot`
- Текущая ветка: `vlad-terraform`
- Облако: `cloud-alexey-lukashin` (`b1gtgpmqgvqfitjnjck7`)
- Каталог: `gaar` (`b1gomu62mtmfec2871l6`)
- Аутентификация Terraform: через `YC_TOKEN` (без service account key)

## 3. Что было реализовано

### 3.1 Terraform

Добавлены/обновлены файлы:

- `terraform/versions.tf`
- `terraform/variables.tf`
- `terraform/main.tf`
- `terraform/outputs.tf`
- `terraform/terraform.tfvars.example`
- `terraform/backend.tf.example`
- `terraform/README.md`
- `terraform/templates/cloud-init.yaml.tftpl`
- `terraform/templates/hosts.ini.tftpl`
- `terraform/terraform.tfvars` (локальный рабочий)

Ключевые решения:

- Добавлен режим переиспользования существующих сетевых ресурсов:
  - `create_network_resources = false`
  - `existing_subnet_id`
  - `existing_security_group_ids`
- Это позволило обойти квотные ограничения на создание новых VPC/SG в cloud.
- Terraform создает VM и генерирует Ansible inventory:
  - `ansible/inventory/hosts.ini`

Использованные существующие ресурсы:

- `subnet_id = e9bprav07fpo1uqbp497` (`default-ru-central1-a`)
- `security_group_id = enpkd9np0qbhc064o9mu` (`default-sg-enpq1korg6qpq5kr687c`)

### 3.2 Ansible

Добавлены/обновлены файлы:

- `ansible/ansible.cfg`
- `ansible/playbook.yml`
- `ansible/inventory/.gitkeep`

Playbook выполняет:

- установку базового ПО и Docker/Compose;
- копирование `docker-compose.yml`, `.env` и SQL-миграций;
- нормализацию `.env` в LF;
- фиксацию `API_SERVER_HOST=0.0.0.0`;
- запуск `postgres`, ожидание готовности БД;
- применение миграций при необходимости;
- запуск `app` через `docker compose up -d --force-recreate app`;
- ожидание доступности порта приложения.

### 3.3 Docker Compose

Обновлен файл:

- `docker-compose.yml`

Изменения:

- удален устаревший `version`;
- `API_SERVER_HOST` зафиксирован как `0.0.0.0` для корректного внешнего доступа.

### 3.4 Сопутствующее

Обновлен `.gitignore`:

- Terraform state/служебные файлы;
- `ansible/inventory/hosts.ini` (генерируется автоматически).

## 4. Проблемы и как были решены

1. Ошибка квоты при создании сети:
- `Quota limit vpc.networks.count exceeded`
- Решение: не создавать сеть/подсеть/SG, а использовать существующие ID.

2. Нет прав на выдачу IAM-роли service account:
- `PermissionDenied` на `add-access-binding`
- Решение: использовать `YC_TOKEN` от пользовательской учетной записи.

## 5. Результат

Terraform:

- VM создана успешно:
  - `id=fhm966hbjr5n3bn3cfkj`
  - `public_ip=89.169.134.154`
  - `private_ip=10.128.0.11`

Ansible:

- `PLAY RECAP`: `failed=0`, `unreachable=0`

Проверка приложения на VM:

```bash
curl -sS http://localhost:8089/healthcheck
```

Ответ:

```json
{"lastTripAdvisorCallTime":"2026-04-05T15:17:46.339027661Z","status":"OK","authors":["Тищенко Артём","Гаар Владислав","Губковский Дмитрий"]}
```

## 6. Выводы

- Terraform и Ansible связаны в единый рабочий процесс для разворачивания `restobot`.
- Для учебной среды с ограниченными IAM/квотами работоспособен вариант с `YC_TOKEN` и переиспользованием общей подсети/SG.
- Автоматизация (Ansible) устранила ручные шаги по Docker, `.env`, миграциям БД и запуску приложения.
- Итог: инфраструктура и приложение поднимаются воспроизводимо, проверка `healthcheck` проходит успешно.

## 7. Что сделать перед/после защиты

Перед защитой:

- проверить, что VM в `RUNNING`;
- показать `terraform plan/apply`, `ansible-playbook`, `healthcheck`.

После защиты:

```bash
terraform destroy
```

чтобы не платить за ресурсы.

## 8. Команды для демонстрации (чеклист)

### 8.1 На Windows (PowerShell): подготовить облако, VM и Terraform

```powershell
cd "D:\Study\Polytech\Облачные вычисления\restobot\terraform"

# Убедиться, что выбран нужный cloud/folder
yc config set cloud-id b1gtgpmqgvqfitjnjck7
yc config set folder-id b1gomu62mtmfec2871l6
yc config list

# Запустить VM (если остановлена) и проверить статус
yc compute instance start fhm966hbjr5n3bn3cfkj
yc compute instance list --folder-id b1gomu62mtmfec2871l6

# Перегенерировать токен перед запуском Terraform
$Env:YC_TOKEN = yc iam create-token

# Обновить state/inventory и показать план/применение
terraform plan
terraform apply
```

### 8.2 На WSL: повторно применить Ansible

```bash
cd /mnt/d/Study/Polytech/Облачные\ вычисления/restobot/ansible
ANSIBLE_CONFIG=./ansible.cfg ansible-playbook playbook.yml --private-key ~/.ssh/id_ed25519_yc -u restobot
```

### 8.3 Проверка приложения

На VM:

```bash
cd /opt/restobot
docker compose ps -a
curl -sS http://localhost:8089/healthcheck
```

С локальной машины:

```powershell
curl http://89.169.134.154:8089/healthcheck
```

### 8.4 После демонстрации

Остановить VM (сохранить стенд, снизить затраты):

```powershell
yc compute instance stop fhm966hbjr5n3bn3cfkj
```

Полностью удалить инфраструктуру:

```powershell
cd "D:\Study\Polytech\Облачные вычисления\restobot\terraform"
$Env:YC_TOKEN = yc iam create-token
terraform destroy
```

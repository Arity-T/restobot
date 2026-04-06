# Лабораторная работа 7: Kubernetes

## 1. Цель работы

- Убрать этап создания инфраструктуры из пайплайна.
- Реализовать доставку и запуск приложения `restobot` в Kubernetes.
- Поднять приложение в общем формате CI/CD:
  - сборка Java-проекта;
  - сборка Docker-образа;
  - деплой в Kubernetes;
  - инициализация БД;
  - проверка доступности приложения.

## 2. Исходные условия

- Репозиторий: `restobot`
- Рабочая ветка: `vlad-k8s`
- Kubernetes: локальный кластер `docker-desktop`
- Jenkins: локально в Docker-контейнере
- Развёртывание выполняется без Terraform и Ansible

Используемые Jenkins credentials:

- `restobot_env` - Secret file с переменными окружения приложения
- `restobot_kubeconfig` - Secret file с kubeconfig локального кластера
- `docker_registry` - Username/Password credential для push образа (необязательно, только при `PUSH_IMAGE=true`)

## 3. Что было реализовано

### 3.1 Jenkins Pipeline

Полностью обновлён файл:

- `Jenkinsfile`

Новый пайплайн выполняет:

1. `Checkout`
2. `Prepare Kubeconfig`
3. `Prepare App Env`
4. `Build`
5. `Build Docker Image`
6. `Push Docker Image` (опционально)
7. `Deploy To Kubernetes`
8. `Smoke Test`
9. `Delete Kubernetes Resources`

Добавлены параметры пайплайна:

- `K8S_ACTION = deploy | delete`
- `RUN_BUILD = true | false`
- `PUSH_IMAGE = true | false`
- `IMAGE_REPOSITORY`
- `IMAGE_TAG`
- `K8S_NAMESPACE`
- `DELETE_NAMESPACE`

### 3.2 Kubernetes manifests

Добавлены файлы:

- `k8s/postgres.yaml`
- `k8s/db-init-job.yaml`
- `k8s/app-deployment.yaml`
- `k8s/app-service.yaml`
- `k8s/README.md`

Реализованные сущности:

- `Deployment` + `Service` для PostgreSQL
- `Job` для применения SQL-миграций и начального наполнения БД
- `Deployment` для приложения `restobot`
- `NodePort Service` для внешнего доступа к приложению

### 3.3 Вспомогательная обёртка для kubectl

Добавлен файл:

- `scripts/kctl.sh`

Назначение:

- запуск `kubectl` через Docker-образ `bitnami/kubectl`
- работа из Jenkins-in-Docker без локальной установки `kubectl` внутрь Jenkins-контейнера
- поддержка stdin для команд вида `kubectl apply -f -`

### 3.4 Сопутствующие изменения

Обновлены файлы:

- `.gitignore`
- `.gitattributes`

Добавлено:

- игнорирование `.kubeconfig`
- игнорирование `k8s/.rendered/`
- LF для `*.sh`, `*.yaml` и `Jenkinsfile`

## 4. Проблемы и как были решены

1. Не было общего Kubernetes-кластера в Yandex Cloud.
- Решение: использовать локальный Kubernetes из Docker Desktop.

2. Для Managed Kubernetes в Yandex Cloud не хватало IAM-прав.
- Решение: отказаться от развёртывания собственного YC-кластера в пользу локального стенда.

3. `DOCKER_IMAGE: unbound variable` в Jenkins pipeline.
- Причина: переменная не была гарантированно инициализирована во всех `sh`-блоках.
- Решение: добавить fallback-вычисление `DOCKER_IMAGE` и `IMAGE_PULL_POLICY` в соответствующих стадиях.

4. `docker.io/bitnami/kubectl:1.30: not found`.
- Причина: неверный тег образа.
- Решение: использовать `bitnami/kubectl:latest`.

5. `error: no objects passed to apply`.
- Причина: обёртка `docker run` для `kubectl` не передавала stdin.
- Решение: добавить ключ `-i` в `scripts/kctl.sh`.

## 5. Результат

После успешного запуска пайплайна в локальном Kubernetes были подняты:

- `pod/restobot-app-568c8c44f5-rp9sw` - `Running`
- `pod/restobot-postgres-5fddb6d4fb-f7p7f` - `Running`
- `job/restobot-db-init` - `Complete`

Сервисы:

- `service/restobot-app` - `NodePort 8089:31923/TCP`
- `service/restobot-postgres` - `ClusterIP 5432/TCP`

Логи job миграции подтвердили успешную инициализацию БД:

```text
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
CREATE TABLE
SET
INSERT 0 25
INSERT 0 16
INSERT 0 3
INSERT 0 1
Migrations completed.
```

Проверка приложения:

```powershell
$port = kubectl -n restobot get svc restobot-app -o jsonpath="{.spec.ports[0].nodePort}"
curl "http://localhost:$port/healthcheck"
```

Ответ:

```json
{"lastTripAdvisorCallTime":"2026-04-06T18:15:28.234199517Z","status":"OK","authors":["Тищенко Артём","Гаар Владислав","Губковский Дмитрий"]}
```

## 6. Выводы

- Пайплайн для л/р 7 успешно переведён с модели `Terraform + Ansible` на модель `Docker + Kubernetes`.
- Приложение, база данных и миграции разворачиваются внутри Kubernetes автоматически.
- Для учебной задачи локальный Kubernetes в Docker Desktop оказался достаточным и заметно проще, чем развёртывание собственного кластера в Yandex Cloud без нужных IAM-ролей.
- Итог: Jenkins доставляет артефакт в Kubernetes и приложение проходит проверку по `healthcheck`.

## 7. Команды для демонстрации

### 7.1 Подготовить локальный Kubernetes

```powershell
kubectl config use-context docker-desktop
kubectl get nodes
kubectl get pods -A
```

### 7.2 Запустить Jenkins pipeline

В Jenkins job `Restobot`:

- `K8S_ACTION=deploy`
- `RUN_BUILD=true`
- `PUSH_IMAGE=false`
- `IMAGE_REPOSITORY=restobot-app`
- `K8S_NAMESPACE=restobot`

### 7.3 Проверить ресурсы Kubernetes

```powershell
kubectl -n restobot get pods,svc,jobs
kubectl -n restobot logs deployment/restobot-app --tail=100
kubectl -n restobot logs job/restobot-db-init --tail=100
```

### 7.4 Проверить доступность приложения

```powershell
$port = kubectl -n restobot get svc restobot-app -o jsonpath="{.spec.ports[0].nodePort}"
curl "http://localhost:$port/healthcheck"
```

### 7.5 Удалить ресурсы после демонстрации

В Jenkins:

- `K8S_ACTION=delete`
- `K8S_NAMESPACE=restobot`

Либо вручную:

```powershell
kubectl -n restobot delete deployment restobot-app --ignore-not-found=true
kubectl -n restobot delete service restobot-app --ignore-not-found=true
kubectl -n restobot delete job restobot-db-init --ignore-not-found=true
kubectl -n restobot delete deployment restobot-postgres --ignore-not-found=true
kubectl -n restobot delete service restobot-postgres --ignore-not-found=true
kubectl -n restobot delete configmap restobot-db-migrations --ignore-not-found=true
kubectl -n restobot delete secret restobot-env --ignore-not-found=true
```


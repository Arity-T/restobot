# Lab 7: доставка артефакта в minikube через Jenkins

## Что сделано в проекте

В репозиторий добавлены:

- `Jenkinsfile` для полного CI/CD-процесса;
- каталог `k8s/` с манифестами Kubernetes;
- `Dockerfile` для запуска `app-fat.jar` внутри контейнера.

Итоговый сценарий такой:

1. Jenkins забирает код из репозитория.
2. Создает временную локальную PostgreSQL в Docker для сборки проекта.
3. Выполняет:
   - `:logic:flywayMigrate`
   - `:logic:generateJooq`
   - `build`
   - `:app:shadowJar`
4. Собирает Docker-образ `restobot-app:<BUILD_NUMBER>`.
5. Загружает образ в `minikube`.
6. Создает или обновляет Kubernetes `Secret` с переменными окружения.
7. Создает или обновляет `ConfigMap` с SQL-миграциями.
8. Деплоит PostgreSQL в namespace `restobot`.
9. Запускает Kubernetes Job для миграций.
10. Деплоит приложение `restobot-app`.

## Файлы

- `Jenkinsfile` - основной Jenkins pipeline.
- `k8s/namespace.yaml` - namespace `restobot`.
- `k8s/postgres.yaml` - PostgreSQL + PVC + Service.
- `k8s/migration-job.yaml` - Job для применения SQL-миграций.
- `k8s/app.yaml` - Deployment и Service для приложения.

## Требования

На той машине, где запускается Jenkins agent, должны быть доступны в `PATH`:

- `java` 23
- `docker`
- `kubectl`
- `minikube`
- `git`

Также должны быть запущены:

- локальный `Docker`
- локальный `minikube`
- локальный `Jenkins`

Pipeline рассчитан на то, что Jenkins и `minikube` видят один и тот же Docker daemon. Самый простой вариант для лабы: Jenkins запускать на хосте, а не в изолированном контейнере.

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

## Подготовка minikube

Запусти `minikube`:

```bash
minikube start
```

Убедись, что включены storage addons:

```bash
minikube addons enable default-storageclass
minikube addons enable storage-provisioner
```

Проверь контекст:

```bash
kubectl config current-context
```

Ожидаемое значение:

```text
minikube
```

Если у тебя профиль `minikube` называется иначе, поменяй значение `MINIKUBE_PROFILE` в `Jenkinsfile`.

## Подготовка переменных окружения

В корне проекта уже есть пример:

```bash
.env.example
```

Создай на его основе файл `.env`:

```bash
cp .env.example .env
```

Заполни реальные значения:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `MAIN_DB_USER`
- `MAIN_DB_PASSWORD`
- `TRIPADVISOR_API_KEY`
- `TRIPADVISOR_API_HOST`
- `TRIPADVISOR_API_LANGUAGE`
- `API_SERVER_HOST`
- `API_SERVER_PORT`

Важно:

- для локального запуска можешь оставить `MAIN_DB_URL`, как в примере;
- в Jenkins pipeline этот URL автоматически подменяется для этапа сборки;
- в Kubernetes приложение все равно получает `MAIN_DB_URL=jdbc:postgresql://restobot-postgres:5432/main`.

## Подготовка Jenkins

### 1. Плагины

Желательно, чтобы в Jenkins были установлены:

- `Pipeline`
- `Git`
- `Credentials Binding`
- `AnsiColor`

### 2. Credentials

В Jenkins создай credential типа `Secret file`:

- `ID`: `restobot_env`
- содержимое файла: твой `.env`

### 3. Pipeline job

Создай job типа `Pipeline` и настрой:

- `Definition`: `Pipeline script from SCM`
- `SCM`: `Git`
- `Script Path`: `Jenkinsfile`

После первого сохранения у job появится параметр:

- `DESTROY_DEPLOYMENT`

Режимы работы:

- `false` - обычная сборка и деплой в `minikube`;
- `true` - удалить все ресурсы, развернутые пайплайном, без сборки и без деплоя.

Если Jenkins запускается не на хосте, а в контейнере, ему нужно дополнительно дать доступ к:

- Docker daemon
- `kubectl`
- `minikube`
- kubeconfig текущего пользователя

Для лабы удобнее не усложнять и использовать локальный Jenkins/agent на хосте.

## Что делает Jenkinsfile

### Этап `Prepare environment`

- берет `.env` из Jenkins credentials;
- сохраняет оригинал как `.env.k8s`;
- создает рабочий `.env` для Gradle со специальным `MAIN_DB_URL` на временную CI-базу.

### Этап `Build artifact`

- поднимает временный контейнер `postgres:16-alpine`;
- создает базу `main`;
- запускает миграции Flyway;
- генерирует `jOOQ`;
- собирает проект и `app-fat.jar`.

### Этап `Build Docker image`

- собирает Docker-образ:

```text
restobot-app:<BUILD_NUMBER>
```

### Этап `Deploy to minikube`

- применяет namespace;
- создает `Secret` `restobot-app-env`;
- создает `ConfigMap` `restobot-db-migrations` из SQL-файлов;
- деплоит PostgreSQL;
- загружает образ в `minikube`;
- запускает Job `restobot-db-migrate`;
- деплоит `restobot-app`;
- обновляет image в deployment.

## Первый запуск

После настройки Jenkins просто запусти pipeline с параметром:

```text
DESTROY_DEPLOYMENT=false
```

Если все прошло успешно, проверь ресурсы:

```bash
kubectl get all -n restobot
```

Ожидаемо должны появиться:

- `deployment/restobot-postgres`
- `deployment/restobot-app`
- `job/restobot-db-migrate`
- `service/restobot-postgres`
- `service/restobot-app`
- `pvc/restobot-postgres-data`

## Проверка приложения

Получить URL сервиса:

```bash
minikube service restobot-app -n restobot --url
```

Проверка healthcheck:

```bash
curl "$(minikube service restobot-app -n restobot --url)/healthcheck"
```

Также можно проверить вручную через NodePort:

```bash
curl "http://$(minikube ip):30089/healthcheck"
```

На macOS с Docker driver чаще удобнее использовать именно `minikube service ... --url`.

## Полезные команды диагностики

Список pod'ов:

```bash
kubectl get pods -n restobot
```

Логи приложения:

```bash
kubectl logs deployment/restobot-app -n restobot
```

Логи миграций:

```bash
kubectl logs job/restobot-db-migrate -n restobot
```

Описание pod:

```bash
kubectl describe pod <pod-name> -n restobot
```

## Повторный деплой

При следующем запуске pipeline:

- будет собран новый Docker-образ с новым тегом;
- образ снова загрузится в `minikube`;
- deployment `restobot-app` обновит image;
- migration job пропустит уже примененные SQL-файлы благодаря таблице `restobot_schema_history`.

## Очистка стенда

Через Jenkins:

- запусти тот же pipeline с параметром `DESTROY_DEPLOYMENT=true`;
- pipeline удалит namespace `restobot`, а вместе с ним и все развернутые ресурсы.

Вручную удалить все ресурсы приложения:

```bash
kubectl delete namespace restobot
```

Остановить `minikube`:

```bash
minikube stop
```

## Коротко для защиты

В этой лабораторной Jenkins выступает как CI/CD-оркестратор:

- собирает Java-проект;
- формирует Docker-артефакт;
- доставляет образ в `minikube`;
- выполняет миграции базы;
- разворачивает приложение в Kubernetes.

То есть доставка артефакта в локальный кластер происходит полностью автоматически через Jenkins pipeline.

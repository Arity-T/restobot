# RestoBot

Этот бот поможет вам мгновенно найти случайный ресторан в радиусе одного километра от вашей текущей геолокации. При желании вы сможете уточнить город, выбрать предпочитаемые кухни, указать ценовой диапазон, а также дополнить запрос некоторыми ключевыми словами. Кроме того, бот позволяет сохранять понравившиеся заведения в список избранных ресторанов и отмечать посещённые рестораны. 

Бот поддерживает: Москва, Санкт-Петербург, Новосибирск, Екатеринбург, Казань, Красноярск, Нижний Новгород, Челябинск, Уфа, Самара, Ростов-на-Дону, Краснодар, Омск, Воронеж, Пермь, Волгоград. 

Поддерживаются следующие типы кухни: африканская, азиатская, американская, барбекю, ближневосточная, британская, вьетнамская, восточно-европейская, европейская, ирландская, испанская, итальянская, индийская, каджунская, карибская, китайская, мексиканская, немецкая, средиземноморская, тайская, французская, фьюжн, греческая, японская, южноамериканская.

Вы можете выбрать следующие ценовые категории: Дешевое питание, Средний ценовой сегмент, Высокая кухня.

Ссылка на Telegram-бота:  

https://t.me/VADRestoBot

Ссылка на Docker-hub:

https://hub.docker.com/r/thearity/restobot-app


## Технологический стек

- **Язык**: Java 23
- **База данных**: PostgreSQL 16
- **Инструменты**:
  - **Gradle 8.10**
  - **JOOQ 10.1**
  - **Spring 6.2.3**
  - **WebFlux 6.2.3**
  - **Spring Rest Docs 3.0.2**
  - **JUnit 5.10.2**
  - **Docker 27.0.3**
  - **Telegram API 8.3.0**
  - **GSON 2.13.1**
  - **Logback 1.5.18**


## Структура проекта

Проект представляет собой [*gradle multi-project*](https://docs.gradle.org/current/userguide/multi_project_builds.html). Каждый подпроект содержит одноименный Java пакет с префиксом `dev.tishenko.restobot`.

Java applications:
- `telegram` - telegram бот (dependencies: `logic`).

Java libraries:
- `api` - обработка запросов на наш API (dependencies: null).
- `logic` - бизнес-логика и работа с базой данных (dependencies: `data`).
- `data` - доступ к данным о ресторанах (dependencies: `integration`).
- `integration` - работа с TripAdvisor API (dependencies: null).

## Настройка баз данных

### Миграции

Для миграций используется плагин [*Flyway*](https://documentation.red-gate.com/fd/getting-started-with-flyway-184127223.html). 

Перед запуском миграций необходимо вручную создать базу данных:
- **Основная база данных**

Далее создайте собственный файл `.env`, указав в нём параметры подключения к базе данных (см. пример в `.env.example`).

После этого можно запустить миграцию для базы данных:
```pwsh
./gradlew :logic:flywayMigrate
```

Информацию о текущем состоянии миграций можно получить с помощью задачи `flywayInfo`.

### Генерация кода

Для генерации кода используется плагин [*jOOQ*](https://github.com/etiennestuder/gradle-jooq-plugin).

После выполнения миграции можно сгенерировать Java-классы из схемы базы данных:
```pwsh
./gradlew :logic:generateJooq
```

Сгенерированные классы будут размещены в следующих директориях:
- `logic/build/generated-sources/jooq`


### Обновление мигргаций

Если требуется обновить базу данных (на примере модуля `logic`):

```bash
psql -U postgres
```

```sql
-- Завершить все активные соединения с базой данных
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'main'
  AND pid <> pg_backend_pid();

-- Удалить базу данных, если она существует
DROP DATABASE IF EXISTS main;

-- Создать новую базу данных
CREATE DATABASE main;
```

```bash
./gradlew :logic:flywayMigrate
./gradlew :logic:flywayInfo
```

## Сборка

Для сборки в Fat Jar используется плагин [*shadow*](https://gradleup.com/shadow/).

```bash
./gradlew build
```

В корне проекта есть `build.gradle` файл, который содержит общие настройки сборки для всех подпроектов. Также в каждом подпроекте есть свой `build.gradle` файл, который содержит настройки для данного подпроекта.

## Запуск

Запуск из Fat Jar:
```bash
java -jar ./app/build/libs/app-fat.jar
```

Запуск из исходников:
```bash
./gradlew :app:run
```


## Зависимости

### Внешние зависимости

Все версии внешних зависимостей указаны в `./gradle/libs.versions.toml`. В `build.gradle` файлах указываются только названия зависимостей.

Пример:
```groovy
dependencies {
    implementation libs.guava
}
```

### Внутренние зависимости

Внутренние зависимости указываются в `build.gradle` файлах каждого подпроекта. 

Пример:
```groovy
dependencies {
    // Подключаем подпроект logic
    implementation project(':logic')
}
```

### Общие зависимости

Общие зависимости указываются в `build.gradle` файле корневого проекта.

## Форматирование кода

Для форматирования кода используется gradle плагин [*spotless*](https://github.com/diffplug/spotless).

```bash
./gradlew spotlessApply
```

## Docker

### Запуск в Docker

При первом запуске:
1. Создать `.env` файл в корне проекта.
2. Запустить PostgreSQL
   ```bash
   docker compose up -d postgres
   ```
3. Выполнить миграции
   ```bash
   psql -h localhost -U postgres -p 5435 -d main -f logic/src/main/resources/db/migration/main/V1__init_main.sql
   psql -h localhost -U postgres -p 5435 -d main -f logic/src/main/resources/db/migration/main/V2__add_data.sql
   ```
4. Запустить приложение вместе с базой данных.
   ```bash
   docker compose up -d
   ```

При повторных запусках достаточно выполнить команду:
```bash
docker compose up -d
```

### Сборка образа

При запуске Docker Compose подтягивается образ `restobot-app:latest` из Docker Hub. Однако, если вы хотите собрать образ самостоятельно, вы можете воспользоваться следующими командами:

Требования:
- Java 23+
- gradle 8.10+

1. Создать `.env` файл в корне проекта.
2. Запустить PostgreSQL
   ```bash
   docker compose up -d postgres
   ```
3. Выполнить миграции
   ```bash
   ./gradlew :logic:flywayMigrate
   ```
4. Собрать проект
   ```bash
   ./gradlew build
   ```
5. Собрать образ
   ```bash
   docker build -t restobot-app .
   ```

Готово! Приложение собрано в образе `restobot-app:latest`.

Загрузить образ в Docker Hub (имя пользователя `thearity` можно заменить на своё):
```bash
docker login -u thearity
docker tag restobot-app:latest thearity/restobot-app:latest
docker push thearity/restobot-app:latest
```

## Авторы
- Артем Тищенко
- Владислав Гаар
- Губкоский Дмитрий


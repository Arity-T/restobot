# RestoBot

## Структура проекта

Проект представляет собой [*gradle multi-project*](https://docs.gradle.org/current/userguide/multi_project_builds.html). Каждый подпроект содержит одноименный Java пакет с префиксом `dev.tishenko.restobot`.

TODO: нужно будет решить, что в итоге будет главной точкой входа.

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

Перед запуском миграций необходимо вручную создать две базы данных:
- **Основная база данных**
- **База данных кэшированной информации**

Далее создайте собственный файл `.env`, указав в нём параметры подключения к обеим базам данных (см. пример в `.env.example`).

После этого можно запустить миграции для каждой базы:
```pwsh
./gradlew :logic:flywayMigrate
./gradlew :data:flywayMigrate
```

Информацию о текущем состоянии миграций можно получить с помощью задачи `flywayInfo`.

### Генерация кода

Для генерации кода используется плагин [*jOOQ*](https://github.com/etiennestuder/gradle-jooq-plugin).

После выполнения миграций можно сгенерировать Java-классы из схемы базы данных:
```pwsh
./gradlew :logic:generateJooq
./gradlew :data:generateJooq
```

Сгенерированные классы будут размещены в следующих директориях:
- `logic/build/generated-sources/jooq`
- `data/build/generated-sources/jooq`


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
java -jar ./telegram/build/libs/telegram-app.jar
```

Запуск из исходников:
```bash
./gradlew :telegram:run
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
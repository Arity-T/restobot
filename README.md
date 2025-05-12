# RestoBot

## Структура проекта

Проект представляет собой [*gradle multi-project*](https://docs.gradle.org/current/userguide/multi_project_builds.html). Каждый подпроект содержит одноименный Java пакет с префиксом `dev.tishenko.restobot`.

Java applications:
- `telegram` - telegram бот (dependencies: `logic`).
- `api` - обработка запросов на наш API (dependencies: `logic`).
- `cache` - обновление кэша (dependencies: `integration`)

Java libraries:
- `logic` - бизнес-логика и работа с базой данных (dependencies: `data`).
- `data` - доступ к данным о ресторанах (dependencies: `integration`).
- `integration` - работа с TripAdvisor API (dependencies: null).


## Сборка

Для сборки в Fat Jar используется плагин [*shadow*](https://gradleup.com/shadow/).

```bash
./gradlew build

# или по отдельности
./gradlew :telegram:shadowJar
# или
./gradlew :api:shadowJar
# или
./gradlew :cache:shadowJar
```

В корне проекта есть `build.gradle` файл, который содержит общие настройки сборки для всех подпроектов. Также в каждом подпроекте есть свой `build.gradle` файл, который содержит настройки для данного подпроекта.

## Запуск

Запуск из Fat Jar:
```bash
java -jar ./telegram/build/libs/telegram-app.jar
# или
java -jar ./api/build/libs/api-app.jar
# или
java -jar ./cache/build/libs/cache-app.jar
```

Запуск из исходников:
```bash
./gradlew :telegram:run
# или
./gradlew :api:run
# или
./gradlew :cache:run
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
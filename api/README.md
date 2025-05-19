# Библиотека Restobot API

## Генерация документации

```bash
./gradlew clean test
./gradlew asciidoctor
```

Файл `index.html` будет находиться в папке `build/docs/asciidoc`.

## Использование

### Реализация сервисов

Реализуем следующие сервисы. Они должны быть доступны в контексте Spring, чтобы библиотека могла их использовать.

```java
// Реализация ApiKeyValidator
@Service
public class YourApiKeyValidator implements ApiKeyValidator {
    @Override
    public boolean isValidApiKey(String apiKey) {
        // Ваша реализация
    }
}

// Реализация TripadvisorTracker
@Service
public class YourTripadvisorTracker implements TripadvisorTracker {
    @Override
    public LocalDateTime getLastCallTime() {
        // Ваша реализация
    }
    
    @Override
    public void updateLastCallTime() {
        // Ваша реализация
    }
}

// Реализация HealthStatusProvider
@Service
public class YourHealthStatusProvider implements HealthStatusProvider {
    @Override
    public Map<String, Object> getHealthStatus() {
        // Ваша реализация
    }
}

// Реализация UserService
@Service
public class YourUserService implements UserService {
    @Override
    public List<Map<String, String>> getUsers() {
        // Ваша реализация
    }
}
```

### Настройка конфигурации

Импортируем `RestobotApiConfig`. Чтобы в дальнейшем запустить http сервер, необходимо определить бин `webHandler`. Без него `WebHttpHandlerBuilder` работать не будет.

```java
@Configuration
@Import(RestobotApiConfig.class)
public class YourAppConfig {
    // ваша конфигурация

    @Bean
    public WebHandler webHandler(RouterFunction<ServerResponse> restobotRoutes) {
        return RouterFunctions.toWebHandler(restobotRoutes);
    }
}
```

### Настройка сервера

Библиотека не запускает сервер самостоятельно.

```java
// Создаем контекст Spring с вашей конфигурацией
AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(YourAppConfig.class);

// Создаем HTTP обработчик из контекста Spring
var httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
var adapter = new ReactorHttpHandlerAdapter(httpHandler);

// Создаем и запускаем HTTP сервер
HttpServer.create()
        .host("localhost")
        .port(8080)
        .handle(adapter)
        .bindNow()
        .onDispose()
        .block();
```

## Пример реализации

В пакете `dev.tishenko.restobot.api.example` лежит полноценный пример. Можно запустить `AppExample.java` и посмотреть, как всё работает.

```bash
curl -X GET http://localhost:8089/healthcheck
curl -X GET http://localhost:8089/users -H "X-API-KEY: my-secure-api-key"
```


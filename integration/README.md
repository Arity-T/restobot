# TripAdvisor API Integration Library

## Эндпоинты

- [Location Details](https://tripadvisor-content-api.readme.io/reference/getlocationdetails)
- [Location Reviews](https://tripadvisor-content-api.readme.io/reference/getlocationreviews)
- [Location Search](https://tripadvisor-content-api.readme.io/reference/searchforlocations)
- [Location Nearby Search](https://tripadvisor-content-api.readme.io/reference/searchfornearbylocations)

## Настройка

В окружении Spring приложения должны быть заданы следующие properties:

- `tripadvisor.api.host` - URL-адрес API
- `tripadvisor.api.key` - API-ключ
- `tripadvisor.api.language` - язык ответов

## Использование

Добавьте следующую конфигурацию в приложение Spring:

```java
@Configuration
@Import(TripAdvisorConfiguration.class)
public class AppConfig {
    // Your application configuration
}
```

Затем можно использовать бин `TripAdvisorClient`:

```java
@Service
public class LocationService {
    private final TripAdvisorClient tripAdvisorClient;
    
    @Autowired
    public LocationService(TripAdvisorClient tripAdvisorClient) {
        this.tripAdvisorClient = tripAdvisorClient;
    }
    
    public Mono<List<String>> getTopRestaurantsInCity(String city) {
        return tripAdvisorClient.searchLocations(city + " restaurants", "restaurants")
            .map(response -> response.getData().stream()
                .map(LocationSearchResponse.LocationSearchResult::getName)
                .collect(Collectors.toList()));
    }
}
```

## Обработка ошибок

Библиотека предоставляет пользовательский класс исключений `TripAdvisorApiException` для обработки ошибок API. Все методы в `TripAdvisorClient` автоматически преобразуют исключения WebClient в этот тип пользовательского исключения.

```java
client.getLocationDetails("invalid-id")
    .doOnError(TripAdvisorApiException.class, e -> {
        System.err.println("API Error: " + e.getMessage());
        System.err.println("Status Code: " + e.getStatusCode());
    })
    .onErrorResume(e -> Mono.empty())
    .subscribe(details -> System.out.println("Location: " + details.getName()));
```
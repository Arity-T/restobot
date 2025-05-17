# RestoBot Telegram Module

This module contains the Telegram bot implementation for the RestoBot application. The bot provides an interface for users to interact with the restaurant search functionality.

## Architecture

The module is designed as a Spring library that can be integrated into any Spring application. The architecture follows these principles:

1. **Spring Integration**: Seamless integration with Spring applications
2. **Service Interfaces**: Clearly defined interfaces for all required services
3. **Configuration**: Simple Spring-based configuration
4. **Examples**: Example implementations for testing and development

### Key Components

- `RestoBot`: The main bot class that handles Telegram API interactions
- `BotConfig`: Configuration class that holds all dependencies 
- `RestoBotUserHandler`: Handles the bot's state machine and user interactions
- `UserData`: Stores user data and state
- `Service Interfaces`: Define the contract for implementations

## Usage

1. Import `BotFactoryConfig` in your Spring configuration:

```java
@Configuration
@Import(BotFactoryConfig.class)
public class MyAppConfig {
    // Your app configuration
}
```

2. Implement the required service interfaces and register them as Spring beans:
   - `FavoriteListDAO`
   - `RestaurantCardFinder`
   - `UserDAO`
   - `UserParamsValidator`
   - `SearchParametersService`

3. Set environment variables or properties:
   - `TELEGRAM_BOT_TOKEN`
   - `TELEGRAM_BOT_USERNAME`

4. Get the `RestoBot` bean and start it:

```java
@Autowired
private RestoBot restoBot;

public void startBot() {
    restoBot.start();
}
```

## Example

The module includes example implementations in the `dev.tishenko.restobot.telegram.example` package:

- `AppExample`: Shows how to start the bot with Spring
- `impl`: Contains example implementations of all service interfaces

## Running the Example

```bash
./gradlew :telegram:run
```

This will start the example bot using the implementations in the `example` package. 
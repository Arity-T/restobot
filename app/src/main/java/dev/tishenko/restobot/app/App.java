package dev.tishenko.restobot.app;

import dev.tishenko.restobot.logic.example.config.AppConfig;
import dev.tishenko.restobot.telegram.BotFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Создаем контекст Spring
        var context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Получаем Environment для чтения properties
        Environment env = context.getEnvironment();
        String host = env.getProperty("api.server.host");
        int port = Integer.parseInt(env.getProperty("api.server.port"));
        logger.info("Starting API server at {}:{}", host, port);

        // Запускаем API сервер в отдельном потоке
        var apiThread =
                new Thread(
                        () -> {
                            var httpHandler =
                                    WebHttpHandlerBuilder.applicationContext(context).build();
                            var adapter = new ReactorHttpHandlerAdapter(httpHandler);

                            HttpServer.create()
                                    .host(host)
                                    .port(port)
                                    .handle(adapter)
                                    .bindNow()
                                    .onDispose()
                                    .block();
                        });
        apiThread.start();

        // Запускаем Telegram бота в основном потоке
        BotFacade telegramApp = context.getBean(BotFacade.class);
        // Бот запускается автоматически через LongPollingUpdateConsumer
        telegramApp.start();
    }
}

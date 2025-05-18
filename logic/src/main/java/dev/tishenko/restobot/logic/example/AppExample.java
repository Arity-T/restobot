package dev.tishenko.restobot.logic.example;

import dev.tishenko.restobot.logic.example.config.AppConfig;
import dev.tishenko.restobot.telegram.BotFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public class AppExample {
    public static void main(String[] args) {
        // Создаем контекст Spring
        var context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Запускаем API сервер в отдельном потоке
        var apiThread =
                new Thread(
                        () -> {
                            var httpHandler =
                                    WebHttpHandlerBuilder.applicationContext(context).build();
                            var adapter = new ReactorHttpHandlerAdapter(httpHandler);

                            HttpServer.create()
                                    .host("localhost")
                                    .port(8089)
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

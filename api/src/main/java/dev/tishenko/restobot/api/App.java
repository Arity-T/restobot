package dev.tishenko.restobot.api;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ApiConfig.class);

        var httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
        var adapter = new ReactorHttpHandlerAdapter(httpHandler);

        HttpServer.create()
                .host("localhost")
                .port(8089)
                .handle(adapter)
                .bindNow()
                .onDispose()
                .block();
    }
}

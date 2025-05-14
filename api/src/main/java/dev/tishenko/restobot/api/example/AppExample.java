package dev.tishenko.restobot.api.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

/**
 * Example application showing how to use the Restobot API library. This class demonstrates how to
 * set up a server with the Restobot API endpoints.
 */
public class AppExample {
    public static void main(String[] args) {
        // Create Spring context with our API configuration
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfigExample.class);

        // Create HTTP handler from the Spring context
        var httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
        var adapter = new ReactorHttpHandlerAdapter(httpHandler);

        // Create and start HTTP server
        HttpServer.create()
                .host("localhost")
                .port(8089)
                .handle(adapter)
                .bindNow()
                .onDispose()
                .block();
    }
}

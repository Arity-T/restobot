package dev.tishenko.restobot.api;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import dev.tishenko.restobot.api.service.ApiHealthStatusProvider;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(RestDocumentationExtension.class)
public class HealthCheckHandlerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        // Create simple implementation of ApiHealthStatusProvider
        ApiHealthStatusProvider healthStatusProvider =
                new ApiHealthStatusProvider() {
                    @Override
                    public Map<String, Object> getHealthStatus() {
                        Map<String, Object> status = new HashMap<>();
                        status.put("status", "OK");
                        status.put("lastTripAdvisorCallTime", LocalDateTime.now().toString());
                        status.put(
                                "authors",
                                List.of("Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"));
                        return status;
                    }
                };

        // Create handler and router
        HealthCheckHandler healthCheckHandler = new HealthCheckHandler(healthStatusProvider);
        RouterFunction<ServerResponse> routerFunction =
                RouterFunctions.route(GET("/healthcheck"), healthCheckHandler::healthcheck);

        // Configure web test client
        this.webTestClient =
                WebTestClient.bindToRouterFunction(routerFunction)
                        .configureClient()
                        .filter(
                                org.springframework.restdocs.webtestclient
                                        .WebTestClientRestDocumentation.documentationConfiguration(
                                        restDocumentation))
                        .build();
    }

    @Test
    public void testHealthCheck() {
        // Execute test
        webTestClient
                .get()
                .uri("/healthcheck")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(
                        document(
                                "healthcheck",
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("status")
                                                .description("Current status of the application"),
                                        fieldWithPath("lastTripAdvisorCallTime")
                                                .description(
                                                        "Timestamp of the last TripAdvisor API call"),
                                        fieldWithPath("authors")
                                                .description("List of application authors"))));
    }
}

package dev.tishenko.restobot.api;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.api.service.ApiUserService;
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
public class UserHandlerTest {

    private WebTestClient webTestClient;
    private WebTestClient webTestClientInvalidKey;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        // Create simple implementations of services
        ApiKeyValidator validKeyValidator =
                new ApiKeyValidator() {
                    @Override
                    public boolean isValidApiKey(String apiKey) {
                        return "test-api-key".equals(apiKey);
                    }
                };

        ApiUserService userService =
                new ApiUserService() {
                    @Override
                    public List<Map<String, String>> getUsers() {
                        return List.of(
                                Map.of("id", "1", "username", "@user1"),
                                Map.of("id", "2", "username", "@user2"),
                                Map.of("id", "3", "username", "@example_user"));
                    }
                };

        // Create handler and router for valid key tests
        UserHandler userHandler = new UserHandler(validKeyValidator, userService);
        RouterFunction<ServerResponse> routerFunction =
                RouterFunctions.route(GET("/users"), userHandler::getUsers);

        // Create handler and router for invalid key tests
        ApiKeyValidator invalidKeyValidator =
                new ApiKeyValidator() {
                    @Override
                    public boolean isValidApiKey(String apiKey) {
                        return false; // Always invalid
                    }
                };
        UserHandler invalidKeyHandler = new UserHandler(invalidKeyValidator, userService);
        RouterFunction<ServerResponse> invalidKeyRouterFunction =
                RouterFunctions.route(GET("/users"), invalidKeyHandler::getUsers);

        // Configure web test clients
        this.webTestClient =
                WebTestClient.bindToRouterFunction(routerFunction)
                        .configureClient()
                        .filter(
                                org.springframework.restdocs.webtestclient
                                        .WebTestClientRestDocumentation.documentationConfiguration(
                                        restDocumentation))
                        .build();

        this.webTestClientInvalidKey =
                WebTestClient.bindToRouterFunction(invalidKeyRouterFunction)
                        .configureClient()
                        .filter(
                                org.springframework.restdocs.webtestclient
                                        .WebTestClientRestDocumentation.documentationConfiguration(
                                        restDocumentation))
                        .build();
    }

    @Test
    public void testGetUsersWithValidApiKey() {
        // Execute test
        webTestClient
                .get()
                .uri("/users")
                .header("X-API-KEY", "test-api-key")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(
                        document(
                                "users-success",
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-API-KEY")
                                                .description("API key for authentication")),
                                responseFields(
                                        fieldWithPath("[].id")
                                                .description("Unique user identifier"),
                                        fieldWithPath("[].username")
                                                .description("User's username"))));
    }

    @Test
    public void testGetUsersWithInvalidApiKey() {
        // Execute test
        webTestClientInvalidKey
                .get()
                .uri("/users")
                .header("X-API-KEY", "invalid-api-key")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(
                        document(
                                "users-unauthorized",
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-API-KEY").description("Invalid API key")),
                                responseFields(
                                        fieldWithPath("error").description("Error message"))));
    }
}

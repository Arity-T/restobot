package dev.tishenko.restobot.api.example.impl;

import dev.tishenko.restobot.api.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Example implementation of UserService. */
@Service
public class ExampleUserService implements UserService {

    @Override
    public List<Map<String, String>> getUsers() {
        return List.of(
                Map.of("id", "1", "username", "@user1"),
                Map.of("id", "2", "username", "@user2"),
                Map.of("id", "3", "username", "@example_user"));
    }
}

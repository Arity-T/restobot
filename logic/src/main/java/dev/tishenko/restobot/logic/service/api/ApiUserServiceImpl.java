package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiUserService;
import dev.tishenko.restobot.logic.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ApiUserServiceImpl implements ApiUserService {

    private final UserRepository userRepository;

    public ApiUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Map<String, String>> getUsers() {
        return userRepository.findAll().stream()
                .map(
                        user ->
                                Map.of(
                                        "chatId", String.valueOf(user.getChatId()),
                                        "nickname", user.getNickname()))
                .collect(Collectors.toList());
    }
}

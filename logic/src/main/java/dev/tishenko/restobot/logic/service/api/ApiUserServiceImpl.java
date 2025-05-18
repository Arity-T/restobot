package dev.tishenko.restobot.logic.service.api;

import dev.tishenko.restobot.api.service.ApiUserService;
import dev.tishenko.restobot.logic.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiUserServiceImpl implements ApiUserService {

    private static final Logger logger = LoggerFactory.getLogger(ApiUserServiceImpl.class);
    private final UserRepository userRepository;

    public ApiUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("ApiUserServiceImpl initialized");
    }

    @Override
    public List<Map<String, String>> getUsers() {
        logger.debug("Fetching all users for API");
        List<Map<String, String>> users =
                userRepository.findAll().stream()
                        .map(
                                user ->
                                        Map.of(
                                                "chatId", String.valueOf(user.getChatId()),
                                                "nickname", user.getNickname()))
                        .collect(Collectors.toList());
        logger.debug("Fetched {} users", users.size());
        return users;
    }
}

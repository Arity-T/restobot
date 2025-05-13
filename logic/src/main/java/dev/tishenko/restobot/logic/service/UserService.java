package dev.tishenko.restobot.logic.service;

import java.util.Optional;

import org.example.jooq.generated.tables.records.UsersRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.logic.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository repo) {
        this.userRepository = repo;
    }

    public void registerUser(long chatId, String nickname) {
        userRepository.saveUser(chatId, nickname);
    }

    public Optional<UsersRecord> getUserById(long chatId) {
        return Optional.ofNullable(userRepository.findByChatId(chatId));
    }

    public void assignFavoriteList(long chatId, int favoriteListId) {
      userRepository.updateFavoriteListId(chatId, favoriteListId);
    }
}

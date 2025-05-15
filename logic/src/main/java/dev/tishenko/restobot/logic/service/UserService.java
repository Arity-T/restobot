package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.UserRepository;
import dev.tishenko.restobot.telegram.services.UserDAO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.UsersRecord;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDAO {

    private final UserRepository userRepository;

    public UserService(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public void addUserToDB(UserDTO userDTO) {
        userRepository.saveUser(userDTO.chatID(), userDTO.nickName());
    }

    @Override
    public Optional<UserDTO> getUserFromDB(int chatId) {
        UsersRecord record = userRepository.findByChatId(chatId);
        if (record == null) {
            return Optional.empty();
        }

        // Convert the database record to UserDTO
        // Split keywords string into List if not null, otherwise empty list
        List<String> keywords =
                record.getKeywords() != null
                        ? Arrays.asList(record.getKeywords().split(","))
                        : List.of();

        return Optional.of(
                new UserDTO(
                        record.getChatId(),
                        record.getNickname(),
                        record.getCityId() != null ? record.getCityId().toString() : "",
                        List.of(), // Kitchen types - needs implementation
                        List.of(), // Price categories - needs implementation
                        keywords,
                        List.of() // Favorite list - needs implementation
                        ));
    }

    @Override
    public void setNewUserCity(int chatId, String city) {
        try {
            int cityId = Integer.parseInt(city);
            userRepository.updateCity(chatId, cityId);
        } catch (NumberFormatException e) {
            // TODO: Implement proper city name to ID conversion
            // For now, just storing 0 as default
            userRepository.updateCity(chatId, 0);
        }
    }

    @Override
    public void setNewUserKitchenTypes(int chatId, List<String> kitchenTypes) {
        // TODO: Implement kitchen types storage
        // This might require a separate table and mapping logic
    }

    @Override
    public void setNewUserPriceCategories(int chatId, List<String> priceCategories) {
        // TODO: Implement price categories storage
        // This might require a separate table and mapping logic
    }

    @Override
    public void setNewUserKeyWords(int chatId, List<String> keyWords) {
        String keywordsString = String.join(",", keyWords);
        userRepository.updateKeywords(chatId, keywordsString);
    }
}

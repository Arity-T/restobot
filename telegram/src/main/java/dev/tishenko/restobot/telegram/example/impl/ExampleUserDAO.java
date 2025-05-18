package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.UserDAO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleUserDAO implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExampleUserDAO.class);

    @Override
    public void addUserToDB(UserDTO userDTO) {
        logger.info("Adding user to DB: {}", userDTO);
    }

    @Override
    public Optional<UserDTO> getUserFromDB(long chatId) {
        logger.info("Getting user from DB: chatId={}", chatId);
        return Optional.empty();
    }

    @Override
    public void setNewUserCity(long chatId, String city) {
        logger.info("Setting new user city: chatId={}, city={}", chatId, city);
    }

    @Override
    public void setNewUserKitchenTypes(long chatId, List<String> kitchenTypes) {
        logger.info(
                "Setting new user kitchen types: chatId={}, kitchenTypes={}", chatId, kitchenTypes);
    }

    @Override
    public void setNewUserPriceCategories(long chatId, List<String> priceCategories) {
        logger.info(
                "Setting new user price categories: chatId={}, priceCategories={}",
                chatId,
                priceCategories);
    }

    @Override
    public void setNewUserKeyWords(long chatId, List<String> keyWords) {
        logger.info("Setting new user key words: chatId={}, keyWords={}", chatId, keyWords);
    }
}

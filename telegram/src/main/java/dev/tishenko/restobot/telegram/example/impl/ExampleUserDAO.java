package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.UserDAO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExampleUserDAO implements UserDAO {


    @Override
    public void addUserToDB(UserDTO userDTO) {

    }

    @Override
    public Optional<UserDTO> getUserFromDB(long chatId) {
        return Optional.empty();
    }

    @Override
    public void setNewUserCity(long chatId, String city) {

    }

    @Override
    public void setNewUserKitchenTypes(long chatId, List<String> kitchenTypes) {

    }

    @Override
    public void setNewUserPriceCategories(long chatId, List<String> priceCategories) {

    }

    @Override
    public void setNewUserKeyWords(long chatId, List<String> keyWords) {

    }
}

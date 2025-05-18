package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.logic.repository.UserRepository;
import dev.tishenko.restobot.logic.service.LogicCityService;
import dev.tishenko.restobot.logic.service.LogicKitchenTypeService;
import dev.tishenko.restobot.logic.service.LogicPriceCategoryService;
import dev.tishenko.restobot.logic.service.UserKitchenTypeService;
import dev.tishenko.restobot.logic.service.UserPriceCategoryService;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.UserDAO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.jooq.generated.tables.records.UsersRecord;
import org.springframework.stereotype.Service;

@Service
public class UserDAOImpl implements UserDAO {

    private final UserRepository userRepository;
    private final UserKitchenTypeService userKitchenTypeService;
    private final UserPriceCategoryService userPriceCategoryService;
    private final LogicCityService cityService;
    private final FavoriteListDAOImpl favoriteRestaurantService;
    private final LogicKitchenTypeService kitchenTypeService;
    private final LogicPriceCategoryService priceCategoryService;

    public UserDAOImpl(
            UserRepository userRepository,
            UserKitchenTypeService userKitchenTypeService,
            UserPriceCategoryService userPriceCategoryService,
            LogicCityService cityService,
            FavoriteListDAOImpl favoriteRestaurantService,
            LogicKitchenTypeService kitchenTypeService,
            LogicPriceCategoryService priceCategoryService) {
        this.userRepository = userRepository;
        this.userKitchenTypeService = userKitchenTypeService;
        this.userPriceCategoryService = userPriceCategoryService;
        this.cityService = cityService;
        this.favoriteRestaurantService = favoriteRestaurantService;
        this.kitchenTypeService = kitchenTypeService;
        this.priceCategoryService = priceCategoryService;
    }

    @Override
    public void addUserToDB(UserDTO userDTO) {
        // Сохраняем базовую информацию о пользователе
        userRepository.saveUser(userDTO.chatID(), userDTO.nickName());

        // Сохраняем город, если он указан
        if (!userDTO.city().isEmpty()) {
            setNewUserCity(userDTO.chatID(), userDTO.city());
        }

        // Сохраняем типы кухни
        if (!userDTO.kitchenTypes().isEmpty()) {
            setNewUserKitchenTypes(userDTO.chatID(), userDTO.kitchenTypes());
        }

        // Сохраняем ценовые категории
        if (!userDTO.priceCategories().isEmpty()) {
            setNewUserPriceCategories(userDTO.chatID(), userDTO.priceCategories());
        }

        // Сохраняем ключевые слова
        if (!userDTO.keyWords().isEmpty()) {
            setNewUserKeyWords(userDTO.chatID(), userDTO.keyWords());
        }

        // Сохраняем избранные рестораны
        if (!userDTO.favoriteList().isEmpty()) {
            userDTO.favoriteList()
                    .forEach(
                            restaurant ->
                                    favoriteRestaurantService.addRestaurantCardToFavoriteList(
                                            userDTO.chatID(),
                                            restaurant.restaurantCardDTO().tripadvisorId()));
        }
    }

    @Override
    public Optional<UserDTO> getUserFromDB(long chatId) {
        UsersRecord record = userRepository.findByChatId(chatId);
        if (record == null) {
            return Optional.empty();
        }

        // Получаем типы кухни пользователя
        List<String> kitchenTypes =
                userKitchenTypeService.getAllForUser(chatId).stream()
                        .map(k -> k.getKitchenTypeId().toString())
                        .collect(Collectors.toList());

        // Получаем ценовые категории пользователя
        List<String> priceCategories =
                userPriceCategoryService.getAllForUser(chatId).stream()
                        .map(p -> p.getPriceCategoryId().toString())
                        .collect(Collectors.toList());

        // Получаем ключевые слова
        List<String> keywords =
                record.getKeywords() != null ? List.of(record.getKeywords().split(",")) : List.of();

        // Получаем избранные рестораны
        List<FavoriteRestaurantCardDTO> favoriteList =
                favoriteRestaurantService.getFavoriteList(chatId);

        return Optional.of(
                new UserDTO(
                        record.getChatId(),
                        record.getNickname(),
                        record.getCityId() != null ? record.getCityId().toString() : "",
                        kitchenTypes,
                        priceCategories,
                        keywords,
                        favoriteList));
    }

    @Override
    public void setNewUserCity(long chatId, String city) {
        try {
            int cityId = Integer.parseInt(city);
            userRepository.updateCity(chatId, cityId);
        } catch (NumberFormatException e) {
            // Если передан текст города, ищем его ID
            cityService
                    .getCityByName(city)
                    .ifPresent(
                            cityRecord ->
                                    userRepository.updateCity(chatId, cityRecord.getCityId()));
        }
    }

    @Override
    public void setNewUserKitchenTypes(long chatId, List<String> kitchenTypes) {
        // Сначала удаляем все существующие типы кухни
        userKitchenTypeService.removeAllByUser(chatId);

        // Добавляем новые типы кухни
        kitchenTypes.forEach(
                kitchenType -> {
                    try {
                        int kitchenTypeId = Integer.parseInt(kitchenType);
                        userKitchenTypeService.addKitchen(chatId, kitchenTypeId);
                    } catch (NumberFormatException e) {
                        // Если передан текст типа кухни, ищем его ID
                        kitchenTypeService
                                .getByName(kitchenType)
                                .ifPresent(
                                        k ->
                                                userKitchenTypeService.addKitchen(
                                                        chatId, k.getKitchenTypeId()));
                    }
                });
    }

    @Override
    public void setNewUserPriceCategories(long chatId, List<String> priceCategories) {
        // Сначала удаляем все существующие ценовые категории
        userPriceCategoryService.removeAllByUser(chatId);

        // Добавляем новые ценовые категории
        priceCategories.forEach(
                priceCategory -> {
                    try {
                        int priceCategoryId = Integer.parseInt(priceCategory);
                        userPriceCategoryService.addPriceCategory(chatId, priceCategoryId);
                    } catch (NumberFormatException e) {
                        // Если передан текст ценовой категории, ищем её ID
                        priceCategoryService
                                .getByName(priceCategory)
                                .ifPresent(
                                        p ->
                                                userPriceCategoryService.addPriceCategory(
                                                        chatId, p.getPriceCategoryId()));
                    }
                });
    }

    @Override
    public void setNewUserKeyWords(long chatId, List<String> keyWords) {
        String keywordsString = String.join(",", keyWords);
        userRepository.updateKeywords(chatId, keywordsString);
    }
}

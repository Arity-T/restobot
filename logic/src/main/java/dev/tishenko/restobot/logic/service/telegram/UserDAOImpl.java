package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.UsersRecord;
import dev.tishenko.restobot.logic.repository.UserRepository;
import dev.tishenko.restobot.logic.service.CityService;
import dev.tishenko.restobot.logic.service.KitchenTypeService;
import dev.tishenko.restobot.logic.service.PriceCategoryService;
import dev.tishenko.restobot.logic.service.UserKitchenTypeService;
import dev.tishenko.restobot.logic.service.UserPriceCategoryService;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.UserDAO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
    private final UserRepository userRepository;
    private final UserKitchenTypeService userKitchenTypeService;
    private final UserPriceCategoryService userPriceCategoryService;
    private final CityService cityService;
    private final FavoriteListDAOImpl favoriteRestaurantService;
    private final KitchenTypeService kitchenTypeService;
    private final PriceCategoryService priceCategoryService;

    public UserDAOImpl(
            UserRepository userRepository,
            UserKitchenTypeService userKitchenTypeService,
            UserPriceCategoryService userPriceCategoryService,
            CityService cityService,
            FavoriteListDAOImpl favoriteRestaurantService,
            KitchenTypeService kitchenTypeService,
            PriceCategoryService priceCategoryService) {
        this.userRepository = userRepository;
        this.userKitchenTypeService = userKitchenTypeService;
        this.userPriceCategoryService = userPriceCategoryService;
        this.cityService = cityService;
        this.favoriteRestaurantService = favoriteRestaurantService;
        this.kitchenTypeService = kitchenTypeService;
        this.priceCategoryService = priceCategoryService;
        logger.info("UserDAOImpl initialized");
    }

    @Override
    public void addUserToDB(UserDTO userDTO) {
        logger.debug("Adding user to DB with chatID: {}", userDTO.chatID());
        // Сохраняем базовую информацию о пользователе
        userRepository.saveUser(userDTO.chatID(), userDTO.nickName());

        // Сохраняем город, если он указан
        if (userDTO.city() != null && !userDTO.city().isEmpty()) {
            logger.debug("Setting city for user {}: {}", userDTO.chatID(), userDTO.city());
            setNewUserCity(userDTO.chatID(), userDTO.city());
        }

        // Сохраняем типы кухни
        if (userDTO.kitchenTypes() != null && !userDTO.kitchenTypes().isEmpty()) {
            logger.debug(
                    "Setting kitchen types for user {}: {}",
                    userDTO.chatID(),
                    userDTO.kitchenTypes());
            setNewUserKitchenTypes(userDTO.chatID(), userDTO.kitchenTypes());
        }

        // Сохраняем ценовые категории
        if (userDTO.priceCategories() != null && !userDTO.priceCategories().isEmpty()) {
            logger.debug(
                    "Setting price categories for user {}: {}",
                    userDTO.chatID(),
                    userDTO.priceCategories());
            setNewUserPriceCategories(userDTO.chatID(), userDTO.priceCategories());
        }

        // Сохраняем ключевые слова
        if (userDTO.keyWords() != null && !userDTO.keyWords().isEmpty()) {
            logger.debug("Setting keywords for user {}: {}", userDTO.chatID(), userDTO.keyWords());
            setNewUserKeyWords(userDTO.chatID(), userDTO.keyWords());
        }

        // Сохраняем избранные рестораны
        if (userDTO.favoriteList() != null && !userDTO.favoriteList().isEmpty()) {
            logger.debug(
                    "Setting favorite restaurants for user {}: count {}",
                    userDTO.chatID(),
                    userDTO.favoriteList().size());
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
        logger.debug("Getting user from DB with chatID: {}", chatId);
        UsersRecord record = userRepository.findByChatId(chatId);
        if (record == null) {
            logger.debug("User not found with chatID: {}", chatId);
            return Optional.empty();
        }

        // Получаем город пользователя
        String city =
                record.getCityId() != null
                        ? cityService
                                .getCityById(record.getCityId())
                                .map(c -> c.getName())
                                .orElse("")
                        : "";
        logger.debug("Found city for user {}: {}", chatId, city);

        // Получаем типы кухни пользователя
        List<String> kitchenTypes =
                userKitchenTypeService.getAllForUser(chatId).stream()
                        .map(
                                k ->
                                        kitchenTypeService
                                                .getById(k.getKitchenTypeId())
                                                .map(kt -> kt.getName())
                                                .orElse(""))
                        .collect(Collectors.toList());
        logger.debug("Found kitchen types for user {}: {}", chatId, kitchenTypes);

        // Получаем ценовые категории пользователя
        List<String> priceCategories =
                userPriceCategoryService.getAllForUser(chatId).stream()
                        .map(
                                p ->
                                        priceCategoryService
                                                .getById(p.getPriceCategoryId())
                                                .map(pc -> pc.getName())
                                                .orElse(""))
                        .collect(Collectors.toList());
        logger.debug("Found price categories for user {}: {}", chatId, priceCategories);

        // Получаем ключевые слова
        List<String> keywords =
                record.getKeywords() != null ? List.of(record.getKeywords().split(",")) : List.of();
        logger.debug("Found keywords for user {}: {}", chatId, keywords);

        // Получаем избранные рестораны
        List<FavoriteRestaurantCardDTO> favoriteList =
                favoriteRestaurantService.getFavoriteList(chatId);
        logger.debug("Found {} favorite restaurants for user {}", favoriteList.size(), chatId);

        return Optional.of(
                new UserDTO(
                        record.getChatId(),
                        record.getNickname(),
                        city,
                        kitchenTypes,
                        priceCategories,
                        keywords,
                        favoriteList,
                        record.getState()));
    }

    @Override
    public void setNewUserCity(long chatId, String city) {
        logger.debug("Setting new city for user {}: {}", chatId, city);
        try {
            int cityId = Integer.parseInt(city);
            logger.debug("Setting city by ID: {}", cityId);
            userRepository.updateCity(chatId, cityId);
        } catch (NumberFormatException e) {
            // Если передан текст города, ищем его ID
            logger.debug("Setting city by name: {}", city);
            cityService
                    .getCityByName(city)
                    .ifPresent(
                            cityRecord -> {
                                logger.debug(
                                        "Found city ID {} for name {}",
                                        cityRecord.getCityId(),
                                        city);
                                userRepository.updateCity(chatId, cityRecord.getCityId());
                            });
        }
    }

    @Override
    public void setNewUserKitchenTypes(long chatId, List<String> kitchenTypes) {
        logger.debug("Setting new kitchen types for user {}: {}", chatId, kitchenTypes);
        // Сначала удаляем все существующие типы кухни
        userKitchenTypeService.removeAllByUser(chatId);

        // Добавляем новые типы кухни
        kitchenTypes.forEach(
                kitchenType -> {
                    try {
                        int kitchenTypeId = Integer.parseInt(kitchenType);
                        logger.debug("Adding kitchen type by ID: {}", kitchenTypeId);
                        userKitchenTypeService.addKitchen(chatId, kitchenTypeId);
                    } catch (NumberFormatException e) {
                        // Если передан текст типа кухни, ищем его ID
                        logger.debug("Adding kitchen type by name: {}", kitchenType);
                        kitchenTypeService
                                .getByName(kitchenType)
                                .ifPresent(
                                        k -> {
                                            logger.debug(
                                                    "Found kitchen type ID {} for name {}",
                                                    k.getKitchenTypeId(),
                                                    kitchenType);
                                            userKitchenTypeService.addKitchen(
                                                    chatId, k.getKitchenTypeId());
                                        });
                    }
                });
    }

    @Override
    public void setNewUserPriceCategories(long chatId, List<String> priceCategories) {
        logger.debug("Setting new price categories for user {}: {}", chatId, priceCategories);
        // Сначала удаляем все существующие ценовые категории
        userPriceCategoryService.removeAllByUser(chatId);

        // Добавляем новые ценовые категории
        priceCategories.forEach(
                priceCategory -> {
                    try {
                        int priceCategoryId = Integer.parseInt(priceCategory);
                        logger.debug("Adding price category by ID: {}", priceCategoryId);
                        userPriceCategoryService.addPriceCategory(chatId, priceCategoryId);
                    } catch (NumberFormatException e) {
                        // Если передан текст ценовой категории, ищем её ID
                        logger.debug("Adding price category by name: {}", priceCategory);
                        priceCategoryService
                                .getByName(priceCategory)
                                .ifPresent(
                                        p -> {
                                            logger.debug(
                                                    "Found price category ID {} for name {}",
                                                    p.getPriceCategoryId(),
                                                    priceCategory);
                                            userPriceCategoryService.addPriceCategory(
                                                    chatId, p.getPriceCategoryId());
                                        });
                    }
                });
    }

    @Override
    public void setNewUserKeyWords(long chatId, List<String> keyWords) {
        logger.debug("Setting new keywords for user {}: {}", chatId, keyWords);
        String keywordsString = String.join(",", keyWords);
        userRepository.updateKeywords(chatId, keywordsString);
    }

    @Override
    public void setUserState(long chatId, String state) {
        userRepository.updateState(chatId, state);
    }
}

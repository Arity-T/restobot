package dev.tishenko.restobot.telegram.config;

import dev.tishenko.restobot.telegram.services.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserData {
    private long chatID;
    private String nickName;
    private List<FavouriteRestaurantCardDTO> favoriteList;
    private String city;
    private List<String> kitchenTypes;
    private List<String> priceCategories;
    private List<String> keyWords;
    private int index;

    private String cityForSearch;
    private List<String> kitchenTypesForSearch;
    private List<String> priceCategoriesForSearch;
    private List<String> keyWordsForSearch;

    private final UserDAO userDAO;
    private final FavoriteListDAO favoriteListDAO;
    private final SearchParametersService searchParametersService;

    private final List<String> correctCities;
    private final List<String> correctKitchenTypes;
    private final List<String> correctPriceCategories;

    public UserData(
            long chatID,
            String nickName,
            UserDTO userDTO,
            UserDAO userDAO,
            FavoriteListDAO favoriteListDAO,
            SearchParametersService searchParametersService) {
        setChatID(chatID);
        setNickName(nickName);

        favoriteList = userDTO.favoriteList();
        city = userDTO.city();
        kitchenTypes = userDTO.kitchenTypes();
        priceCategories = userDTO.priceCategories();
        keyWords = userDTO.keyWords();

        cityForSearch = city;
        kitchenTypesForSearch = kitchenTypes;
        priceCategoriesForSearch = priceCategories;
        keyWordsForSearch = keyWords;
        index = 0;

        this.userDAO = userDAO;
        this.favoriteListDAO = favoriteListDAO;
        this.searchParametersService = searchParametersService;

        correctCities = searchParametersService.getCitiesNames();
        correctKitchenTypes = searchParametersService.getKitchenTypesNames();
        correctPriceCategories = searchParametersService.getPriceCategoriesNames();
    }

    public UserData(
            long chatID,
            String nickName,
            UserDAO userDAO,
            FavoriteListDAO favoriteListDAO,
            SearchParametersService searchParametersService) {
        setChatID(chatID);
        setNickName(nickName);
        favoriteList = new ArrayList<>();
        city = "Любой";
        kitchenTypes = List.of("Любые");
        priceCategories = List.of("Любые");
        keyWords = List.of("Любые");

        cityForSearch = city;
        kitchenTypesForSearch = kitchenTypes;
        priceCategoriesForSearch = priceCategories;
        keyWordsForSearch = keyWords;
        index = 0;

        this.userDAO = userDAO;
        this.favoriteListDAO = favoriteListDAO;
        this.searchParametersService = searchParametersService;

        correctCities = searchParametersService.getCitiesNames();
        correctKitchenTypes = searchParametersService.getKitchenTypesNames();
        correctPriceCategories = searchParametersService.getPriceCategoriesNames();
    }

    public UserDTO toUserDTO() {
        return new UserDTO(
                chatID, nickName, city, kitchenTypes, priceCategories, keyWords, favoriteList);
    }

    public String userParamsToString() {
        return "Город: "
                + city
                + ".\n"
                + "Типы кухни: "
                + listToStringStream(kitchenTypes)
                + ".\n"
                + "Ценовые категории: "
                + listToStringStream(priceCategories)
                + ".\n"
                + "Ключевые слова: "
                + listToStringStream(keyWords)
                + ".\n";
    }

    public String userParamsToSearchRestaurantsToString() {
        return "Город: "
                + cityForSearch
                + ".\n"
                + "Типы кухни: "
                + listToStringStream(kitchenTypesForSearch)
                + ".\n"
                + "Ценовые категории: "
                + listToStringStream(priceCategoriesForSearch)
                + ".\n"
                + "Ключевые слова: "
                + listToStringStream(keyWordsForSearch)
                + ".\n";
    }

    public static String listToStringStream(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (list.size() > 1) {
            return String.join(", ", list);
        }
        return list.getFirst();
    }

    public boolean setKeyWords(String keyWords) {
        this.keyWords = List.of(keyWords.split(","));
        userDAO.setNewUserKeyWords(chatID, List.of(keyWords.split(",")));
        return true;
    }

    public boolean setKeyWordsForSearch(String keyWords) {
        this.keyWordsForSearch = List.of(keyWords.split(","));
        return true;
    }

    public FavouriteRestaurantCardDTO nextRestaurantFromFavoriteList() {
        index = index >= favoriteList.size() - 1 ? 0 : index + 1;
        return favoriteList.get(index);
    }

    public long getChatID() {
        return chatID;
    }

    void setChatID(long chatID) {
        this.chatID = chatID;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<FavouriteRestaurantCardDTO> getFavoriteList() {
        return favoriteList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        userDAO.setNewUserCity(chatID, city);
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public boolean isRestaurantInFavouriteList(RestaurantCardDTO restaurantCard) {
        return favoriteList.stream().anyMatch(x -> x.restaurantCardDTO() == restaurantCard);
    }

    public void removeRestaurantFromFavouriteListByIndex() {
        favoriteListDAO.removeRestaurantCardToFavoriteList(
                chatID, favoriteList.remove(index).restaurantCardDTO().tripadvisorId());
    }

    public FavouriteRestaurantCardDTO getRestaurantFromFavouriteListByIndex() {
        return favoriteList.get(index);
    }

    public void addRestaurantToFavouriteList(RestaurantCardDTO restaurantCard) {
        if (!isRestaurantInFavouriteList(restaurantCard)) {
            favoriteList.add(new FavouriteRestaurantCardDTO(restaurantCard, false));
            favoriteListDAO.addRestaurantCardToFavoriteList(chatID, restaurantCard.tripadvisorId());
        }
    }

    public void addRestaurantToFavouriteList(
            FavouriteRestaurantCardDTO favouriteRestaurantCardDTO) {
        favoriteListDAO.addRestaurantCardToFavoriteList(
                chatID, favouriteRestaurantCardDTO.restaurantCardDTO().tripadvisorId());
        favoriteList.add(favouriteRestaurantCardDTO);
    }

    public void removeRestaurantFromFavouriteList(RestaurantCardDTO restaurantCard) {
        favoriteListDAO.removeRestaurantCardToFavoriteList(chatID, restaurantCard.tripadvisorId());
        favoriteList.removeIf(x -> x.restaurantCardDTO() == restaurantCard);
    }

    public List<String> getKitchenTypes() {
        return kitchenTypes;
    }

    public void setKitchenTypes(List<String> kitchenTypes) {
        userDAO.setNewUserKitchenTypes(chatID, kitchenTypes);
        this.kitchenTypes = kitchenTypes;
    }

    public void changeIsVisited() {
        var updatedFavouriteRestaurantCardDTO =
                new FavouriteRestaurantCardDTO(
                        getRestaurantFromFavouriteListByIndex().restaurantCardDTO(),
                        !getRestaurantFromFavouriteListByIndex().isVisited());
        favoriteList.set(index, updatedFavouriteRestaurantCardDTO);
        favoriteListDAO.setVisitedStatus(
                chatID,
                updatedFavouriteRestaurantCardDTO.restaurantCardDTO().tripadvisorId(),
                updatedFavouriteRestaurantCardDTO.isVisited());
    }

    public List<String> getPriceCategories() {
        return priceCategories;
    }

    public void setCityForSearch(String cityForSearch) {
        this.cityForSearch = cityForSearch;
    }

    public void setKitchenTypesForSearch(List<String> kitchenTypesForSearch) {
        this.kitchenTypesForSearch = kitchenTypesForSearch;
    }

    public void setPriceCategoriesForSearch(List<String> priceCategoriesForSearch) {
        this.priceCategoriesForSearch = priceCategoriesForSearch;
    }

    public void setKeyWordsForSearch(List<String> keyWordsForSearch) {
        this.keyWordsForSearch = keyWordsForSearch;
    }

    public boolean checkAndSetCity(String city) {
        if (correctCities.contains(city)) {
            this.city = city;
            userDAO.setNewUserCity(chatID, city);
            return true;
        }
        return false;
    }

    public boolean checkAndSetKitchenTypes(String params) {
        List<String> kitchenTypes = Arrays.stream(params.split(",")).map(String::trim)
                .map(String::toLowerCase).toList();
        if (new HashSet<>(correctKitchenTypes).containsAll(kitchenTypes)) {
            this.kitchenTypes = kitchenTypes;
            userDAO.setNewUserKitchenTypes(chatID, kitchenTypes);
            return true;
        }
        return false;
    }

    public boolean checkAndSetPriceCategories(String params) {
        List<String> priceCategories = Arrays.stream(params.split(",")).map(String::trim).toList();
        if (new HashSet<>(correctPriceCategories).containsAll(priceCategories)) {
            this.priceCategories = priceCategories;
            userDAO.setNewUserKitchenTypes(chatID, priceCategories);
            return true;
        }
        return false;
    }

    public boolean checkAndSetCityForSearch(String city) {
        if (correctKitchenTypes.contains(city)) {
            this.cityForSearch = city;
            return true;
        }
        return false;
    }

    public boolean checkAndSetKitchenTypesForSearch(String params) {
        List<String> kitchenTypes = Arrays.stream(params.split(",")).map(String::trim)
                .map(String::toLowerCase).toList();
        if (new HashSet<>(correctKitchenTypes).containsAll(kitchenTypes)) {
            this.kitchenTypesForSearch = kitchenTypes;
            return true;
        }
        return false;
    }

    public boolean checkAndSetPriceCategoriesForSearch(String params) {
        List<String> priceCategories = Arrays.stream(params.split(",")).map(String::trim).toList();
        if (new HashSet<>(correctPriceCategories).containsAll(priceCategories)) {
            this.priceCategoriesForSearch = priceCategories;
            return true;
        }
        return false;
    }
}

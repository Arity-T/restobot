package dev.tishenko.restobot.telegram.config;

import dev.tishenko.restobot.telegram.services.FavouriteRestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.UserDTO;
import java.util.ArrayList;
import java.util.Arrays;
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

    public UserData(long chatID, String nickName, UserDTO userDTO) {
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
    }

    public UserData(long chatID, String nickName) {
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
    }

        public UserDTO toUserDTO() {
            return new UserDTO(chatID, nickName, city, kitchenTypes, priceCategories, keyWords,
     favoriteList);
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
            return String.join(",", list);
        }
        return list.getFirst();
    }

    public boolean setKeyWords(String keyWords) {
        this.keyWords = List.of(keyWords.split(","));
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<FavouriteRestaurantCardDTO> getFavoriteList() {
        return favoriteList;
    }

    public void setFavoriteList(List<FavouriteRestaurantCardDTO> favoriteList) {
        this.favoriteList = favoriteList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }

    public void parseKeyWords(String newKeyWords) {
        keyWords = Arrays.stream(newKeyWords.split(" ")).toList();
    }

    public boolean isRestaurantInFavouriteList(RestaurantCardDTO restaurantCard) {
        return favoriteList.stream().anyMatch(x -> x.restaurantCardDTO() == restaurantCard);
    }

    public void removeRestaurantFromFavouriteListByIndex() {
        favoriteList.remove(index);
    }

    public FavouriteRestaurantCardDTO getRestaurantFromFavouriteListByIndex() {
        return favoriteList.get(index);
    }

    public void addRestaurantToFavouriteList(RestaurantCardDTO restaurantCard) {
        if (!isRestaurantInFavouriteList(restaurantCard)) {
            favoriteList.add(new FavouriteRestaurantCardDTO(restaurantCard, false));
        }
    }

    public void addRestaurantToFavouriteList(FavouriteRestaurantCardDTO favouriteRestaurantCardDTO){
        favoriteList.add(favouriteRestaurantCardDTO);
    }

    public void removeRestaurantFromFavouriteList(RestaurantCardDTO restaurantCard) {
        favoriteList.removeIf(x -> x.restaurantCardDTO() == restaurantCard);
    }

    public List<String> getKitchenTypes() {
        return kitchenTypes;
    }

    public void setKitchenTypes(List<String> kitchenTypes) {
        this.kitchenTypes = kitchenTypes;
    }

    public void parseKitchenTypes(String newKitchenTypes) {
        kitchenTypes = Arrays.stream(newKitchenTypes.split(" ")).toList();
    }

    public void changeIsVisited(){
        var updatedFavouriteRestaurantCardDTO = new FavouriteRestaurantCardDTO(getRestaurantFromFavouriteListByIndex().restaurantCardDTO(), !getRestaurantFromFavouriteListByIndex().isVisited());
        removeRestaurantFromFavouriteListByIndex();
        addRestaurantToFavouriteList(updatedFavouriteRestaurantCardDTO);
    }

    public List<String> getPriceCategories() {
        return priceCategories;
    }

    public void setPriceCategories(List<String> priceCategories) {
        this.priceCategories = priceCategories;
    }

    public void parsePriceCategories(String newPriceCategories) {
        priceCategories = Arrays.stream(newPriceCategories.split(" ")).toList();
    }

    public String getCityForSearch() {
        return cityForSearch;
    }

    public void setCityForSearch(String cityForSearch) {
        this.cityForSearch = cityForSearch;
    }

    public List<String> getKitchenTypesForSearch() {
        return kitchenTypesForSearch;
    }

    public void setKitchenTypesForSearch(List<String> kitchenTypesForSearch) {
        this.kitchenTypesForSearch = kitchenTypesForSearch;
    }

    public List<String> getPriceCategoriesForSearch() {
        return priceCategoriesForSearch;
    }

    public void setPriceCategoriesForSearch(List<String> priceCategoriesForSearch) {
        this.priceCategoriesForSearch = priceCategoriesForSearch;
    }

    public List<String> getKeyWordsForSearch() {
        return keyWordsForSearch;
    }

    public void setKeyWordsForSearch(List<String> keyWordsForSearch) {
        this.keyWordsForSearch = keyWordsForSearch;
    }
}

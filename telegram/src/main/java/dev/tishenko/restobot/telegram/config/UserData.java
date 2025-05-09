package dev.tishenko.restobot.telegram.config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Scope("prototype")
public class UserData {
    private long chatID;
    private String nickName;
    private List<RestaurantCard> favoriteList;
    private String city;
    private List<String> kitchenTypes;
    private List<String> priceCategories;
    private List<String> keyWords;
    private int index;

    private String cityForSearch;
    private List<String> kitchenTypesForSearch;
    private List<String> priceCategoriesForSearch;
    private List<String> keyWordsForSearch;

    private List<String> correctCities;
    private List<String> correctKitchenTypes;
    private List<String> correctPriceCategoriesForSearch;


    public UserData() {
    }

    public UserData(long chatID, String nickName) {
        setChatID(chatID);
        setNickName(nickName);
        city = "Любой";
        kitchenTypes = List.of("Любые");
        priceCategories = List.of("Любые");
        keyWords = List.of("Любые");

        cityForSearch = city;
        kitchenTypesForSearch = kitchenTypes;
        priceCategoriesForSearch = priceCategories;
        keyWordsForSearch = keyWords;
        index = 0;

        correctCities = List.of("Москва", "Санкт-Петербург", "Новосибирск",
                "Екатеринбург", "Казань", "Красноярск", "Нижний Новгород",
                "Челябинск", "Уфа", "Самара", "Ростов-на-Дону",
                "Краснодар", "Омск", "Воронеж", "Пермь", "Волгоград");
        correctKitchenTypes = List.of("африканская", "азиатская", "американская",
                "барбекю", "ближневосточная", "британская", "вьетнамская",
                "восточно-европейская", "европейская", "ирландская", "испанская", "итальянская",
                "индийская", "каджунская", "карибская", "китайская", "мексиканская", "немецкая",
                "средиземноморская", "тайская", "французская", "фьюжн", "греческая", "японская", "южноамериканская");
        correctPriceCategoriesForSearch = List.of("Дешевое питание", "Средний ценовой сегмент", "Высокая кухня");
    }


    public String userParamsToString() {
        return "Город: " + city + ".\n" +
                "Типы кухни: " + listToStringStream(kitchenTypes) + ".\n" +
                "Ценовые категории: " + listToStringStream(priceCategories) + ".\n" +
                "Ключевые слова: " + listToStringStream(keyWords) + ".\n";
    }

    public String userParamsToSearchRestaurantsToString() {
        return "Город: " + cityForSearch + ".\n" +
                "Типы кухни: " + listToStringStream(kitchenTypesForSearch) + ".\n" +
                "Ценовые категории: " + listToStringStream(priceCategoriesForSearch) + ".\n" +
                "Ключевые слова: " + listToStringStream(keyWordsForSearch) + ".\n";
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

    public boolean checkAndSetCity(String city){
        if (correctCities.contains(city)){
            setCity(city);
            return true;
        }
        return false;
    }

    public boolean checkAndSetCityForSearch(String city){
        if (correctCities.contains(city)){
            setCityForSearch(city);
            return true;
        }
        return false;
    }

    public boolean checkAndSetKitchenTypes(String kitchenTypes){
        if (Arrays.stream(kitchenTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .allMatch(correctKitchenTypes::contains)){
            setKitchenTypes(List.of(kitchenTypes.split(",")));
            return true;
        }
        return false;
    }
    public boolean checkAndSetKitchenTypesForSearch(String kitchenTypes){
        if (Arrays.stream(kitchenTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .allMatch(correctKitchenTypes::contains)){
            setKitchenTypesForSearch(List.of(kitchenTypes.split(",")));
            return true;
        }
        return false;
    }

    public boolean checkAndSetPriceCategories(String priceCategories){
        if (Arrays.stream(priceCategories.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .allMatch(correctPriceCategoriesForSearch::contains)){
            setPriceCategories(List.of(priceCategories.split(",")));
            return true;
        }
        return false;
    }
    public boolean checkAndSetPriceCategoriesForSearch(String priceCategories){
        if (Arrays.stream(priceCategories.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .allMatch(correctPriceCategoriesForSearch::contains)){
            setPriceCategoriesForSearch(List.of(priceCategories.split(",")));
            return true;
        }
        return false;
    }

    public boolean setKeyWords(String keyWords) {
        this.keyWords = List.of(keyWords.split(","));
        return true;
    }

    public boolean setKeyWordsForSearch(String keyWords) {
        this.keyWordsForSearch = List.of(keyWords.split(","));
        return true;
    }

    public RestaurantCard nextRestaurantFromFavoriteList() {
        index = index >= favoriteList.size() ? 0 : index + 1;
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

    public List<RestaurantCard> getFavoriteList() {
        return favoriteList;
    }

    public void setFavoriteList(List<RestaurantCard> favoriteList) {
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

    public boolean isRestaurantInFavouriteList(RestaurantCard restaurantCard) {
        return favoriteList.contains(restaurantCard);
    }

    public void removeRestaurantFromFavouriteListByIndex() {
        favoriteList.remove(index);
    }

    public RestaurantCard getRestaurantFromFavouriteListByIndex() {
        return favoriteList.get(index);
    }

    public void addRestaurantToFavouriteList(RestaurantCard restaurantCard) {
        if (!favoriteList.contains(restaurantCard)) {
            favoriteList.add(restaurantCard);
        }
    }

    public void removeRestaurantFromFavouriteList(RestaurantCard restaurantCard) {
        favoriteList.remove(restaurantCard);
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

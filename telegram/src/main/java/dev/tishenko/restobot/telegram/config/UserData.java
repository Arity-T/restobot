package dev.tishenko.restobot.telegram.config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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

    public UserData() {
    }

    public UserData(long chatID, String nickName) {
        setChatID(chatID);
        setNickName(nickName);
        city = "Любой";
        kitchenTypes = List.of("Любые");
        priceCategories = List.of("Любые");
        keyWords = List.of("Любые");
    }


    public String userParamsToString() {
        return "Город: " + city + ".\n" +
                "Типы кухни: " + listToStringStream(kitchenTypes) + ".\n" +
                "Ценовые категории: " + listToStringStream(priceCategories) + ".\n" +
                "Ключевые слова: " + listToStringStream(keyWords) + ".\n";
    }


    public static String listToStringStream(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        // Если больше одного элемента — join
        if (list.size() > 1) {
            return String.join(",", list);
        }
        return list.stream()
                .findFirst()
                .map(s -> s.isEmpty() ? "" : s.substring(0, 1))
                .orElse("");
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

    public void parseKeyWords(String newkeyWords) {
        keyWords = Arrays.stream(newkeyWords.split(" ")).toList();
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
}

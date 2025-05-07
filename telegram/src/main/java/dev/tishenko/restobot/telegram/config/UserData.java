package dev.tishenko.restobot.telegram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Scope("prototype")
public class UserData {
    private int chatID;
    private String nickName;
    private List<RestaurantCard> favoriteList;
    private String city;
    private List<String> keyWords;

    public UserData(int chatID, String nickName, List<RestaurantCard> favoriteList) {
        setChatID(chatID);
        setNickName(nickName);
        setFavoriteList(favoriteList);
    }


    public int getChatID() {
        return chatID;
    }

    void setChatID(int chatID) {
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

    public void addRestaurantToFavouriteList(RestaurantCard restaurantCard){
        if (!favoriteList.contains(restaurantCard)){
            favoriteList.add(restaurantCard);
        }
    }

    public void removeRestaurantFromFavouriteList(RestaurantCard restaurantCard){
        if (favoriteList.contains(restaurantCard)){
            favoriteList.remove(restaurantCard);
        }
    }

}

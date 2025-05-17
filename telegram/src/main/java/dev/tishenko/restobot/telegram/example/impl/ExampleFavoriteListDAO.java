package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.FavoriteListDAO;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExampleFavoriteListDAO implements FavoriteListDAO {

    private List<FavoriteRestaurantCardDTO> favouriteList = new ArrayList<>();

    @Override
    public void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {}

    @Override
    public void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {}

    @Override
    public void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited) {}
}

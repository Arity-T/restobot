package dev.tishenko.restobot.telegram.example.impl;

import dev.tishenko.restobot.telegram.services.FavoriteListDAO;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleFavoriteListDAO implements FavoriteListDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExampleFavoriteListDAO.class);

    private List<FavoriteRestaurantCardDTO> favouriteList = new ArrayList<>();

    @Override
    public void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        logger.info(
                "Adding restaurant card to favorite list: chatId={}, tripadvisorId={}",
                chatId,
                tripadvisorId);
    }

    @Override
    public void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        logger.info(
                "Removing restaurant card from favorite list: chatId={}, tripadvisorId={}",
                chatId,
                tripadvisorId);
    }

    @Override
    public void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited) {
        logger.info(
                "Setting visited status: chatId={}, tripadvisorId={}, isVisited={}",
                chatId,
                tripadvisorId,
                isVisited);
    }
}

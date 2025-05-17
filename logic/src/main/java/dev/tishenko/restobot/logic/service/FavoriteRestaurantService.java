package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.FavoriteRestaurantRepository;
import dev.tishenko.restobot.telegram.services.FavoriteListDAO;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.example.jooq.generated.tables.records.FavoriteRestaurantRecord;
import org.springframework.stereotype.Service;

@Service
public class FavoriteRestaurantService implements FavoriteListDAO {

    private final FavoriteRestaurantRepository repo;

    public FavoriteRestaurantService(FavoriteRestaurantRepository repo) {
        this.repo = repo;
    }

    @Override
    public void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        repo.addRestaurant(chatId, tripadvisorId);
    }

    @Override
    public void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        repo.delete(chatId, tripadvisorId);
    }

    @Override
    public void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited) {
        repo.markVisited(chatId, tripadvisorId, isVisited);
    }

    // TODO: implement when cache db is ready
    public List<FavoriteRestaurantCardDTO> getFavoriteList(long chatId) {
        return repo.getAllFavorites(chatId).stream()
                .map(
                        record ->
                                new FavoriteRestaurantCardDTO(
                                        new RestaurantCardDTO(
                                                record.getTripadvisorId(),
                                                "", // name will be filled by the caller
                                                "", // address will be filled by the caller
                                                0.0, // rating will be filled by the caller
                                                null, // website will be filled by the caller
                                                "", // description will be filled by the caller
                                                0.0, // latitude will be filled by the caller
                                                0.0, // longitude will be filled by the caller
                                                "" // city will be filled by the caller
                                                ),
                                        record.getIsVisited()))
                .collect(Collectors.toList());
    }

    public List<FavoriteRestaurantRecord> getFavoriteListRecords(long chatId) {
        return repo.getAllFavorites(chatId);
    }

    // Legacy methods for backward compatibility
    public FavoriteRestaurantRecord addRestaurant(long chatId, int tripadvisorId) {
        return repo.addRestaurant(chatId, tripadvisorId);
    }

    public void setVisited(long chatId, int tripadvisorId, boolean visited) {
        repo.markVisited(chatId, tripadvisorId, visited);
    }

    public void removeFromFavorites(long chatId, int tripadvisorId) {
        repo.delete(chatId, tripadvisorId);
    }

    public List<FavoriteRestaurantRecord> getAll(long chatId) {
        return repo.getAllFavorites(chatId);
    }

    public List<FavoriteRestaurantRecord> getByVisitStatus(long chatId, boolean visited) {
        return repo.getVisited(chatId, visited);
    }
}

package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.FavoriteRestaurantRepository;
import java.util.List;
import org.example.jooq.generated.tables.records.FavoriteRestaurantRecord;
import org.springframework.stereotype.Service;

@Service
public class FavoriteRestaurantService {

    private final FavoriteRestaurantRepository repo;

    public FavoriteRestaurantService(FavoriteRestaurantRepository repo) {
        this.repo = repo;
    }

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

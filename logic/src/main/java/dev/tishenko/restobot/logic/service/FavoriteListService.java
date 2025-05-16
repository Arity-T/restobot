package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.FavoriteListRepository;
import java.util.List;
import org.example.jooq.generated.tables.records.FavoriteListRecord;
import org.springframework.stereotype.Service;

@Service
public class FavoriteListService {

    private final FavoriteListRepository repo;

    public FavoriteListService(FavoriteListRepository repo) {
        this.repo = repo;
    }

    public FavoriteListRecord addRestaurant(int tripadvisorId) {
        return repo.addRestaurant(tripadvisorId);
    }

    public void setVisited(int favoriteListId, boolean visited) {
        repo.markVisited(favoriteListId, visited);
    }

    public void removeFromFavorites(int favoriteListId) {
        repo.delete(favoriteListId);
    }

    public List<FavoriteListRecord> getAll() {
        return repo.getAllFavorites();
    }

    public List<FavoriteListRecord> getByVisitStatus(boolean visited) {
        return repo.getVisited(visited);
    }
}

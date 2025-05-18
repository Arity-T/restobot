package dev.tishenko.restobot.data.service;

import dev.tishenko.restobot.data.repository.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.RestaurantRecord;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    public final RestaurantRepository repo;

    public RestaurantService(RestaurantRepository repo) {
        this.repo = repo;
    }

    public void addRestaurant(RestaurantRecord restaurant) {
        repo.addRestaurant(restaurant);
    }

    public Optional<RestaurantRecord> getById(int restaurantId) {
        return repo.findById(restaurantId);
    }

    public Optional<RestaurantRecord> getByTripadvisorId(int tripadvisorId) {
        return repo.findByTripadvisorId(tripadvisorId);
    }

    public List<RestaurantRecord> getByCityId(int cityId) {
        return repo.findByCityId(cityId);
    }

    public List<RestaurantRecord> getAll() {
        return repo.findAll();
    }

    public void assignCity(int restaurantId, int cityId) {
        repo.updateCity(restaurantId, cityId);
    }

    public List<RestaurantRecord> getByTripadvisorIds(List<Integer> tripadvisorIds) {
        return repo.findByTripadvisorIds(tripadvisorIds);
    }
}

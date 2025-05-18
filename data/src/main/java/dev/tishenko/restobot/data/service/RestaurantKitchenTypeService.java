package dev.tishenko.restobot.data.service;

import dev.tishenko.restobot.data.repository.RestaurantKitchenTypeRepository;
import java.util.List;
import org.example.jooq.generated.tables.records.RestaurantKitchenTypeRecord;
import org.springframework.stereotype.Service;

@Service
public class RestaurantKitchenTypeService {

    private final RestaurantKitchenTypeRepository repo;

    public RestaurantKitchenTypeService(RestaurantKitchenTypeRepository repo) {
        this.repo = repo;
    }

    public void addKitchen(int restaurantId, int kitchenTypeId) {
        repo.addKitchen(restaurantId, kitchenTypeId);
    }

    public void removeKitchen(int restaurantId, int kitchenTypeId) {
        repo.removeKitchen(restaurantId, kitchenTypeId);
    }

    public List<RestaurantKitchenTypeRecord> getAllByRestaurant(int restaurantId) {
        return repo.getAllByRestaurant(restaurantId);
    }
}

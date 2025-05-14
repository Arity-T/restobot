package dev.tishenko.restobot.data.service;

import java.util.List;

import org.example.jooq.generated.tables.records.RestaurantPriceCategoryRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.data.repository.RestaurantPriceCategoryRepository;

@Service
public class RestaurantPriceCategoryService {
  
  private final RestaurantPriceCategoryRepository repo;

  public RestaurantPriceCategoryService(RestaurantPriceCategoryRepository repo) {
    this.repo = repo;
  }

  public void addPriceCategory(int restaurantId, int priceCategoryId) {
    repo.addPriceCategory(restaurantId, priceCategoryId);
  }

  public void removePriceCategory(int restaurantId, int priceCategoryId) {
    repo.removePriceCategory(restaurantId, priceCategoryId);
  }

  public List<RestaurantPriceCategoryRecord> getAllByRestaurant(int restaurantId) {
    return repo.getAllByRestaurant(restaurantId);
  }
}

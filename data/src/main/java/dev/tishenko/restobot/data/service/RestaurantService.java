package dev.tishenko.restobot.data.service;

import java.util.List;
import java.util.Optional;

import org.example.jooq.generated.tables.records.RestaurantRecord;

import dev.tishenko.restobot.data.repository.RestaurantRepository;

public class RestaurantService {
  
  public final RestaurantRepository repo;

  public RestaurantService(RestaurantRepository repo) {
    this.repo = repo;
  }

  public void addRestaurant(RestaurantRecord restaurant) {
    repo.addRestaurant(restaurant);
  }

  public Optional<RestaurantRecord> getById(int id) {
    return Optional.ofNullable(repo.findById(id));
  }

  public Optional<RestaurantRecord> getByTripadvisorId(int tripadvisorId) {
    return Optional.ofNullable(repo.findByTripadvisorId(tripadvisorId));
  }

  public List<RestaurantRecord> getByCityId(int cityId) {
    return repo.findByCityId(cityId);
  }

  public List<RestaurantRecord> getAll() {
    return repo.findAll();
  }

  public void assingCity(int restaurantId, int cityId) {
    repo.updateCity(restaurantId, cityId);
  }
}

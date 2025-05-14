package dev.tishenko.restobot.data.repository;

import java.util.List;

import org.example.jooq.generated.tables.Restaurant;
import org.example.jooq.generated.tables.records.RestaurantRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantRepository {
  private final DSLContext dsl;

  public RestaurantRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public void addRestaurant(RestaurantRecord restaurant) {
    dsl.insertInto(Restaurant.RESTAURANT)
       .set(restaurant)
       .execute();
  }

  public RestaurantRecord findById(int id) {
    return dsl.selectFrom(Restaurant.RESTAURANT)
              .where(Restaurant.RESTAURANT.RESTAURANT_ID.eq(id))
              .fetchOne();
  }

  public RestaurantRecord findByTripadvisorId(int tripadvisorId) {
    return dsl.selectFrom(Restaurant.RESTAURANT)
              .where(Restaurant.RESTAURANT.TRIPADVISOR_ID.eq(tripadvisorId))
              .fetchOne();
  }

  public List<RestaurantRecord> findByCityId(int cityId) {
    return dsl.selectFrom(Restaurant.RESTAURANT)
              .where(Restaurant.RESTAURANT.CITY_ID.eq(cityId))
              .fetch();
  }

  public List<RestaurantRecord> findAll() {
    return dsl.selectFrom(Restaurant.RESTAURANT).fetch();
  }

  public void updateCity(int restaurantId, int cityId) {
    dsl.update(Restaurant.RESTAURANT)
       .set(Restaurant.RESTAURANT.CITY_ID, cityId)
       .where(Restaurant.RESTAURANT.RESTAURANT_ID.eq(restaurantId))
       .execute();
  }
}

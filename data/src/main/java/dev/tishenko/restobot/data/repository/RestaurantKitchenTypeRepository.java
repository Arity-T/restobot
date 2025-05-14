package dev.tishenko.restobot.data.repository;

import java.util.List;

import org.example.jooq.generated.tables.RestaurantKitchenType;
import org.example.jooq.generated.tables.records.RestaurantKitchenTypeRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantKitchenTypeRepository {
  
  private final DSLContext dsl;

  public RestaurantKitchenTypeRepository(DSLContext dsl) {
    this.dsl = dsl;
  }
  
  public void addKitchen(int restaurantId, int kitchenTypeId) {
    dsl.insertInto(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE)
       .set(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE.RESTAURANT_ID, restaurantId)
       .set(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE.KITCHEN_TYPE_ID, kitchenTypeId)
       .execute();
  }

  public void removeKitchen(int restaurantId, int kitchenTypeId) {
    dsl.deleteFrom(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE)
       .where(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE.RESTAURANT_ID.eq(restaurantId))
       .and(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE.KITCHEN_TYPE_ID.eq(kitchenTypeId))
       .execute();
  }

  public List<RestaurantKitchenTypeRecord> getAllByRestaurant(int restaurantId) {
    return dsl.selectFrom(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE)
              .where(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE.RESTAURANT_ID.eq(restaurantId))
              .fetch();
  }
}

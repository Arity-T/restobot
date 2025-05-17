package dev.tishenko.restobot.data.repository;

import java.util.List;
import org.example.jooq.generated.tables.RestaurantPriceCategory;
import org.example.jooq.generated.tables.records.RestaurantPriceCategoryRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantPriceCategoryRepository {

    private final DSLContext dsl;

    public RestaurantPriceCategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void addPriceCategory(int restaurantId, int priceCategoryId) {
        dsl.insertInto(RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY)
                .set(RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY.RESTAURANT_ID, restaurantId)
                .set(
                        RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY.PRICE_CATEGORY_ID,
                        priceCategoryId)
                .execute();
    }

    public void removePriceCategory(int restaurantId, int priceCategoryId) {
        dsl.deleteFrom(RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY)
                .where(
                        RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY.RESTAURANT_ID.eq(
                                restaurantId))
                .and(
                        RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY.PRICE_CATEGORY_ID.eq(
                                priceCategoryId))
                .execute();
    }

    public List<RestaurantPriceCategoryRecord> getAllByRestaurant(int restaurantId) {
        return dsl.selectFrom(RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY)
                .where(
                        RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY.RESTAURANT_ID.eq(
                                restaurantId))
                .fetch();
    }
}

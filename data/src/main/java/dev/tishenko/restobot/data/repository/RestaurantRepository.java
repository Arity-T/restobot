package dev.tishenko.restobot.data.repository;

import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.example.jooq.generated.tables.City;
import org.example.jooq.generated.tables.KitchenType;
import org.example.jooq.generated.tables.PriceCategory;
import org.example.jooq.generated.tables.Restaurant;
import org.example.jooq.generated.tables.RestaurantKitchenType;
import org.example.jooq.generated.tables.RestaurantPriceCategory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.example.jooq.generated.tables.records.RestaurantRecord;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantRepository {
    private final DSLContext dsl;

    private final CityRepository cityRepository;

    public RestaurantRepository(DSLContext dsl, CityRepository cityRepository) {
        this.dsl = dsl;
        this.cityRepository = cityRepository;
    }

    public List<RestaurantCardDTO> findByGeolocation(double latitude, double longitude)
            throws MalformedURLException, URISyntaxException {
        // Радиус 1 км в градусах (примерно)
        double radiusInDegrees = 0.009;

        return dsl.select()
                .from(Restaurant.RESTAURANT)
                .where(
                        Restaurant.RESTAURANT
                                .LATITUDE
                                .between(latitude - radiusInDegrees, latitude + radiusInDegrees)
                                .and(
                                        Restaurant.RESTAURANT.LONGITUDE.between(
                                                longitude - radiusInDegrees,
                                                longitude + radiusInDegrees)))
                .fetch()
                .map(
                        record -> {
                            try {
                                return new RestaurantCardDTO(
                                        record.get(Restaurant.RESTAURANT.TRIPADVISOR_ID),
                                        record.get(Restaurant.RESTAURANT.NAME),
                                        record.get(Restaurant.RESTAURANT.ADDRESS_STRING),
                                        record.get(Restaurant.RESTAURANT.RATING).doubleValue(),
                                        new URI(record.get(Restaurant.RESTAURANT.WEBSITE)).toURL(),
                                        record.get(Restaurant.RESTAURANT.DESCRIPTION),
                                        record.get(Restaurant.RESTAURANT.LATITUDE),
                                        record.get(Restaurant.RESTAURANT.LONGITUDE),
                                        cityRepository.findNameById(record.get(Restaurant.RESTAURANT.CITY_ID)));
                            } catch (URISyntaxException | MalformedURLException e) {
                                throw new RuntimeException("Invalid URL in database", e);
                            }
                        });
    }

    public List<RestaurantCardDTO> findByParams(
            String city,
            List<String> kitchenTypes,
            List<String> priceCategories,
            List<String> keyWords)
            throws MalformedURLException, URISyntaxException {

        Condition condition = null;
        // TODO: "Любой" может быть только город, "отключено" для всех остальных параметров
        // Добавляем фильтр по городу, если указан
        if (city != null && !city.equals("Любой")) {
            Integer cityId =
                    dsl.select(City.CITY.CITY_ID)
                            .from(City.CITY)
                            .where(City.CITY.NAME.eq(city))
                            .fetchOne(City.CITY.CITY_ID);

            if (cityId != null) {
                condition = Restaurant.RESTAURANT.CITY_ID.eq(cityId);
            }
        }

        // Добавляем фильтр по типам кухни, если указаны
        if (kitchenTypes != null && !kitchenTypes.contains("Отключено")) {
            var kitchenTypeIds =
                    dsl.select(KitchenType.KITCHEN_TYPE.KITCHEN_TYPE_ID)
                            .from(KitchenType.KITCHEN_TYPE)
                            .where(KitchenType.KITCHEN_TYPE.NAME.in(kitchenTypes))
                            .fetch()
                            .map(record -> record.get(KitchenType.KITCHEN_TYPE.KITCHEN_TYPE_ID));

            if (!kitchenTypeIds.isEmpty()) {
                Condition kitchenTypeCondition =
                        Restaurant.RESTAURANT.RESTAURANT_ID.in(
                                dsl.select(
                                                RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE
                                                        .RESTAURANT_ID)
                                        .from(RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE)
                                        .where(
                                                RestaurantKitchenType.RESTAURANT_KITCHEN_TYPE
                                                        .KITCHEN_TYPE_ID.in(kitchenTypeIds)));
                condition =
                        condition == null
                                ? kitchenTypeCondition
                                : condition.and(kitchenTypeCondition);
            }
        }

        // Добавляем фильтр по ценовым категориям, если указаны
        if (priceCategories != null && !priceCategories.contains("Отключено")) {
            var priceCategoryIds =
                    dsl.select(PriceCategory.PRICE_CATEGORY.PRICE_CATEGORY_ID)
                            .from(PriceCategory.PRICE_CATEGORY)
                            .where(PriceCategory.PRICE_CATEGORY.NAME.in(priceCategories))
                            .fetch()
                            .map(
                                    record ->
                                            record.get(
                                                    PriceCategory.PRICE_CATEGORY
                                                            .PRICE_CATEGORY_ID));

            if (!priceCategoryIds.isEmpty()) {
                Condition priceCategoryCondition =
                        Restaurant.RESTAURANT.RESTAURANT_ID.in(
                                dsl.select(
                                                RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY
                                                        .RESTAURANT_ID)
                                        .from(RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY)
                                        .where(
                                                RestaurantPriceCategory.RESTAURANT_PRICE_CATEGORY
                                                        .PRICE_CATEGORY_ID.in(priceCategoryIds)));
                condition =
                        condition == null
                                ? priceCategoryCondition
                                : condition.and(priceCategoryCondition);
            }
        }

        // TODO: надо придумать, что с ними делать. Ключевые слова мы нигде не храним, их надо
        // подмешивать в запрос к tripadvisor.
        // Добавляем фильтр по ключевым словам, если указаны
        // if (keyWords != null && !keyWords.contains("Отключено")) {
        //     for (String keyword : keyWords) {
        //         Condition keywordCondition = Restaurant.RESTAURANT.NAME.likeIgnoreCase("%" +
        // keyword + "%")
        //                 .or(Restaurant.RESTAURANT.DESCRIPTION.likeIgnoreCase("%" + keyword +
        // "%"));
        //         condition = condition == null ? keywordCondition :
        // condition.and(keywordCondition);
        //     }
        // }

        var baseQuery = dsl.select().from(Restaurant.RESTAURANT);

        var query = condition != null ? baseQuery.where(condition) : baseQuery;

        return query.fetch()
                .map(
                        record -> {
                            try {
                                return new RestaurantCardDTO(
                                        record.get(Restaurant.RESTAURANT.TRIPADVISOR_ID),
                                        record.get(Restaurant.RESTAURANT.NAME),
                                        record.get(Restaurant.RESTAURANT.ADDRESS_STRING),
                                        record.get(Restaurant.RESTAURANT.RATING).doubleValue(),
                                        new URI(record.get(Restaurant.RESTAURANT.WEBSITE)).toURL(),
                                        record.get(Restaurant.RESTAURANT.DESCRIPTION),
                                        record.get(Restaurant.RESTAURANT.LATITUDE),
                                        record.get(Restaurant.RESTAURANT.LONGITUDE),
                                        cityRepository.findNameById(record.get(Restaurant.RESTAURANT.CITY_ID)));
                            } catch (URISyntaxException | MalformedURLException e) {
                                throw new RuntimeException("Invalid URL in database", e);
                            }
                        });
    }

    public void addRestaurant(RestaurantRecord restaurant) {
        dsl.insertInto(Restaurant.RESTAURANT).values(restaurant).execute();
    }

    public Optional<RestaurantRecord> findById(int restaurantId) {
        return Optional.ofNullable(dsl.selectFrom(Restaurant.RESTAURANT)
                .where(Restaurant.RESTAURANT.RESTAURANT_ID.eq(restaurantId))
                .fetchOne());
    }

    public Optional<RestaurantRecord> findByTripadvisorId(int tripadvisorId) {
        return Optional.ofNullable(dsl.selectFrom(Restaurant.RESTAURANT)
                .where(Restaurant.RESTAURANT.TRIPADVISOR_ID.eq(tripadvisorId))
                .fetchOne());
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

package dev.tishenko.restobot.logic.repository;

import java.util.List;
import org.example.jooq.generated.tables.FavoriteRestaurant;
import org.example.jooq.generated.tables.records.FavoriteRestaurantRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRestaurantRepository {

    private final DSLContext dsl;

        public FavoriteRestaurantRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public FavoriteRestaurantRecord addRestaurant(long chatId, int tripadvisorId) {
        return dsl.insertInto(FavoriteRestaurant.FAVORITE_RESTAURANT)
                .set(FavoriteRestaurant.FAVORITE_RESTAURANT.CHAT_ID, chatId)
                .set(FavoriteRestaurant.FAVORITE_RESTAURANT.TRIPADVISOR_ID, tripadvisorId)
                .returning()
                .fetchOne();
    }

    public void markVisited(long chatId, int tripadvisorId, boolean visited) {
        dsl.update(FavoriteRestaurant.FAVORITE_RESTAURANT)
                .set(FavoriteRestaurant.FAVORITE_RESTAURANT.IS_VISITED, visited)
                .where(FavoriteRestaurant.FAVORITE_RESTAURANT.CHAT_ID.eq(chatId)
                        .and(FavoriteRestaurant.FAVORITE_RESTAURANT.TRIPADVISOR_ID.eq(tripadvisorId)))
                .execute();
    }

    public void delete(long chatId, int tripadvisorId) {
        dsl.deleteFrom(FavoriteRestaurant.FAVORITE_RESTAURANT)
                .where(FavoriteRestaurant.FAVORITE_RESTAURANT.CHAT_ID.eq(chatId)
                        .and(FavoriteRestaurant.FAVORITE_RESTAURANT.TRIPADVISOR_ID.eq(tripadvisorId)))
                .execute();
    }

    public List<FavoriteRestaurantRecord> getAllFavorites(long chatId) {
        return dsl.selectFrom(FavoriteRestaurant.FAVORITE_RESTAURANT)
                .where(FavoriteRestaurant.FAVORITE_RESTAURANT.CHAT_ID.eq(chatId))
                .fetch();
    }

    public List<FavoriteRestaurantRecord> getVisited(long chatId, boolean visited) {
        return dsl.selectFrom(FavoriteRestaurant.FAVORITE_RESTAURANT)
                .where(FavoriteRestaurant.FAVORITE_RESTAURANT.CHAT_ID.eq(chatId)
                        .and(FavoriteRestaurant.FAVORITE_RESTAURANT.IS_VISITED.eq(visited)))
                .fetch();
    }
}

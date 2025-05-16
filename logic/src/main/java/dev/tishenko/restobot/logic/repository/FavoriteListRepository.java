package dev.tishenko.restobot.logic.repository;

import java.util.List;
import org.example.jooq.generated.tables.FavoriteList;
import org.example.jooq.generated.tables.records.FavoriteListRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteListRepository {

    private final DSLContext dsl;

    public FavoriteListRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public FavoriteListRecord addRestaurant(int tripadvisorId) {
        return dsl.insertInto(FavoriteList.FAVORITE_LIST)
                .set(FavoriteList.FAVORITE_LIST.TRIPADVISOR_ID, tripadvisorId)
                .set(FavoriteList.FAVORITE_LIST.IS_VISITED, false)
                .returning()
                .fetchOne();
    }

    public void markVisited(int favoriteListId, boolean visited) {
        dsl.update(FavoriteList.FAVORITE_LIST)
                .set(FavoriteList.FAVORITE_LIST.IS_VISITED, visited)
                .where(FavoriteList.FAVORITE_LIST.FAVORITE_LIST_ID.eq(favoriteListId))
                .execute();
    }

    public void delete(int favoriteListId) {
        dsl.deleteFrom(FavoriteList.FAVORITE_LIST)
                .where(FavoriteList.FAVORITE_LIST.FAVORITE_LIST_ID.eq(favoriteListId))
                .execute();
    }

    public List<FavoriteListRecord> getAllFavorites() {
        return dsl.selectFrom(FavoriteList.FAVORITE_LIST).fetch();
    }

    public List<FavoriteListRecord> getVisited(boolean visited) {
        return dsl.selectFrom(FavoriteList.FAVORITE_LIST)
                .where(FavoriteList.FAVORITE_LIST.IS_VISITED.eq(visited))
                .fetch();
    }
}

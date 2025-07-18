package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.UserPriceCategory;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.UserPriceCategoryRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserPriceCategoryRepository {

    private final DSLContext dsl;

    public UserPriceCategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void addPriceCategory(long chatId, int categoryId) {
        dsl.insertInto(UserPriceCategory.USER_PRICE_CATEGORY)
                .set(UserPriceCategory.USER_PRICE_CATEGORY.CHAT_ID, chatId)
                .set(UserPriceCategory.USER_PRICE_CATEGORY.PRICE_CATEGORY_ID, categoryId)
                .execute();
    }

    public void removePriceCategory(long chatId, int categoryId) {
        dsl.deleteFrom(UserPriceCategory.USER_PRICE_CATEGORY)
                .where(UserPriceCategory.USER_PRICE_CATEGORY.CHAT_ID.eq(chatId))
                .and(UserPriceCategory.USER_PRICE_CATEGORY.PRICE_CATEGORY_ID.eq(categoryId))
                .execute();
    }

    public void removeAllByUser(long chatId) {
        dsl.deleteFrom(UserPriceCategory.USER_PRICE_CATEGORY)
                .where(UserPriceCategory.USER_PRICE_CATEGORY.CHAT_ID.eq(chatId))
                .execute();
    }

    public List<UserPriceCategoryRecord> getAllByUser(long chatId) {
        return dsl.selectFrom(UserPriceCategory.USER_PRICE_CATEGORY)
                .where(UserPriceCategory.USER_PRICE_CATEGORY.CHAT_ID.eq(chatId))
                .fetch();
    }
}

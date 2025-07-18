package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.UserKitchenType;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.UserKitchenTypeRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserKitchenTypeRepository {

    private final DSLContext dsl;

    public UserKitchenTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void addKitchen(long chatId, int kitchenTypeId) {
        dsl.insertInto(UserKitchenType.USER_KITCHEN_TYPE)
                .set(UserKitchenType.USER_KITCHEN_TYPE.CHAT_ID, chatId)
                .set(UserKitchenType.USER_KITCHEN_TYPE.KITCHEN_TYPE_ID, kitchenTypeId)
                .execute();
    }

    public void removeKitchen(long chatId, int kitchenTypeId) {
        dsl.deleteFrom(UserKitchenType.USER_KITCHEN_TYPE)
                .where(UserKitchenType.USER_KITCHEN_TYPE.CHAT_ID.eq(chatId))
                .and(UserKitchenType.USER_KITCHEN_TYPE.KITCHEN_TYPE_ID.eq(kitchenTypeId))
                .execute();
    }

    public void removeAllByUser(long chatId) {
        dsl.deleteFrom(UserKitchenType.USER_KITCHEN_TYPE)
                .where(UserKitchenType.USER_KITCHEN_TYPE.CHAT_ID.eq(chatId))
                .execute();
    }

    public List<UserKitchenTypeRecord> getAllByUser(long chatId) {
        return dsl.selectFrom(UserKitchenType.USER_KITCHEN_TYPE)
                .where(UserKitchenType.USER_KITCHEN_TYPE.CHAT_ID.eq(chatId))
                .fetch();
    }
}

package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.Users;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.UsersRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final DSLContext dsl;

    // через конструктор получаем DSLContext — он будет создан в JooqConfig
    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // Метод добавления пользователя
    public void saveUser(long chatId, String nickname) {
        dsl.insertInto(Users.USERS)
                .set(Users.USERS.CHAT_ID, chatId)
                .set(Users.USERS.NICKNAME, nickname)
                .onConflictDoNothing() // чтобы не падало, если уже есть
                .execute();
    }

    // Метод чтения пользователя
    public UsersRecord findByChatId(long chatId) {
        return dsl.selectFrom(Users.USERS)
                .where(Users.USERS.CHAT_ID.eq(chatId))
                .fetchOne(); // вернёт null, если не найден
    }

    public void updateCity(long chatId, int cityId) {
        dsl.update(Users.USERS)
                .set(Users.USERS.CITY_ID, cityId)
                .where(Users.USERS.CHAT_ID.eq(chatId))
                .execute();
    }

    public void updateKeywords(long chatId, String keywords) {
        dsl.update(Users.USERS)
                .set(Users.USERS.KEYWORDS, keywords)
                .where(Users.USERS.CHAT_ID.eq(chatId))
                .execute();
    }

    public List<UsersRecord> findAll() {
        return dsl.selectFrom(Users.USERS).fetch();
    }

    public void updateState(long chatId, String state) {
        dsl.update(Users.USERS)
                .set(Users.USERS.STATE, state)
                .where(Users.USERS.CHAT_ID.eq(chatId))
                .execute();
    }
}

package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.AdminData;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.AdminDataRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDataRepository {

    private final DSLContext dsl;

    public AdminDataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insertAdminKey(String hash, String salt) {
        dsl.insertInto(AdminData.ADMIN_DATA)
                .set(AdminData.ADMIN_DATA.HASH, hash)
                .set(AdminData.ADMIN_DATA.SALT, salt)
                .execute();
    }

    public AdminDataRecord findByHash(String hash) {
        return dsl.selectFrom(AdminData.ADMIN_DATA)
                .where(AdminData.ADMIN_DATA.HASH.eq(hash))
                .fetchOne();
    }

    public List<AdminDataRecord> findAll() {
        return dsl.selectFrom(AdminData.ADMIN_DATA).fetch();
    }
}

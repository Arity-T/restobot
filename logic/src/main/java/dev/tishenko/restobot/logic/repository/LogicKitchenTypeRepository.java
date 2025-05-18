package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.KitchenType;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.KitchenTypeRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class LogicKitchenTypeRepository {

    private final DSLContext dsl;

    public LogicKitchenTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public KitchenTypeRecord findById(int id) {
        return dsl.selectFrom(KitchenType.KITCHEN_TYPE)
                .where(KitchenType.KITCHEN_TYPE.KITCHEN_TYPE_ID.eq(id))
                .fetchOne();
    }

    public KitchenTypeRecord findByName(String name) {
        return dsl.selectFrom(KitchenType.KITCHEN_TYPE)
                .where(KitchenType.KITCHEN_TYPE.NAME.eq(name))
                .fetchOne();
    }

    public List<KitchenTypeRecord> findAll() {
        return dsl.selectFrom(KitchenType.KITCHEN_TYPE).fetch();
    }
}

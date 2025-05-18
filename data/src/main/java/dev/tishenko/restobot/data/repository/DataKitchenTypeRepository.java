package dev.tishenko.restobot.data.repository;

import java.util.List;
import org.example.jooq.generated.tables.KitchenType;
import org.example.jooq.generated.tables.records.KitchenTypeRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class DataKitchenTypeRepository {

    private final DSLContext dsl;

    public DataKitchenTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public KitchenTypeRecord findById(int kitchenTypeId) {
        return dsl.selectFrom(KitchenType.KITCHEN_TYPE)
                .where(KitchenType.KITCHEN_TYPE.KITCHEN_TYPE_ID.eq(kitchenTypeId))
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

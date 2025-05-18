package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.PriceCategory;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.PriceCategoryRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class PriceCategoryRepository {

    private final DSLContext dsl;

    public PriceCategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PriceCategoryRecord findById(int id) {
        return dsl.selectFrom(PriceCategory.PRICE_CATEGORY)
                .where(PriceCategory.PRICE_CATEGORY.PRICE_CATEGORY_ID.eq(id))
                .fetchOne();
    }

    public PriceCategoryRecord findByName(String name) {
        return dsl.selectFrom(PriceCategory.PRICE_CATEGORY)
                .where(PriceCategory.PRICE_CATEGORY.NAME.eq(name))
                .fetchOne();
    }

    public List<PriceCategoryRecord> findAll() {
        return dsl.selectFrom(PriceCategory.PRICE_CATEGORY).fetch();
    }
}

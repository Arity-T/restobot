package dev.tishenko.restobot.logic.repository;

import dev.tishenko.restobot.logic.jooq.generated.tables.City;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.CityRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class CityRepository {
    private final DSLContext dsl;

    public CityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public CityRecord findById(int cityId) {
        return dsl.selectFrom(City.CITY)
                .where(City.CITY.CITY_ID.eq(cityId))
                .fetchOne(); // вернёт null, если не найден
    }

    public List<CityRecord> findAll() {
        return dsl.selectFrom(City.CITY).fetch();
    }

    public CityRecord findByName(String name) {
        return dsl.selectFrom(City.CITY)
                .where(City.CITY.NAME.eq(name))
                .fetchOne(); // вернёт null, если не найден
    }
}

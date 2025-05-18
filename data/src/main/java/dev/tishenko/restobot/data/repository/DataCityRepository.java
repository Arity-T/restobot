package dev.tishenko.restobot.data.repository;

import java.util.List;
import org.example.jooq.generated.tables.City;
import org.example.jooq.generated.tables.records.CityRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class DataCityRepository {
    private final DSLContext dsl;

    public DataCityRepository(DSLContext dsl) {
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

    public String findNameById(int cityId) {
        return dsl.select(City.CITY.NAME)
                .from(City.CITY)
                .where(City.CITY.CITY_ID.eq(cityId))
                .fetchOne(City.CITY.NAME);
    }
}

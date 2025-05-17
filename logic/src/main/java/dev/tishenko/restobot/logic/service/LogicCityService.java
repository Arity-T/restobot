package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.LogicCityRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.CityRecord;
import org.springframework.stereotype.Service;

@Service
public class LogicCityService {

    private final LogicCityRepository repo;

    public LogicCityService(LogicCityRepository repo) {
        this.repo = repo;
    }

    public Optional<CityRecord> getCityById(int id) {
        return Optional.ofNullable(repo.findById(id));
    }

    public List<CityRecord> getAllCities() {
        return repo.findAll();
    }

    public Optional<CityRecord> getCityByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }
}

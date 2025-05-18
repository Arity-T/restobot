package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.CityRecord;
import dev.tishenko.restobot.logic.repository.CityRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CityService {

    private final CityRepository repo;

    public CityService(CityRepository repo) {
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

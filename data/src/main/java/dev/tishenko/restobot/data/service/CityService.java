package dev.tishenko.restobot.data.service;

import dev.tishenko.restobot.data.repository.CityRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.CityRecord;
import org.springframework.stereotype.Service;

@Service
public class CityService {

    private final CityRepository repo;

    public CityService(CityRepository repo) {
        this.repo = repo;
    }

    public Optional<CityRecord> getCityById(int cityId) {
        return Optional.ofNullable(repo.findById(cityId));
    }

    public List<CityRecord> getAllCities() {
        return repo.findAll();
    }

    public Optional<CityRecord> getCityByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }

    public String getCityNameById(int cityId) {
        return repo.findNameById(cityId);
    }
}

package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.CityRecord;
import dev.tishenko.restobot.logic.repository.CityRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    private final CityRepository repo;

    public CityService(CityRepository repo) {
        this.repo = repo;
        logger.info("CityService initialized");
    }

    public Optional<CityRecord> getCityById(int id) {
        logger.debug("Fetching city by id: {}", id);
        return Optional.ofNullable(repo.findById(id));
    }

    public List<CityRecord> getAllCities() {
        logger.debug("Fetching all cities");
        return repo.findAll();
    }

    public Optional<CityRecord> getCityByName(String name) {
        logger.debug("Fetching city by name: {}", name);
        return Optional.ofNullable(repo.findByName(name));
    }
}

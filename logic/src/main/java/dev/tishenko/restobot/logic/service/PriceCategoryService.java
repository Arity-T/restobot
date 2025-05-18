package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.PriceCategoryRecord;
import dev.tishenko.restobot.logic.repository.PriceCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PriceCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(PriceCategoryService.class);
    private final PriceCategoryRepository repo;

    public PriceCategoryService(PriceCategoryRepository repo) {
        this.repo = repo;
        logger.info("PriceCategoryService initialized");
    }

    public Optional<PriceCategoryRecord> getById(int id) {
        logger.debug("Fetching price category by id: {}", id);
        return Optional.ofNullable(repo.findById(id));
    }

    public Optional<PriceCategoryRecord> getByName(String name) {
        logger.debug("Fetching price category by name: {}", name);
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<PriceCategoryRecord> getAll() {
        logger.debug("Fetching all price categories");
        return repo.findAll();
    }
}

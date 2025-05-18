package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.KitchenTypeRecord;
import dev.tishenko.restobot.logic.repository.KitchenTypeRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KitchenTypeService {

    private static final Logger logger = LoggerFactory.getLogger(KitchenTypeService.class);
    private final KitchenTypeRepository repo;

    public KitchenTypeService(KitchenTypeRepository repo) {
        this.repo = repo;
        logger.info("KitchenTypeService initialized");
    }

    public Optional<KitchenTypeRecord> getById(int id) {
        logger.debug("Fetching kitchen type by id: {}", id);
        return Optional.ofNullable(repo.findById(id));
    }

    public Optional<KitchenTypeRecord> getByName(String name) {
        logger.debug("Fetching kitchen type by name: {}", name);
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<KitchenTypeRecord> getAll() {
        logger.debug("Fetching all kitchen types");
        return repo.findAll();
    }
}

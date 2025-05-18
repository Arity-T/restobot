package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.KitchenTypeRecord;
import dev.tishenko.restobot.logic.repository.LogicKitchenTypeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LogicKitchenTypeService {

    private final LogicKitchenTypeRepository repo;

    public LogicKitchenTypeService(LogicKitchenTypeRepository repo) {
        this.repo = repo;
    }

    public Optional<KitchenTypeRecord> getById(int id) {
        return Optional.ofNullable(repo.findById(id));
    }

    public Optional<KitchenTypeRecord> getByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<KitchenTypeRecord> getAll() {
        return repo.findAll();
    }
}

package dev.tishenko.restobot.data.service;

import dev.tishenko.restobot.data.repository.KitchenTypeRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.KitchenTypeRecord;
import org.springframework.stereotype.Service;

@Service
public class KitchenTypeService {

    private final KitchenTypeRepository repo;

    public KitchenTypeService(KitchenTypeRepository repo) {
        this.repo = repo;
    }

    public Optional<KitchenTypeRecord> getById(int kitchenTypeId) {
        return Optional.ofNullable(repo.findById(kitchenTypeId));
    }

    public Optional<KitchenTypeRecord> getByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<KitchenTypeRecord> getAll() {
        return repo.findAll();
    }
}

package dev.tishenko.restobot.logic.service;

import java.util.List;
import java.util.Optional;

import org.example.jooq.generated.tables.records.KitchenTypeRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.logic.repository.KitchenTypeRepository;

@Service
public class KitchenTypeService {

    private final KitchenTypeRepository repo;

    public KitchenTypeService(KitchenTypeRepository repo) {
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

package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.PriceCategoryRecord;
import dev.tishenko.restobot.logic.repository.PriceCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PriceCategoryService {

    private final PriceCategoryRepository repo;

    public PriceCategoryService(PriceCategoryRepository repo) {
        this.repo = repo;
    }

    public Optional<PriceCategoryRecord> getById(int id) {
        return Optional.ofNullable(repo.findById(id));
    }

    public Optional<PriceCategoryRecord> getByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<PriceCategoryRecord> getAll() {
        return repo.findAll();
    }
}

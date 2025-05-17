package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.LogicPriceCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.PriceCategoryRecord;
import org.springframework.stereotype.Service;

@Service
public class LogicPriceCategoryService {

    private final LogicPriceCategoryRepository repo;

    public LogicPriceCategoryService(LogicPriceCategoryRepository repo) {
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

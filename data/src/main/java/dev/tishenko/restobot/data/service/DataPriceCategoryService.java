package dev.tishenko.restobot.data.service;

import dev.tishenko.restobot.data.repository.DataPriceCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.example.jooq.generated.tables.records.PriceCategoryRecord;
import org.springframework.stereotype.Service;

@Service
public class DataPriceCategoryService {

    private final DataPriceCategoryRepository repo;

    public DataPriceCategoryService(DataPriceCategoryRepository repo) {
        this.repo = repo;
    }

    public Optional<PriceCategoryRecord> getById(int priceCategoryId) {
        return Optional.ofNullable(repo.findById(priceCategoryId));
    }

    public Optional<PriceCategoryRecord> getByName(String name) {
        return Optional.ofNullable(repo.findByName(name));
    }

    public List<PriceCategoryRecord> getAll() {
        return repo.findAll();
    }
}

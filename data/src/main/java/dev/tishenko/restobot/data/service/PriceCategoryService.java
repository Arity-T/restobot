package dev.tishenko.restobot.data.service;

import java.util.List;
import java.util.Optional;

import org.example.jooq.generated.tables.records.PriceCategoryRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.data.repository.PriceCategoryRepository;

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
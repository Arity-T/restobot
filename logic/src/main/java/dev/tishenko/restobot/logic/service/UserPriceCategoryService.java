package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.repository.UserPriceCategoryRepository;
import org.example.jooq.generated.tables.records.UserPriceCategoryRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPriceCategoryService {

    private final UserPriceCategoryRepository repo;

    public UserPriceCategoryService(UserPriceCategoryRepository repo) {
        this.repo = repo;
    }

    public void addPriceCategory(long chatId, int categoryId) {
        repo.addPriceCategory(chatId, categoryId);
    }

    public void removePriceCategory(long chatId, int categoryId) {
        repo.removePriceCategory(chatId, categoryId);
    }

    public List<UserPriceCategoryRecord> getAllForUser(long chatId) {
        return repo.getAllByUser(chatId);
    }
}

package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.UserPriceCategoryRecord;
import dev.tishenko.restobot.logic.repository.UserPriceCategoryRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserPriceCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(UserPriceCategoryService.class);
    private final UserPriceCategoryRepository repo;

    public UserPriceCategoryService(UserPriceCategoryRepository repo) {
        this.repo = repo;
        logger.info("UserPriceCategoryService initialized");
    }

    public void addPriceCategory(long chatId, int categoryId) {
        logger.debug("Adding price category {} for user {}", categoryId, chatId);
        repo.addPriceCategory(chatId, categoryId);
    }

    public void removePriceCategory(long chatId, int categoryId) {
        logger.debug("Removing price category {} for user {}", categoryId, chatId);
        repo.removePriceCategory(chatId, categoryId);
    }

    public void removeAllByUser(long chatId) {
        logger.debug("Removing all price categories for user {}", chatId);
        repo.removeAllByUser(chatId);
    }

    public List<UserPriceCategoryRecord> getAllForUser(long chatId) {
        logger.debug("Fetching all price categories for user {}", chatId);
        return repo.getAllByUser(chatId);
    }
}

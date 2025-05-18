package dev.tishenko.restobot.logic.service;

import dev.tishenko.restobot.logic.jooq.generated.tables.records.UserKitchenTypeRecord;
import dev.tishenko.restobot.logic.repository.UserKitchenTypeRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserKitchenTypeService {

    private static final Logger logger = LoggerFactory.getLogger(UserKitchenTypeService.class);
    private final UserKitchenTypeRepository repo;

    public UserKitchenTypeService(UserKitchenTypeRepository repo) {
        this.repo = repo;
        logger.info("UserKitchenTypeService initialized");
    }

    public void addKitchen(long chatId, int kitchenTypeId) {
        logger.debug("Adding kitchen type {} for user {}", kitchenTypeId, chatId);
        repo.addKitchen(chatId, kitchenTypeId);
    }

    public void removeKitchen(long chatId, int kitchenTypeId) {
        logger.debug("Removing kitchen type {} for user {}", kitchenTypeId, chatId);
        repo.removeKitchen(chatId, kitchenTypeId);
    }

    public void removeAllByUser(long chatId) {
        logger.debug("Removing all kitchen types for user {}", chatId);
        repo.removeAllByUser(chatId);
    }

    public List<UserKitchenTypeRecord> getAllForUser(long chatId) {
        logger.debug("Fetching all kitchen types for user {}", chatId);
        return repo.getAllByUser(chatId);
    }
}

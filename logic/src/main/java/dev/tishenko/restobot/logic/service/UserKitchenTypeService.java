package dev.tishenko.restobot.logic.service;

import java.util.List;

import org.example.jooq.generated.tables.records.UserKitchenTypeRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.logic.repository.UserKitchenTypeRepository;

@Service
public class UserKitchenTypeService {

    private final UserKitchenTypeRepository repo;

    public UserKitchenTypeService(UserKitchenTypeRepository repo) {
        this.repo = repo;
    }

    public void addKitchen(long chatId, int kitchenTypeId) {
        repo.addKitchen(chatId, kitchenTypeId);
    }

    public void removeKitchen(long chatId, int kitchenTypeId) {
        repo.removeKitchen(chatId, kitchenTypeId);
    }

    public List<UserKitchenTypeRecord> getAllForUser(long chatId) {
        return repo.getAllByUser(chatId);
    }
}

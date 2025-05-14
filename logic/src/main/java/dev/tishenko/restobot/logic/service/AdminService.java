package dev.tishenko.restobot.logic.service;

import java.util.Optional;

import org.example.jooq.generated.tables.records.AdminDataRecord;
import org.springframework.stereotype.Service;

import dev.tishenko.restobot.logic.repository.AdminDataRepository;

@Service
public class AdminService {

    private final AdminDataRepository repo;

    public AdminService(AdminDataRepository repo) {
        this.repo = repo;
    }

    public void saveAdminKey(byte[] hash, byte[] salt) {
        repo.insertAdminKey(hash, salt);
    }

    public Optional<AdminDataRecord> getByHash(byte[] hash) {
        return Optional.ofNullable(repo.findByHash(hash));
    }
}

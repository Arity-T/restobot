package dev.tishenko.restobot.logic.service.api;

import com.google.common.hash.Hashing;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.logic.repository.AdminDataRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.jooq.generated.tables.records.AdminDataRecord;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyValidatorImpl implements ApiKeyValidator {

    private final AdminDataRepository repo;

    public ApiKeyValidatorImpl(AdminDataRepository repo) {
        this.repo = repo;
    }

    public List<AdminDataRecord> getAllAdminKeys() {
        return repo.findAll();
    }

    @Override
    public boolean isValidApiKey(String apiKey) {
        List<AdminDataRecord> adminKeys = repo.findAll();
        ;

        for (AdminDataRecord adminKey : adminKeys) {
            String saltedKey = adminKey.getSalt() + apiKey;
            String hashedKey = hashWithSHA256(saltedKey);

            if (hashedKey.equals(adminKey.getHash())) {
                return true;
            }
        }

        return false;
    }

    private String hashWithSHA256(String input) {
        return Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
    }
}

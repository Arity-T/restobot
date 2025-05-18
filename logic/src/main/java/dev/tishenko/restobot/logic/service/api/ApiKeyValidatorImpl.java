package dev.tishenko.restobot.logic.service.api;

import com.google.common.hash.Hashing;
import dev.tishenko.restobot.api.service.ApiKeyValidator;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.AdminDataRecord;
import dev.tishenko.restobot.logic.repository.AdminDataRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyValidatorImpl implements ApiKeyValidator {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyValidatorImpl.class);
    private final AdminDataRepository repo;

    public ApiKeyValidatorImpl(AdminDataRepository repo) {
        this.repo = repo;
        logger.info("ApiKeyValidatorImpl initialized");
    }

    public List<AdminDataRecord> getAllAdminKeys() {
        logger.debug("Fetching all admin keys");
        return repo.findAll();
    }

    @Override
    public boolean isValidApiKey(String apiKey) {
        logger.debug("Validating API key");
        List<AdminDataRecord> adminKeys = repo.findAll();

        for (AdminDataRecord adminKey : adminKeys) {
            String saltedKey = adminKey.getSalt() + apiKey;
            String hashedKey = hashWithSHA256(saltedKey);

            if (hashedKey.equals(adminKey.getHash())) {
                logger.debug("API key validation successful");
                return true;
            }
        }

        logger.debug("API key validation failed");
        return false;
    }

    private String hashWithSHA256(String input) {
        logger.trace("Hashing input with SHA-256");
        return Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
    }
}

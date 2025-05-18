package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.integration.tripadvisor.TripAdvisorClient;
import dev.tishenko.restobot.integration.tripadvisor.model.LocationDetails;
import dev.tishenko.restobot.logic.jooq.generated.tables.records.FavoriteRestaurantRecord;
import dev.tishenko.restobot.logic.repository.FavoriteRestaurantRepository;
import dev.tishenko.restobot.telegram.services.FavoriteListDAO;
import dev.tishenko.restobot.telegram.services.FavoriteRestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FavoriteListDAOImpl implements FavoriteListDAO {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteListDAOImpl.class);
    private final FavoriteRestaurantRepository favoriteRestaurantRepository;
    private final TripAdvisorClient tripAdvisorClient;

    public FavoriteListDAOImpl(
            FavoriteRestaurantRepository favoriteRestaurantRepository,
            TripAdvisorClient tripAdvisorClient) {
        this.favoriteRestaurantRepository = favoriteRestaurantRepository;
        this.tripAdvisorClient = tripAdvisorClient;
        logger.info("FavoriteListDAOImpl initialized");
    }

    @Override
    public void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        logger.debug(
                "Adding restaurant with ID {} to favorites for user {}", tripadvisorId, chatId);
        favoriteRestaurantRepository.addRestaurant(chatId, tripadvisorId);
    }

    @Override
    public void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        logger.debug(
                "Removing restaurant with ID {} from favorites for user {}", tripadvisorId, chatId);
        favoriteRestaurantRepository.delete(chatId, tripadvisorId);
    }

    @Override
    public void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited) {
        logger.debug(
                "Setting visited status to {} for restaurant ID {} for user {}",
                isVisited,
                tripadvisorId,
                chatId);
        favoriteRestaurantRepository.markVisited(chatId, tripadvisorId, isVisited);
    }

    public List<FavoriteRestaurantCardDTO> getFavoriteList(long chatId) {
        logger.debug("Getting favorite list for user {}", chatId);
        List<FavoriteRestaurantRecord> favRecords =
                favoriteRestaurantRepository.getAllFavorites(chatId);
        if (favRecords.isEmpty()) {
            logger.debug("No favorite restaurants found for user {}", chatId);
            return List.of();
        }

        logger.debug("Found {} favorite restaurants for user {}", favRecords.size(), chatId);
        return favRecords.stream()
                .map(
                        record -> {
                            logger.debug(
                                    "Fetching details for restaurant ID: {}",
                                    record.getTripadvisorId());
                            LocationDetails details =
                                    tripAdvisorClient
                                            .getLocationDetails(record.getTripadvisorId())
                                            .block(); // Блокируем выполнение, так как метод должен
                            // быть синхронным

                            if (details == null) {
                                logger.warn(
                                        "Could not fetch details for restaurant ID: {}",
                                        record.getTripadvisorId());
                                return new FavoriteRestaurantCardDTO(
                                        new RestaurantCardDTO(
                                                record.getTripadvisorId(),
                                                "",
                                                "",
                                                0.0,
                                                null,
                                                "",
                                                0.0,
                                                0.0,
                                                ""),
                                        record.getIsVisited());
                            }

                            URL websiteUrl = null;
                            try {
                                websiteUrl =
                                        details.getWebsite() != null
                                                ? new URI(details.getWebsite()).toURL()
                                                : null;
                            } catch (MalformedURLException | URISyntaxException e) {
                                logger.warn(
                                        "Invalid website URL for restaurant {}: {}",
                                        details.getName(),
                                        details.getWebsite());
                                // ignore, leave as null
                            }

                            RestaurantCardDTO card =
                                    new RestaurantCardDTO(
                                            details.getLocationId(),
                                            details.getName(),
                                            details.getAddressObj().getAddressString(),
                                            details.getRating() != null ? details.getRating() : 0.0,
                                            websiteUrl,
                                            details.getDescription(),
                                            details.getLatitude() != null
                                                    ? details.getLatitude()
                                                    : 0.0,
                                            details.getLongitude() != null
                                                    ? details.getLongitude()
                                                    : 0.0,
                                            details.getAddressObj().getCity());

                            return new FavoriteRestaurantCardDTO(card, record.getIsVisited());
                        })
                .collect(Collectors.toList());
    }

    public List<FavoriteRestaurantRecord> getFavoriteListRecords(long chatId) {
        logger.debug("Getting favorite list records for user {}", chatId);
        return favoriteRestaurantRepository.getAllFavorites(chatId);
    }

    // Legacy methods for backward compatibility
    public FavoriteRestaurantRecord addRestaurant(long chatId, int tripadvisorId) {
        logger.debug(
                "Legacy: Adding restaurant with ID {} to favorites for user {}",
                tripadvisorId,
                chatId);
        return favoriteRestaurantRepository.addRestaurant(chatId, tripadvisorId);
    }

    public void setVisited(long chatId, int tripadvisorId, boolean visited) {
        logger.debug(
                "Legacy: Setting visited status to {} for restaurant ID {} for user {}",
                visited,
                tripadvisorId,
                chatId);
        favoriteRestaurantRepository.markVisited(chatId, tripadvisorId, visited);
    }

    public void removeFromFavorites(long chatId, int tripadvisorId) {
        logger.debug(
                "Legacy: Removing restaurant with ID {} from favorites for user {}",
                tripadvisorId,
                chatId);
        favoriteRestaurantRepository.delete(chatId, tripadvisorId);
    }

    public List<FavoriteRestaurantRecord> getAll(long chatId) {
        logger.debug("Legacy: Getting all favorite restaurants for user {}", chatId);
        return favoriteRestaurantRepository.getAllFavorites(chatId);
    }

    public List<FavoriteRestaurantRecord> getByVisitStatus(long chatId, boolean visited) {
        logger.debug(
                "Legacy: Getting {} restaurants for user {}",
                visited ? "visited" : "unvisited",
                chatId);
        return favoriteRestaurantRepository.getVisited(chatId, visited);
    }
}

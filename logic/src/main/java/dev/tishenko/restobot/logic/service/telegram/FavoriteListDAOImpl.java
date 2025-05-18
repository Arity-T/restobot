package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.data.service.DataCityService;
import dev.tishenko.restobot.data.service.RestaurantService;
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
import java.util.Map;
import java.util.stream.Collectors;
import org.example.jooq.generated.tables.records.RestaurantRecord;
import org.springframework.stereotype.Service;

@Service
public class FavoriteListDAOImpl implements FavoriteListDAO {

    private final FavoriteRestaurantRepository repo;
    private final RestaurantService restaurantService;
    private final DataCityService cityService;

    public FavoriteListDAOImpl(
            FavoriteRestaurantRepository repo,
            RestaurantService restaurantService,
            DataCityService cityService) {
        this.repo = repo;
        this.restaurantService = restaurantService;
        this.cityService = cityService;
    }

    @Override
    public void addRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        repo.addRestaurant(chatId, tripadvisorId);
    }

    @Override
    public void removeRestaurantCardToFavoriteList(long chatId, int tripadvisorId) {
        repo.delete(chatId, tripadvisorId);
    }

    @Override
    public void setVisitedStatus(long chatId, int tripadvisorId, boolean isVisited) {
        repo.markVisited(chatId, tripadvisorId, isVisited);
    }

    public List<FavoriteRestaurantCardDTO> getFavoriteList(long chatId) {
        List<FavoriteRestaurantRecord> favRecords = repo.getAllFavorites(chatId);
        List<Integer> tripadvisorIds =
                favRecords.stream()
                        .map(FavoriteRestaurantRecord::getTripadvisorId)
                        .collect(Collectors.toList());
        if (tripadvisorIds.isEmpty()) {
            return List.of();
        }
        // Получаем рестораны из кэша
        List<RestaurantRecord> restaurantRecords =
                restaurantService.getByTripadvisorIds(tripadvisorIds);
        // Преобразуем в словарь (tripadvisorId -> RestaurantRecord)
        Map<Integer, RestaurantRecord> restaurantMap =
                restaurantRecords.stream()
                        .collect(Collectors.toMap(RestaurantRecord::getTripadvisorId, r -> r));
        return favRecords.stream()
                .map(
                        record -> {
                            RestaurantRecord rest = restaurantMap.get(record.getTripadvisorId());
                            RestaurantCardDTO card;
                            if (rest == null) {
                                card =
                                        new RestaurantCardDTO(
                                                record.getTripadvisorId(),
                                                "",
                                                "",
                                                0.0,
                                                null,
                                                "",
                                                0.0,
                                                0.0,
                                                "");
                            } else {
                                URL websiteUrl = null;
                                try {
                                    websiteUrl =
                                            rest.getWebsite() != null
                                                    ? new URI(rest.getWebsite()).toURL()
                                                    : null;
                                } catch (MalformedURLException | URISyntaxException e) {
                                    // ignore, leave as null
                                }
                                card =
                                        new RestaurantCardDTO(
                                                rest.getTripadvisorId(),
                                                rest.getName(),
                                                rest.getAddressString(),
                                                rest.getRating() != null
                                                        ? rest.getRating().doubleValue()
                                                        : 0.0,
                                                websiteUrl,
                                                rest.getDescription(),
                                                rest.getLatitude() != null
                                                        ? rest.getLatitude()
                                                        : 0.0,
                                                rest.getLongitude() != null
                                                        ? rest.getLongitude()
                                                        : 0.0,
                                                cityService.getCityNameById(rest.getCityId()));
                            }
                            return new FavoriteRestaurantCardDTO(card, record.getIsVisited());
                        })
                .collect(Collectors.toList());
    }

    public List<FavoriteRestaurantRecord> getFavoriteListRecords(long chatId) {
        return repo.getAllFavorites(chatId);
    }

    // Legacy methods for backward compatibility
    public FavoriteRestaurantRecord addRestaurant(long chatId, int tripadvisorId) {
        return repo.addRestaurant(chatId, tripadvisorId);
    }

    public void setVisited(long chatId, int tripadvisorId, boolean visited) {
        repo.markVisited(chatId, tripadvisorId, visited);
    }

    public void removeFromFavorites(long chatId, int tripadvisorId) {
        repo.delete(chatId, tripadvisorId);
    }

    public List<FavoriteRestaurantRecord> getAll(long chatId) {
        return repo.getAllFavorites(chatId);
    }

    public List<FavoriteRestaurantRecord> getByVisitStatus(long chatId, boolean visited) {
        return repo.getVisited(chatId, visited);
    }
}

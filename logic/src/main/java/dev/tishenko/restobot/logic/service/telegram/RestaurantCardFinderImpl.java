package dev.tishenko.restobot.logic.service.telegram;

import dev.tishenko.restobot.logic.repository.RestaurantRepository;
import dev.tishenko.restobot.telegram.services.RestaurantCardDTO;
import dev.tishenko.restobot.telegram.services.RestaurantCardFinder;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RestaurantCardFinderImpl implements RestaurantCardFinder {

    private final RestaurantRepository restaurantRepository;

    public RestaurantCardFinderImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByGeolocation(double latitude, double longitude)
            throws MalformedURLException, URISyntaxException {
        return restaurantRepository.findByGeolocation(latitude, longitude);
    }

    @Override
    public List<RestaurantCardDTO> getRestaurantCardByParams(
            String city,
            List<String> kitchenTypes,
            List<String> priceCategories,
            List<String> keyWords)
            throws MalformedURLException, URISyntaxException {
        return restaurantRepository.findByParams(city, kitchenTypes, priceCategories, keyWords);
    }
}

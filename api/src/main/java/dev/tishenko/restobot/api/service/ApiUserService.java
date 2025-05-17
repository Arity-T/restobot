package dev.tishenko.restobot.api.service;

import java.util.List;
import java.util.Map;

/** Service for retrieving user information. */
public interface ApiUserService {

    /**
     * Gets list of users with their information.
     *
     * @return A list of maps containing user information
     */
    List<Map<String, String>> getUsers();
}

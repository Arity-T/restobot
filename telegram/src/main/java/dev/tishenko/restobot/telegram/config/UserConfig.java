package dev.tishenko.restobot.telegram.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class UserConfig {
    private final int chatID;
    private final String nickName;
    private final int favoriteListId;
    private String city;
    private List<String> keyWords;

    public UserConfig(int chatID, String nickName, int favoriteListId) {
        this.chatID = chatID;
        this.nickName = nickName;
        this.favoriteListId = favoriteListId;
    }
}

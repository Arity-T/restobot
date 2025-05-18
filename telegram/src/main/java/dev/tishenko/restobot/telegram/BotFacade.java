package dev.tishenko.restobot.telegram;

/**
 * Interface for interacting with a Telegram bot without requiring dependencies on the Telegram
 * library. Serves as a facade for RestoBot.
 */
public interface BotFacade {
    /** Starts the Telegram bot. */
    void start();
}

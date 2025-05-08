package dev.tishenko.restobot.telegram.RestoBot;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RestoBot implements LongPollingUpdateConsumer {
    private Executor updatesProcessorExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final TelegramClient telegramClient;

    public RestoBot(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }


    @Override
    public void consume(List<Update> updates) {

    }
}

package com.example.bot;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class WeatherBotApplication {

    private static final Logger LOGGER = LogManager.getLogger(WeatherBotApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Creating Bot...");
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new MyWeatherTgBot(new DefaultBotOptions()));
            LOGGER.info("My weather bot is ready for work!");
        } catch (TelegramApiException e) {
            LOGGER.error("Error while initializing bot! " + e.getMessage());
            e.printStackTrace();
        }
    }

}

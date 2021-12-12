package com.example.bot.commands;

import com.example.bot.utils.DBUser;
import com.example.bot.utils.Weather;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.example.bot.commands.LocationCommand.getKeyboardLoc;
import static com.example.bot.commands.WeatherCommand.getKeyboardWeatherNow;

public class BadWeatherCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(BadWeatherCommand.class);

    public BadWeatherCommand() {
        super("bad_weather",
                "determines the nearest bad weather (rain, snow, strong wind, etc.) for your location \n");
    }

    public static SendMessage getMessageRemind(Long user_id, Long chat_id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_id));
        LOGGER.info("Bad weather ...");

        DBUser base = new DBUser();
        base.updateCurrentMessage(user_id, "/bad_weather");
        message.setChatId(String.valueOf(chat_id));
        if (base.isUserId(user_id)) {
            double lat = base.getLatUser(user_id);
            double lon = base.getLonUser(user_id);

            Weather weather = new Weather(lat, lon);
            String[] parseWeather = weather.getWeather3H();
            String date = parseWeather[5];

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(getKeyboardWeatherNow());
            markupInline.setKeyboard(rowsInline);

            message.setText("Get ready! At " + date + " expected: \n" + weather.toWrap(parseWeather));
            message.setReplyMarkup(markupInline);
        } else {
            message
                    .setText("I can't tell the weather, if I don't know where are you. " + "I need your coordinates");
            message.setReplyMarkup(getKeyboardLoc());
        }
        return message;
    }

    public static List<InlineKeyboardButton> getKeyboardWorseWeather() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("When get worse?");
        button.setCallbackData("bad_weather");
        rowInline.add(button);
        return rowInline;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/reminder' command from @" + user.getUserName() + "...");

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message = getMessageRemind(user.getId(), chat.getId());

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }
}

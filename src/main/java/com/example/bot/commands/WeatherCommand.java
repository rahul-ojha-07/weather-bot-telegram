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

import static com.example.bot.commands.BadWeatherCommand.getKeyboardWorseWeather;
import static com.example.bot.commands.LocateCommand.getKeyboardLoc;


public class WeatherCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(WeatherCommand.class);

    public WeatherCommand() {
        super("weather", "determines the current weather by coordinate now \n");
    }

    public static SendMessage getMessageWeatherNow(Long user_id, Long chat_id) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_id));
        DBUser base = new DBUser();
        if (base.isUserId(user_id)) {
            base.updateCurrentMessage(user_id, "/weather");
            Weather weather = new Weather(base.getLatUser(user_id), base.getLonUser(user_id));
            String[] parseWeather = weather.getWeatherNow();

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(getKeyboardWorseWeather());
            markupInline.setKeyboard(rowsInline);

            message.setText("The weather now: \n" + weather.toWrap(parseWeather));
            message.setReplyMarkup(markupInline);

        } else {
            message
                    .setText("I can't tell the weather, if I don't know where are you. " + "I need your coordinates");
            message.setReplyMarkup(getKeyboardLoc());
        }
        return message;
    }

    public static List<InlineKeyboardButton> getKeyboardWeatherNow() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Weather now?");
        button.setCallbackData("weather");
        rowInline.add(button);
        return rowInline;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/weather' command from @" + user.getUserName() + "...");

        SendMessage message;
        Long user_id = user.getId();
        message = getMessageWeatherNow(user_id, chat.getId());

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }
}

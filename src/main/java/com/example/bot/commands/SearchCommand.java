package com.example.bot.commands;

import com.example.bot.utils.DBUser;
import com.example.bot.utils.Weather;
import com.vdurmont.emoji.EmojiParser;
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


public class SearchCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(SearchCommand.class);

    public SearchCommand() {
        super("search", "search for a city\n");
    }

    public static SendMessage getMessageWeatherPlace(Long user_id, Long chat_id, String place) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_id));
        DBUser base = new DBUser();
        if (base.isUserId(user_id)) {
            base.setUserID(user_id);
        }

        base.updateCurrentMessage(user_id, "/search");
        Weather weather = new Weather(place);
        String[] parseWeather = weather.getWeatherPlace();

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(getKeyboardSetLocation(place));
        markupInline.setKeyboard(rowsInline);
        if (parseWeather.length == 2) {
            message.setText(weather.errorWrap(parseWeather));
            return message;
        }
        message.setText("The weather now: \n" + weather.toWrap(parseWeather));
        message.setReplyMarkup(markupInline);
        return message;

    }

    public static List<InlineKeyboardButton> getKeyboardSetLocation(String place) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Set As Current Location?");
        button.setCallbackData("location " + place);
        rowInline.add(button);
        return rowInline;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/search' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        DBUser base = new DBUser();
        sb.append("Enter the City Name Below").append(EmojiParser.parseToUnicode(":point_down: \n"));
        message.setText(sb.toString());
        base.updateCurrentMessage(user.getId(), "/search");
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/search' command", e);
        }
    }
}

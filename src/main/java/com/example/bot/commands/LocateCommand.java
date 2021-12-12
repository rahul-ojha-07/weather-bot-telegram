package com.example.bot.commands;

import com.example.bot.utils.DBUser;
import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static com.example.bot.commands.BadWeatherCommand.getKeyboardWorseWeather;
import static com.example.bot.commands.WeatherCommand.getKeyboardWeatherNow;


public class LocateCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(LocateCommand.class);

    public LocateCommand() {
        super("location", "I set/change your location for future work \n");
    }

    public static void setLocation(Long user_id, String userName, double lat, double lon) {
        DBUser base = new DBUser();
        if (!base.isUserId(user_id)) {
            base.setUserID(user_id);
        }
        base.updateUserName(user_id, userName);
        base.updateLatUser(lat, user_id);
        base.updateLonUser(lon, user_id);
        base.updateCurrentMessage(user_id, "/location");
    }

    public static double getLat(Update update) {
        Location location = update.getMessage().getLocation();
        LOGGER.info("Get lat from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

        return location.getLatitude();
    }

    public static double getLon(Update update) {
        Location location = update.getMessage().getLocation();
        LOGGER.info("Get lon from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

        return location.getLongitude();
    }

    public static SendMessage location(Update update) {
        double lat = getLat(update);
        double lon = getLon(update);
        Long user_id = update.getMessage().getFrom().getId();
        setLocation(user_id, update.getMessage().getFrom().getUserName(), lat, lon);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(getKeyboardWorseWeather());
        rowsInline.add(getKeyboardWeatherNow());
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setText("I save your location! You can more activity, see /help");
        message.setReplyMarkup(markupInline);
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        return message;
    }

    public static ReplyKeyboardMarkup getKeyboardLoc() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("I'm here!");
        keyboardButton.setRequestLocation(true);
        keyboardFirstRow.add(keyboardButton);
        keyboard.add(keyboardFirstRow);

        // KeyboardRow keyboardSecondRow = new KeyboardRow();
        // keyboardSecondRow.add(new KeyboardButton("city"));
        // keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/weather' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        (new DBUser()).updateCurrentMessage(user.getId(), "/location");
        sb.append("Send me your location").append(EmojiParser.parseToUnicode(":point_down: \n"));

        message.setReplyMarkup(getKeyboardLoc());
        message.setText(sb.toString());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/location' command", e);
        }
    }
}

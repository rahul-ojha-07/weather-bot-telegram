package com.example.bot;

import com.example.bot.commands.*;
import com.example.bot.utils.BotProperties;
import com.example.bot.utils.DBUser;
import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.example.bot.commands.BadWeatherCommand.getMessageRemind;
import static com.example.bot.commands.LocateCommand.location;
import static com.example.bot.commands.WeatherCommand.getMessageWeatherNow;

public class MyWeatherTgBot extends TelegramLongPollingCommandBot {
    private static final String BOT_TOKEN = BotProperties.getProperty("BOT_TOKEN");
    private static final String BOT_NAME = BotProperties.getProperty("BOT_NAME");

    private static final Logger LOGGER = LogManager.getLogger(MyWeatherTgBot.class);

    public MyWeatherTgBot(DefaultBotOptions botOptions) {
        super(botOptions);
        LOGGER.info("Initializing My Weather bot...");

        LOGGER.info("Registering commands...");
        LOGGER.info("Registering '/start'...");
        register(new StartCommand());
        LOGGER.info("Registering '/help'...");
        HelpCommand helpCommand = new HelpCommand(this);
        register(helpCommand);
        LOGGER.info("Registering '/stop'...");
        register(new StopCommand());
        LOGGER.info("Registering '/weather'...");
        register(new WeatherCommand());
        LOGGER.info("Registering '/bad_weather'...");
        register(new BadWeatherCommand());
        LOGGER.info("Registering '/location'...");
        register(new LocateCommand());
        LOGGER.info("Registering '/search'...");
        register(new SearchCommand());


        registerDefaultAction((absSender, message) -> {
            LOGGER.info("Registering unknown command from " + message.getFrom().getUserName() + " : "
                    + message.getText() + "...");

            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(String.valueOf(message.getChatId()));
            commandUnknownMessage
                    .setText("The command '" + message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in custom unregistered command " + e.getMessage(), e);
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[]{});
        });
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        LOGGER.info("processNonCommandUpdate...");
        SendMessage message = new SendMessage();

        // message generation
        if (update.hasCallbackQuery()) {
            /* callback from keyboard */
            LOGGER.info("Callback Query...");
            String call_data = update.getCallbackQuery().getData();
            Long user_id = update.getCallbackQuery().getFrom().getId();
            Long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("bad_weather")) {
                // bad weather
                LOGGER.info("When bad weather..");
                message = getMessageRemind(user_id, chat_id);
            } else if (call_data.equals("weather")) {
                // now weather
                LOGGER.info("Weather now...");
                message = getMessageWeatherNow(user_id, chat_id);
            } else if (call_data.startsWith("location")) {
                LOGGER.info("Set Location ...");
                String[] msgs = call_data.split("\\s+");
                (new DBUser()).setAsCurrentLocationPlace(user_id, msgs[1]);
                message.setChatId(String.valueOf(chat_id));
                message.setText("Updated your current location to : " + msgs[1]);
            }
        } else if (update.hasMessage()) {
            if (update.getMessage().hasLocation()) {
                message = location(update);
            } else if (update.getMessage().hasText()) {
                /* text message */

                String text = update.getMessage().getText();
                if ((new DBUser()).getLastMessage(update.getMessage().getFrom().getId()).equals("/search")) {
                    Long user_id = update.getMessage().getFrom().getId();
                    Long chat_id = update.getMessage().getChatId();
                    message = SearchCommand.getMessageWeatherPlace(user_id, chat_id, update.getMessage().getText());
                } else {
                    SendMessage messageNot = getMessageNot(update, text);
                    HelpCommand helpCommand = new HelpCommand(this);
                    message = helpCommand.getHelpMessage(update.getMessage().getChatId());
                    try {
                        execute(messageNot); // Call method to send the message
                    } catch (TelegramApiException e) {
                        LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
                    }
                }
            } else {
                /* we get someone else */
                SendMessage messageNot = getMessageNot(update);
                HelpCommand helpCommand = new HelpCommand(this);
                message = helpCommand.getHelpMessage(update.getMessage().getChatId());
                try {
                    execute(messageNot); // Call method to send the message;
                } catch (TelegramApiException e) {
                    LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
                }
            }
        }
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
        }
    }

    private SendMessage getMessageNot(Update update) {
        SendMessage message = new SendMessage();

        LOGGER.info("Executing non-custom update from without text!");

        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText("I don't know what to do with this" + EmojiParser.parseToUnicode(":cry: \n"));
        return message;
    }

    private SendMessage getMessageNot(Update update, String text) {
        SendMessage message = new SendMessage();

        LOGGER.info(
                "Executing non-custom update from @" + update.getMessage().getFrom().getUserName() + "without text!");

        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText(
                "You said: " + text + ", I don't know what to do with this" + EmojiParser.parseToUnicode(":cry: \n"));
        return message;
    }

    /* operations to be executed not in response to an update */
    public void sendNotification() {
        // TODO: do stuff for example send a notification to some user
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

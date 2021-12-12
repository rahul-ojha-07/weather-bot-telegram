package com.example.bot.commands;

import com.example.bot.utils.DBUser;
import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelpCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(HelpCommand.class);
    private final ICommandRegistry commandRegistry;

    public HelpCommand(ICommandRegistry iCommandRegistry) {
        super("help", "list all known commands\n");
        commandRegistry = iCommandRegistry;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/help' command from @" + user.getUserName() + "...");
        DBUser base = new DBUser();
        base.updateCurrentMessage(user.getId(), "/help");
        SendMessage helpMessage = getHelpMessage(chat.getId());
        try {
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/help' command", e);
        }
    }

    public SendMessage getHelpMessage(Long chat_id) {
        StringBuilder helpMessageBuilder = new StringBuilder();
        helpMessageBuilder.append("<b>What's commands I can do </b>").append(EmojiParser.parseToUnicode(":question: "));

        for (IBotCommand command : commandRegistry.getRegisteredCommands()) {
            if (!command.getCommandIdentifier().equals("start")) {
                helpMessageBuilder.append(EmojiParser.parseToUnicode("\n:red_circle: ")).append(command);
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setText(helpMessageBuilder.toString());
        sendMessage.setChatId(String.valueOf(chat_id));
        return sendMessage;
    }
}

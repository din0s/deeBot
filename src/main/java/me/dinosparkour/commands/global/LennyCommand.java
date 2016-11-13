package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;

public class LennyCommand extends GlobalCommand {

    private static final ArrayList<String> LENNY_LIST = new ArrayList<>(Arrays.asList(
            "( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)",
            "( \u0361\u2609 \u035c\u0296 \u0361\u2609)",
            "[ \u0361\u00b0 \u035c\u0296 \u0361\u00b0 ]",
            "(\u0e07 \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u0e07",
            "\u2514[ \u0361\u00b0 \u035c\u0296 \u0361\u00b0]\u2518",
            "\u1559( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u1557",
            "\u4e41( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)\u310f",
            "\u30fd( \u0361\u00b0 \u035c\u0296 \u0361\u00b0) \uff89",
            "( \u0361\u00b0 \u035c\u0296 \u0361\u00b0)>\u2310\u25a0-\u25a0",
            "[\u0332\u0305$\u0332\u0305(\u0332\u0305 \u0361\u00b0 \u035c\u0296 \u0361\u00b0\u0332\u0305)\u0332\u0305$\u0332\u0305]"
    ));

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String lenny = LENNY_LIST.get(new Random().nextInt(LENNY_LIST.size()));
        chat.sendMessageWithMentions(args, lenny);
    }

    @Override
    public String getName() {
        return "lenny";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "lennyface");
    }

    @Override
    public String getDescription() {
        return "Returns a lennyface.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("user");
    }
}
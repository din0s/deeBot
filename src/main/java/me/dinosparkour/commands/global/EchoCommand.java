package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EchoCommand extends GlobalCommand {

    private static final String ZWSP = "\u180E";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage(e.getMessage().getMentionedUsers().size() > 5
                ? "Please don't mention so many users! \uD83E\uDD10"
                : ZWSP + String.join(" ", Arrays.asList(args)));
    }

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "say");
    }

    @Override
    public String getDescription() {
        return "Repeats the defined text";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("text");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "I am the greatest!";
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReverseCommand extends GlobalCommand {

    private static final String RTL_OVERRIDE = "\u202E";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        if (e.getMessage().getMentionedUsers().size() > 5)
            chat.sendMessage("Please don't mention so many users! \uD83E\uDD10");
        else
            chat.sendMessage(allArgs.startsWith(RTL_OVERRIDE) ? allArgs.substring(RTL_OVERRIDE.length()) : RTL_OVERRIDE + allArgs);
    }

    @Override
    public String getName() {
        return "reverse";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Reverses the given text.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("text");
    }

    @Override
    public int getArgMin() {
        return 1;
    }
}
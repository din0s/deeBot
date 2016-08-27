package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class InviteCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        sendMessage(MessageUtil.getAuthInvite(e.getJDA(), null));
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns the invite link for the bot.";
    }
}
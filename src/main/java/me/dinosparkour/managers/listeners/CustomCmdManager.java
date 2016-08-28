package me.dinosparkour.managers.listeners;

import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import java.util.List;
import java.util.Random;

public class CustomCmdManager extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        ServerManager sm = new ServerManager(e.getGuild());
        String prefix = sm.getPrefix();
        String rawContent = e.getMessage().getRawContent();
        String cmdName = rawContent.length() <= prefix.length() ? null : rawContent.substring(prefix.length());
        if (!sm.isValid(cmdName))
            return; // Only listen for valid custom commands

        List<String> responses = sm.getCommandResponses(cmdName);
        String message = responses.get(new Random().nextInt(responses.size()));
        MessageUtil.sendMessage(MessageUtil.parseVariables(message, e.getAuthor()), e.getChannel());
    }
}
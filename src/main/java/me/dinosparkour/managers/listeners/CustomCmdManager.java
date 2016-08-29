package me.dinosparkour.managers.listeners;

import me.dinosparkour.managers.BlacklistManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCmdManager extends ListenerAdapter {

    private static final Pattern RANDOM = Pattern.compile(".*(\\$random\\{(.*)\\}).*");

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if (BlacklistManager.isBlacklisted(e.getChannel()))
            return; // Ignore channel if the channel is blacklisted
        ServerManager sm = new ServerManager(e.getGuild());
        String prefix = sm.getPrefix();
        String rawContent = e.getMessage().getRawContent();
        if (rawContent.length() <= prefix.length())
            return; // Ignore message if it's shorter than the prefix itself
        String noPrefix = rawContent.substring(prefix.length());
        String cmdName = noPrefix.contains(" ") ? noPrefix.substring(0, noPrefix.indexOf(" ")) : noPrefix;
        String input = noPrefix.contains(" ") ? noPrefix.substring(cmdName.length()).trim() : "";
        if (!sm.isValid(cmdName))
            return; // Only listen for valid custom commands

        List<String> responses = sm.getCommandResponses(cmdName);
        String message = responses.get(new Random().nextInt(responses.size()));

        Matcher randomMatch = RANDOM.matcher(message);
        if (randomMatch.matches()) {
            List<String> optionList = new ArrayList<>();
            for (String s : randomMatch.group(2).split(";")) {
                s = s.trim();
                if (!s.isEmpty())
                    optionList.add(s);
            }

            String option = !optionList.isEmpty() ? optionList.get(new Random().nextInt(optionList.size())) : null;
            if (option != null)
                message = message.replace(randomMatch.group(1), option);
        }

        message = MessageUtil.parseVariables(message, e.getAuthor())
                .replace("\\n", "\n")
                .replaceAll("(?i)%input%", input);
        MessageUtil.sendMessage(message, e.getChannel());
    }
}
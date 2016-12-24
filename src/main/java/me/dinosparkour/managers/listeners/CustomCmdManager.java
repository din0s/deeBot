package me.dinosparkour.managers.listeners;

import me.dinosparkour.commands.guild.CustomCmdCommand;
import me.dinosparkour.managers.BlacklistManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCmdManager extends ListenerAdapter {

    private static final Pattern RANDOM = Pattern.compile(".*(\\$random\\{(.*)}).*");

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        if (e.getAuthor() == null || e.getAuthor().isBot() || BlacklistManager.isBlacklisted(e.getChannel()))
            return; // Ignore message if the author is a bot or if the channel is blacklisted
        ServerManager sm = new ServerManager(e.getGuild());
        String prefix = sm.getPrefix();
        String rawContent = e.getMessage().getRawContent();
        if (rawContent.length() <= prefix.length() || !rawContent.startsWith(prefix))
            return; // Ignore message if it's shorter than the prefix itself
        String noPrefix = rawContent.substring(prefix.length());
        String cmdName = noPrefix.contains(" ") ? noPrefix.substring(0, noPrefix.indexOf(" ")) : noPrefix;
        String input = noPrefix.contains(" ") ? noPrefix.substring(cmdName.length()).trim() : "[no input]";
        if (!sm.isValid(cmdName))
            return; // Only listen for valid custom commands

        List<String> responses = sm.getCommandResponses(cmdName);
        String message = responses.get(new Random().nextInt(responses.size()));

        Matcher randomMatch = RANDOM.matcher(message);
        if (randomMatch.matches()) {
            List<String> optionList = new ArrayList<>();
            for (String s : randomMatch.group(2).split(";")) {
                s = s.trim();
                if (!s.isEmpty()) {
                    optionList.add(s);
                }
            }

            String option = !optionList.isEmpty() ? optionList.get(new Random().nextInt(optionList.size())) : null;
            if (option != null) {
                message = message.replace(randomMatch.group(1), option);
            }
        }

        Set<String> flags = MessageUtil.parseFlags(message, CustomCmdCommand.getFlagSet());
        Map<String, String> vars = new HashMap<>();
        vars.put("user", MessageUtil.stripFormatting(e.getAuthor().getName()));
        vars.put("userid", e.getAuthor().getId());
        vars.put("input", input);
        vars.put("nickname", e.getGuild().getMember(e.getAuthor()).getEffectiveName());
        vars.put("mention", e.getAuthor().getAsMention());

        message = MessageUtil.stripFlags(message, flags);
        message = MessageUtil.replaceVars(message, vars);

        String msg = message.replace("\\\\n", "\n").replace("\uFE50", ",");

        MessageChannel c = e.getChannel();
        if (flags.contains("--private")) {
            if (!e.getAuthor().hasPrivateChannel()) {
                e.getAuthor().openPrivateChannel().queue(channel -> MessageUtil.sendMessage(msg, channel));
            } else {
                c = e.getAuthor().getPrivateChannel();
            }
        }

        if (flags.contains("--delete") &&
                e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_MANAGE)) {
            e.getMessage().deleteMessage().queue();
        }

        MessageUtil.sendMessage(msg, c);
    }
}
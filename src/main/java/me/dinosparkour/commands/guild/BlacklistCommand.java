package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.BlacklistManager;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistCommand extends GuildCommand {

    public BlacklistCommand() {
        BlacklistManager.init(); // Initialize the BlacklistManager and load the IDs
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        String allArgs = String.join(" ", Arrays.asList(args));
        switch (args.length) {
            case 0:
                TextChannel tc = e.getTextChannel();
                if (!tc.checkPermission(e.getAuthor(), Permission.MESSAGE_MANAGE)) {
                    sendMessage("You need `[MESSAGE_MANAGE]` in order to modify the blacklist!");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                if (BlacklistManager.isBlacklisted(tc)) {
                    BlacklistManager.removeFromBlackList(tc);
                    sb.append("Removed ").append(tc.getAsMention()).append(" from");
                } else {
                    BlacklistManager.addToBlacklist(tc);
                    sb.append("Added ").append(tc.getAsMention()).append(" to");
                }
                sendMessage(sb.append(" the blacklist!").toString());
                break;

            default:
                if (allArgs.equalsIgnoreCase("info")) {
                    List<String> blacklist = BlacklistManager.getBlacklistedChannelIds(e.getGuild()).stream()
                            .map(id -> e.getJDA().getTextChannelById(id).getAsMention())
                            .collect(Collectors.toList());
                    sendMessage("Blacklisted channels: " + (blacklist.isEmpty() ? "None" : String.join(", ", blacklist)));
                } else sendUsageMessage();
                break;
        }
    }

    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Toggles command blacklist for the current channel,\nor returns info on blacklisted channels.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("info");
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.managers.listeners.ShardManager;
import me.dinosparkour.managers.listeners.StatsManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class StatsCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        long globalGuilds = ShardManager.getInstances().stream().flatMap(jda -> jda.getGuilds().stream()).count();
        long globalTextChannels = ShardManager.getInstances().stream().flatMap(jda -> jda.getTextChannels().stream()).count();
        long globalVoiceChannels = ShardManager.getInstances().stream().flatMap(jda -> jda.getVoiceChannels().stream()).count();
        long globalResponses = ShardManager.getInstances().stream().mapToLong(JDA::getResponseTotal).sum();
        int globalUsers = ShardManager.getGlobalUsers().size();

        chat.sendMessage("__Connections__\n"
                + "**\u00b7 " + globalGuilds + "** guilds\n"
                + "**\u00b7 " + globalTextChannels + "** text channels\n"
                + "**\u00b7 " + globalVoiceChannels + "** voice channels\n"
                + "**\u00b7 " + globalUsers + "** unique users\n\n"
                + "__Callbacks__\n"
                + "**\u00b7 " + StatsManager.amountRead() + "** read messages\n"
                + "**\u00b7 " + StatsManager.amountSent() + "** sent messages\n"
                + "**\u00b7 " + globalResponses + "** API responses");
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "statistics");
    }

    @Override
    public String getDescription() {
        return "Returns the statistics for the bot's current session.";
    }
}
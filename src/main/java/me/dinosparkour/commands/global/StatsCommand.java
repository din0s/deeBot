package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class StatsCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        JDA jda = e.getJDA();
        chat.sendMessage("__Connections__\n"
                + "**\u00b7 " + jda.getGuilds().size() + "** guilds\n"
                + "**\u00b7 " + jda.getTextChannels().size() + "** text channels\n"
                + "**\u00b7 " + jda.getVoiceChannels().size() + "** voice channels\n"
                + "**\u00b7 " + jda.getUsers().size() + "** unique users\n\n"
                + "__Callbacks__\n"
                + "**\u00b7 " + amountRead() + "** read messages\n"
                + "**\u00b7 " + amountSent() + "** sent messages\n"
                + "**\u00b7 " + jda.getResponseTotal() + "** API responses");
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
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ShardCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("Info about __**`Shard #" + e.getJDA().getShardInfo().getShardId() + "`**__:\n"
                + "**› " + e.getJDA().getGuilds().size() + "** guilds\n"
                + "**› " + e.getJDA().getTextChannels().size() + "** text channels\n"
                + "**› " + e.getJDA().getVoiceChannels().size() + "** voice channels\n"
                + "**› " + e.getJDA().getUsers().size() + "** unique users");
    }

    @Override
    public String getName() {
        return "shard";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "shardinfo", "shardid");
    }

    @Override
    public String getDescription() {
        return "Returns the current shard's ID.";
    }
}
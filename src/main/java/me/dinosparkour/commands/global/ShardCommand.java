package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.managers.listeners.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ShardCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        if (e.getJDA().getShardInfo() == null) { // Not sharding
            chat.sendMessage("This bot isn't running on separate shards!");
        } else {
            switch (args.length) {
                case 0: // Get current shard
                    chat.sendMessage(getShardInfo(e.getJDA().getShardInfo().getShardId(), e.getJDA()));
                    break;

                case 1:
                    try {
                        if (args[0].equals("*")) { // Get info for all shards
                            StringBuilder sb = new StringBuilder();
                            ShardManager.getInstances()
                                    .forEach(jda -> sb.append(getShardInfo(jda.getShardInfo().getShardId(), jda)).append("\n\n"));
                            chat.sendMessage(sb.toString());
                            return;
                        }

                        int shardNum = Integer.valueOf(args[0]);
                        if (ShardManager.getInstances().size() <= shardNum || shardNum < 0)
                            throw new NumberFormatException();

                        chat.sendMessage(getShardInfo(shardNum, ShardManager.getInstanceList().get(shardNum)));
                    } catch (NumberFormatException ex) {
                        invalidInput(chat);
                    }
                    break;

                default:
                    invalidInput(chat);
                    break;
            }
        }
    }

    @Override
    public String getName() {
        return "shard";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "shardinfo", "shards");
    }

    @Override
    public String getDescription() {
        return "Returns the current shard's ID.";
    }

    private void invalidInput(MessageSender chat) {
        chat.sendMessage("That's not a valid shard number! (0 - " + (ShardManager.getInstances().size() - 1) + ")");
    }

    private String getShardInfo(int id, JDA jda) {
        return "Info about __**`Shard #" + id + "`**__:\n"
                + "**› " + jda.getGuilds().size() + "** guilds\n"
                + "**› " + jda.getTextChannels().size() + "** text channels\n"
                + "**› " + jda.getVoiceChannels().size() + "** voice channels\n"
                + "**› " + jda.getUsers().size() + "** unique users";
    }
}
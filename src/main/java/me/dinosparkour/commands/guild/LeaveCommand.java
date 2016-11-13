package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        if (!e.getMember().hasPermission(Permission.KICK_MEMBERS) && !e.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            chat.sendMessage("You do not have the required permissions to execute this command!\n`[KICK_MEMBERS || BAN_MEMBERS]`");
        } else {
            chat.sendMessage("\uD83D\uDC4B\uD83C\uDFFD Leaving the server!", m -> e.getGuild().leave().queue());
        }
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Forces the bot to leave the server.";
    }
}
package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        if (!PermissionUtil.checkPermission(e.getGuild(), e.getAuthor(), Permission.KICK_MEMBERS)
                && !PermissionUtil.checkPermission(e.getGuild(), e.getAuthor(), Permission.BAN_MEMBERS))
            sendMessage("You do not have the required permissions to execute this command!\n`[KICK_MEMBERS || BAN_MEMBERS]`");
        else
            sendMessage("\uD83D\uDC4B\uD83C\uDFFD Leaving the server!", m -> e.getGuild().getManager().leave());
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
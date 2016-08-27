package me.dinosparkour.commands.guild;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrefixCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        if (!PermissionUtil.checkPermission(e.getGuild(), e.getAuthor(), Permission.ADMINISTRATOR)) {
            sendMessage("You need `[ADMINISTRATOR]` to modify this guild's prefix!");
            return;
        }

        String allArgs = String.join(" ", Arrays.asList(args));
        ServerManager sm = new ServerManager(e.getGuild());
        if (allArgs.equalsIgnoreCase("reset")) {
            String defaultPrefix = Info.DEFAULT_PREFIX;
            sm.setPrefix(defaultPrefix).update();
            sendMessage("__Reset the prefix to__: " + defaultPrefix);
        } else if (!allArgs.equals(getPrefix()) && !allArgs.toLowerCase().startsWith("%s%")) {
            sm.setPrefix(allArgs.replaceAll("(?i)%s%", " ")).update();
            sendMessage("__Set the prefix to__: " + MessageUtil.stripFormatting(allArgs).replaceAll("(?i)%s%", "__ __"));
        } else if (allArgs.equals(getPrefix()))
            sendMessage("The prefix is already set to " + MessageUtil.stripFormatting(getPrefix()));
        else
            sendMessage("The prefix can't have a space as its first char!");
    }

    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "setprefix");
    }

    @Override
    public String getDescription() {
        return "Sets the prefix used by the bot for this guild.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("new prefix / reset");
    }

    @Override
    public Map<String, String> getVariables() {
        return Collections.singletonMap("%s%", "Adds a space character");
    }

    @Override
    public int getArgMin() {
        return 1;
    }
}
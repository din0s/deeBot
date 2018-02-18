package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BanListCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        e.getGuild().getBanList().queue(banList -> {
            if (!banList.isEmpty()) {
                StringBuilder sb = new StringBuilder("```xl\n");
                banList.stream().map(ban -> MessageUtil.userDiscrimSet(ban.getUser())).forEach(user -> {
                    if (sb.length() + user.length() > 2000 - "```".length()) {
                        chat.sendMessage(sb.append("```").toString());
                        sb.setLength(0);
                    } else {
                        sb.append(user).append("\n");
                    }
                });
                if (sb.length() > 0) {
                    chat.sendMessage(sb.append("```").toString());
                }
            } else {
                chat.sendMessage("*The banlist is empty!*");
            }
        });
    }

    @Override
    public String getName() {
        return "banlist";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "bans");
    }

    @Override
    public String getDescription() {
        return "Returns a list of all banned users.";
    }

    @Override
    public List<Permission> requiredPermissions() {
        return Collections.singletonList(Permission.BAN_MEMBERS);
    }
}
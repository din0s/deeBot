package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UnbanCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args, e.getGuild().getManager().getBans());
        switch (userList.size()) {
            case 0:
                sendMessage("No users were found that meet the criteria!");
                break;

            case 1:
                User u = userList.get(0);
                e.getGuild().getManager().unBan(u);
                sendMessage("**" + MessageUtil.userDiscrimSet(u) + "** was \uD83D\uDE4C\uD83C\uDFFD'd by **" + MessageUtil.userDiscrimSet(e.getAuthor()) + "**");
                break;

            default:
                sendMessage("More than one users were found that meet the criteria!\nPlease narrow down your query.");
                break;
        }
    }

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Unbans a user.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("user");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public List<Permission> requiredPermissions() {
        return Collections.singletonList(Permission.BAN_MEMBERS);
    }
}
package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UnbanCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        e.getGuild().getBanList().queue(banList -> {
            List<User> bannedUsers = banList.stream().map(Guild.Ban::getUser).collect(Collectors.toList());
            List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args, bannedUsers);
            switch (userList.size()) {
                case 0:
                    chat.sendMessage("No users were found that meet the criteria!");
                    break;

                case 1:
                    User u = userList.get(0);
                    e.getGuild().getController().unban(u).queue(success -> chat.sendMessage("**" + MessageUtil.userDiscrimSet(u)
                                    + "** was \uD83D\uDE4C\uD83C\uDFFD'd by **" + MessageUtil.userDiscrimSet(e.getAuthor()) + "**")
                            , failure -> chat.sendMessage("**That's not a valid user!**"));
                    break;

                default:
                    chat.sendMessage("More than one users were found that meet the criteria!\nPlease narrow down your query.");
                    break;
            }
        });
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

    @Override
    public String getExample() {
        return "dinos#0649";
    }
}
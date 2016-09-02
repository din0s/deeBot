package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Collections;
import java.util.List;

public class KickCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args);
        switch (userList.size()) {
            case 0:
                chat.sendMessage("No users were found that meet the criteria!");
                return;

            case 1:
                User u = userList.get(0);
                if (!canKick(chat, u, e.getMessage())) return;
                e.getGuild().getManager().kick(u);
                chat.sendMessage("**" + MessageUtil.userDiscrimSet(u) + "** got \uD83D\uDC62'd by **" + MessageUtil.userDiscrimSet(e.getAuthor()) + "**");
                break;

            default:
                chat.sendMessage("More than one users were found that meet the criteria!\nPlease narrow down your query.");
        }
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Kicks a user.";
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
        return Collections.singletonList(Permission.KICK_MEMBERS);
    }

    @Override
    public String getExample() {
        return "dinos#0649";
    }

    private boolean canKick(MessageSender chat, User target, Message msg) {
        Guild guild = ((TextChannel) msg.getChannel()).getGuild();
        User selfInfo = msg.getJDA().getSelfInfo();
        if (!PermissionUtil.canInteract(msg.getAuthor(), target, guild)) {
            chat.sendMessage("Your role is lower in hierarchy than the given user's!");
            return false;
        } else if (target == selfInfo) {
            chat.sendMessage("Please use " + getPrefix(guild) + "leave to remove the bot from the server.");
            return false;
        } else if (!PermissionUtil.canInteract(selfInfo, target, guild)) {
            chat.sendMessage("The bot's role is lower in hierarchy than the given user's!");
            return false;
        } else return true;
    }
}
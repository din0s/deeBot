package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BanCommand extends GuildCommand {

    private MessageReceivedEvent e;

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        this.e = e;
        int days;
        String userTag;
        GuildManager gm = e.getGuild().getManager();
        List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args);
        String allArgs = String.join(" ", Arrays.asList(args));

        switch (userList.size()) {
            case 0:
                String id = args[0];
                days = args.length > 1 ? parseDays(allArgs.replace(id, "").trim()) : 0;
                if (invalidDays(days)) return;

                try {
                    if (id.length() < 17 || id.length() > 18)
                        throw new IllegalArgumentException();
                    gm.ban(id, days);
                    userTag = "U(" + id + ")";
                } catch (IllegalArgumentException ex) {
                    sendMessage("**That's not a valid user!**");
                    return;
                }
                break;

            case 1:
                User u = userList.get(0);
                days = allArgs.contains(" ") ? parseDays(allArgs.substring(allArgs.lastIndexOf(" ") + 1)) : 0;
                if (invalidDays(days) || !canBan(u)) return;

                gm.ban(u, days);
                userTag = MessageUtil.userDiscrimSet(u);
                break;

            default:
                sendMessage("More than one users were found that meet the criteria!\nPlease narrow down your query.");
                return;
        }
        sendMessage("**" + userTag + "** got \uD83C\uDF4C'd by **" + MessageUtil.userDiscrimSet(e.getAuthor()) + "**");
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList("ban");
    }

    @Override
    public String getDescription() {
        return "Bans a user and deletes their messages based on the amount of days specified.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("user");
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("days to purge");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public List<Permission> requiredPermissions() {
        return Collections.singletonList(Permission.BAN_MEMBERS);
    }

    private int parseDays(String days) {
        try {
            return days.equals("") ? 0 : Integer.parseInt(days);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean invalidDays(int days) {
        if (days < 0) {
            sendMessage("**That's not a valid amount of days to purge!**");
            return true;
        } else if (days > 7) {
            sendMessage("**You can only purge a maximum of 7 days worth of messages!**");
            return true;
        }
        return false;
    }

    private boolean canBan(User target) {
        User selfInfo = e.getJDA().getSelfInfo();
        if (!e.getGuild().getUsers().contains(target))
            return true;
        else if (!PermissionUtil.canInteract(e.getAuthor(), target, e.getGuild())) {
            sendMessage("Your role is lower in hierarchy than the given user's!");
            return false;
        } else if (target == selfInfo) {
            sendMessage("Please use " + getPrefix() + "leave to remove the bot from the server.");
            return false;
        } else if (!PermissionUtil.canInteract(selfInfo, target, e.getGuild())) {
            sendMessage("The bot's role is lower in hierarchy than the given user's!");
            return false;
        } else return true;
    }
}
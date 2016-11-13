package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BanCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        int days;
        GuildController controller = e.getGuild().getController();
        List<Member> memberList = new UserUtil().getMentionedMembers(e.getMessage(), args);
        String allArgs = String.join(" ", Arrays.asList(args));

        switch (memberList.size()) {
            case 0: // Ban a user we do not know
                String id = args[0];
                days = args.length > 1 ? parseDays(allArgs.replace(id + " ", "")) : 0;

                if (id.length() < 17 || id.length() > 18 || !NumberUtils.isDigits(id)) {
                    chat.sendMessage("**That's not a valid user!**");
                }

                if (invalidDays(chat, days)) return;
                controller.ban(id, days)
                        .queue(success -> sendBanMessage("U(" + id + ")", e.getAuthor(), chat),
                                failure -> chat.sendMessage("**That's not a valid user!**"));
                break;

            case 1: // Ban a user we know
                Member m = memberList.get(0);
                String lastArg = args[args.length - 1];
                days = args.length > 1 && NumberUtils.isDigits(lastArg) ? parseDays(lastArg) : 0;
                if (invalidDays(chat, days) || !canBan(chat, m, e.getMessage())) return;

                controller.ban(m, days).queue(success -> sendBanMessage(MessageUtil.userDiscrimSet(m.getUser()), e.getAuthor(), chat));
                break;

            default: // Too many users to ban (more than 1)
                chat.sendMessage("More than one users were found that meet the criteria!\nPlease narrow down your query.");
                break;
        }
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

    @Override
    public String getExample() {
        return "dinos#0649 7";
    }

    private int parseDays(String days) {
        try {
            return days.isEmpty() ? 0 : Integer.parseInt(days);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean invalidDays(MessageSender chat, int days) {
        if (days < 0) {
            chat.sendMessage("**That's not a valid amount of days to purge!**");
            return true;
        } else if (days > 7) {
            chat.sendMessage("**You can only purge a maximum of 7 days worth of messages!**");
            return true;
        }
        return false;
    }

    private boolean canBan(MessageSender chat, Member target, Message msg) {
        Guild guild = msg.getGuild();
        if (!guild.getMembers().contains(target)) {
            return true;
        } else if (!guild.getMember(msg.getAuthor()).canInteract(target)) {
            chat.sendMessage("Your role is lower in hierarchy than the given user's!");
            return false;
        } else if (target.equals(guild.getSelfMember())) {
            chat.sendMessage("Please use " + getPrefix(guild) + "leave to remove the bot from the server.");
            return false;
        } else if (!guild.getSelfMember().canInteract(target)) {
            chat.sendMessage("The bot's role is lower in hierarchy than the given user's!");
            return false;
        } else return true;
    }

    private void sendBanMessage(String userTag, User author, MessageSender chat) {
        chat.sendMessage("**" + userTag + "** got \uD83C\uDF4C'd by **" + MessageUtil.userDiscrimSet(author) + "**");
    }
}
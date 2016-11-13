package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class KickCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        List<Member> memberList = new UserUtil().getMentionedMembers(e.getMessage(), args);
        switch (memberList.size()) {
            case 0:
                chat.sendMessage("No users were found that meet the criteria!");
                return;

            case 1:
                Member m = memberList.get(0);
                if (!canKick(chat, m, e.getMessage())) return;
                e.getGuild().getController().kick(m).queue(success ->
                        chat.sendMessage("**" + MessageUtil.userDiscrimSet(m.getUser()) + "** got \uD83D\uDC62'd by **" + MessageUtil.userDiscrimSet(e.getAuthor()) + "**"));
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

    private boolean canKick(MessageSender chat, Member target, Message msg) {
        Guild guild = msg.getGuild();
        if (!guild.getMember(msg.getAuthor()).canInteract(target)) {
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
}
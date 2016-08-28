package me.dinosparkour.managers.listeners;

import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Timer;
import java.util.TimerTask;

public class ActionManager extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        if (ServerManager.hasData(e.getGuild())) onEvent(e, true);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
        if (ServerManager.hasData(e.getGuild())) onEvent(e, false);
    }

    public void onEvent(GenericGuildMemberEvent e, boolean isJoin) {
        Guild guild = e.getGuild();
        User selfInfo = e.getJDA().getSelfInfo();
        ServerManager sm = new ServerManager(guild);
        String message = isJoin ? sm.getWelcomeMessage() : sm.getFarewellMessage();
        Role role = guild.getRoleById(sm.getAutoRoleId());

        if (message != null) {
            message = parseVariables(message, e.getUser(), guild);
            TextChannel channel = e.getJDA().getTextChannelById(isJoin ? sm.getWelcomeChannelId() : sm.getFarewellChannelId());
            if (channel == null || !guild.getTextChannels().contains(channel)) // Make sure we always have a channel
                channel = guild.getPublicChannel();
            MessageUtil.sendMessage(message, channel);
        }

        if (isJoin && role != null) {
            String reason;
            if (!PermissionUtil.checkPermission(guild, selfInfo, Permission.MANAGE_ROLES))
                reason = "the bot not having `[MANAGE_ROLES]`";
            else if (PermissionUtil.canInteract(selfInfo, role)) {
                new Timer().schedule(new TimerTask() {                                  // We have to use a timer because
                    @Override                                                           // otherwise the bot cannot
                    public void run() {                                                 // assign the custom role to
                        guild.getManager().addRoleToUser(e.getUser(), role).update();   // new bots joining the guild
                    }                                                                   // due to a race condition when
                }, 11);                                                                 // autogenerating their role..
                return;
            } else reason = "the role's position being higher in the hierarchy.\n"
                    + "Please move the bot's role to the top in order to fix this issue";
            MessageUtil.sendMessage(unableToGiveRole(role, e.getUser(), reason), guild.getPublicChannel()); // Hopefully we don't reach this statement!!
        }
    }

    private String unableToGiveRole(Role role, User user, String reason) {
        return MessageUtil.stripFormatting("Could not give the role \"" + role.getName() + "\" "
                + "to @" + MessageUtil.userDiscrimSet(user) + " due to ") + reason + "!";
    }

    private String parseVariables(String message, User user, Guild guild) {
        return MessageUtil.parseVariables(message, user)
                .replaceAll("(?i)%guild%", MessageUtil.stripFormatting(guild.getName()))
                .replaceAll("(?i)%usercount%", String.valueOf(guild.getUsers().size()));
    }
}
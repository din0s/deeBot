package me.dinosparkour.managers.listeners;

import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActionManager extends ListenerAdapter {

    private final ScheduledExecutorService roleScheduler = Executors.newScheduledThreadPool(1);

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

        if (e.getMember().equals(guild.getSelfMember()))
            return; // Ignore our own Join/Leave events

        ServerManager sm = new ServerManager(guild);
        String message = isJoin ? sm.getWelcomeMessage() : sm.getFarewellMessage();
        Role role = guild.getRoleById(sm.getAutoRoleId());

        if (message != null) {
            message = parseVariables(message, e.getMember().getUser(), guild);
            TextChannel channel = e.getJDA().getTextChannelById(isJoin ? sm.getWelcomeChannelId() : sm.getFarewellChannelId());
            if (channel == null || !guild.getTextChannels().contains(channel)) // Make sure we always have a channel
                channel = guild.getPublicChannel();
            MessageUtil.sendMessage(message, channel);
        }

        if (isJoin && role != null) {
            String reason;
            if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
                reason = "the bot not having `[MANAGE_ROLES]`";
            else if (guild.getSelfMember().canInteract(role)) {
                // We have to use a timer because otherwise the bot cannot assign the custom role to
                // new bots joining the guild due to a race condition when Discord generates their role..
                roleScheduler.schedule(() -> guild.getController().addRolesToMember(e.getMember(), role).queue(), 11L, TimeUnit.MILLISECONDS);
                return;
            } else reason = "the role's position being higher in the hierarchy.\n"
                    + "Please move the bot's role to the top in order to fix this issue";
            MessageUtil.sendMessage(unableToGiveRole(role, e.getMember().getUser(), reason), guild.getPublicChannel()); // Hopefully we don't reach this statement!!
        }
    }

    private String unableToGiveRole(Role role, User user, String reason) {
        return MessageUtil.stripFormatting("Could not give the role \"" + role.getName() + "\" "
                + "to @" + MessageUtil.userDiscrimSet(user) + " due to ") + reason + "!";
    }

    private String parseVariables(String message, User user, Guild guild) {
        Map<String, String> vars = new HashMap<>();
        vars.put("user", MessageUtil.stripFormatting(user.getName()));
        vars.put("userid", user.getId());
        vars.put("guild", MessageUtil.stripFormatting(guild.getName()));
        vars.put("usercount", String.valueOf(guild.getMembers().size()));
        vars.put("mention", user.getAsMention());
        return MessageUtil.replaceVars(message, vars);
    }
}
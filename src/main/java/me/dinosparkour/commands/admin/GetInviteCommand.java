package me.dinosparkour.commands.admin;

import me.dinosparkour.commands.impls.AdminCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.InviteUtil;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetInviteCommand extends AdminCommand {

    private static final String INVITE_PREFIX = "https://discord.gg/";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        List<Guild> guilds = new ArrayList<>();
        Guild idGuild = e.getJDA().getGuildById(allArgs);

        if (idGuild != null)
            guilds.add(idGuild);
        else
            guilds.addAll(e.getJDA().getGuilds().stream()
                    .filter(g -> g.getName().toLowerCase().contains(allArgs.toLowerCase()))
                    .collect(Collectors.toList()));

        if (guilds.isEmpty()) // No guilds match the arguments
            chat.sendMessage("Your query returned no results!");
        else if (guilds.size() > 1) { // More than one guild match the arguments
            StringBuilder sb = new StringBuilder("Your query returned too many results!\n");
            guilds.stream()
                    .map(g -> String.format("%s (%s) by %s\n", g.getName(), g.getId(), g.getOwner().getUsername()))
                    .forEach(sb::append);
            chat.sendMessage(sb.toString());
        } else { // Exactly one guild matches the arguments
            Guild guild = guilds.get(0);
            User selfInfo = e.getJDA().getSelfInfo();

            List<InviteUtil.AdvancedInvite> existentInvites = new ArrayList<>();
            if (PermissionUtil.checkPermission(guild, selfInfo, Permission.MANAGE_SERVER))
                existentInvites.addAll(InviteUtil.getInvites(guild));

            if (existentInvites.isEmpty()) {
                List<TextChannel> channelList = guilds.get(0).getTextChannels();
                TextChannel channel;
                if (canGenerate(guild.getPublicChannel(), selfInfo))
                    channel = guild.getPublicChannel();
                else
                    channel = channelList.stream()
                            .filter(c -> canGenerate(c, selfInfo))
                            .findAny().orElse(null);
                if (channel == null)
                    chat.sendMessage("I cannot generate an invite to that guild!");
                else
                    chat.sendMessage(INVITE_PREFIX + InviteUtil.createInvite(channel, InviteUtil.InviteDuration.THIRTY_MINUTES, 0, false).getCode());
            } else chat.sendMessage(INVITE_PREFIX + existentInvites.get(0));
        }
    }

    @Override
    public String getName() {
        return "getinvite";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Generates an invite for the specified guild.";
    }

    @Override
    public boolean allowsPrivate() {
        return true;
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("guild name/id");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    private boolean canGenerate(TextChannel channel, User selfInfo) {
        return PermissionUtil.checkPermission(channel, selfInfo, Permission.CREATE_INSTANT_INVITE);
    }
}
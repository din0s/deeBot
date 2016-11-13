package me.dinosparkour.commands.admin;

/* JDA 3.x doesn't support InviteUtil yet
import me.dinosparkour.commands.impls.AdminCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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

        if (idGuild != null) {
            guilds.add(idGuild);
        } else {
            guilds.addAll(e.getJDA().getGuilds().stream()
                    .filter(g -> g.getName().toLowerCase().contains(allArgs.toLowerCase()))
                    .collect(Collectors.toList()));
        }

        if (guilds.isEmpty()) { // No guilds match the arguments
            chat.sendMessage("Your query returned no results!");
        } else if (guilds.size() > 1) { // More than one guild match the arguments
            StringBuilder sb = new StringBuilder("Your query returned too many results!\n");
            guilds.stream()
                    .map(g -> String.format("%s (%s) by %s\n", g.getName(), g.getId(), g.getOwner().getUser().getName()))
                    .forEach(sb::append);
            chat.sendMessage(sb.toString());
        } else { // Exactly one guild matches the arguments
            Guild guild = guilds.get(0);
            Member selfMember = guild.getSelfMember();

            List<InviteUtil.AdvancedInvite> existentInvites = new ArrayList<>();
            if (selfMember.hasPermission(Permission.MANAGE_SERVER)) {
                existentInvites.addAll(InviteUtil.getInvites(guild));
            }

            if (existentInvites.isEmpty()) {
                List<TextChannel> channelList = guilds.get(0).getTextChannels();
                TextChannel channel;
                if (canGenerate(guild.getPublicChannel(), selfMember)) {
                    channel = guild.getPublicChannel();
                } else {
                    channel = channelList.stream()
                            .filter(c -> canGenerate(c, selfMember))
                            .findAny().orElse(null);
                }

                if (channel == null) {
                    chat.sendMessage("I cannot generate an invite to that guild!");
                } else {
                    chat.sendMessage(INVITE_PREFIX + InviteUtil.createInvite(channel, InviteUtil.InviteDuration.THIRTY_MINUTES, 0, false).getCode());
                }
            } else {
                chat.sendMessage(INVITE_PREFIX + existentInvites.get(0).getCode());
            }
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

    private boolean canGenerate(TextChannel channel, Member selfMember) {
        return selfMember.hasPermission(channel, Permission.CREATE_INSTANT_INVITE);
    }
}
*/
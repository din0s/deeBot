package me.dinosparkour.commands.admin;

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

        Guild idGuild = null;
        if (allArgs.matches("\\d+")) { // If it's just digits
            idGuild = e.getJDA().getGuildById(allArgs);
        }

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
            if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                guild.getInvites().queue(existentInvites -> {
                    if (existentInvites.isEmpty()) { // No existent invites, generate one for ourselves
                        createInvite(guild.getTextChannels(), guild, chat);
                    } else { // Pull a random invite, it will do.
                        chat.sendMessage(INVITE_PREFIX + existentInvites.get(0).getCode());
                    }
                });
            } else {
                // We cannot see existent invites, attempt to generate one anyway
                createInvite(guild.getTextChannels(), guild, chat);
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

    private void createInvite(List<TextChannel> channelList, Guild guild, MessageSender chat) {
        TextChannel channel;
        TextChannel defaultChannel = guild.getSelfMember().getDefaultChannel();
        if (canGenerate(defaultChannel, guild.getSelfMember())) {
            channel = defaultChannel;
        } else {
            channel = channelList.stream()
                    .filter(c -> canGenerate(c, guild.getSelfMember()))
                    .findAny().orElse(null);
        }

        if (channel == null) {
            chat.sendMessage("I cannot generate an invite to that guild!");
        } else {
            channel.createInvite().setMaxUses(1).queue(invite -> chat.sendMessage(INVITE_PREFIX + invite.getCode()));
        }
    }
}
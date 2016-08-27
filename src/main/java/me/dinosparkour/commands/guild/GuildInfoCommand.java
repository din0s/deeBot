package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class GuildInfoCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        Guild guild = e.getGuild();
        sendMessage("**``Guild Information`` [** " + MessageUtil.stripFormatting(guild.getName()) + " (" + guild.getId() + ") **]**\n\n"
                + "**Owner**: " + MessageUtil.userDiscrimSet(guild.getOwner()) + " (" + guild.getOwnerId() + ")\n"
                + "**Verification Level**: " + guild.getVerificationLevel() + "\n"
                + "\n"
                + "**User Count**: " + guild.getUsers().size() + "\n"
                + "**TextChannel Count**: " + guild.getTextChannels().size() + "\n"
                + "**VoiceChannel Count**: " + guild.getVoiceChannels().size() + "\n"
                + "\n"
                + "**Voice Region**: " + guild.getRegion().getName() + "\n"
                + "**AFK Timeout**: " + (guild.getAfkTimeout() < 60 ? guild.getAfkTimeout() + " seconds"
                : guild.getAfkTimeout() / 60 + " minutes") + "\n"
                + "**AFK Channel**: " + (guild.getAfkChannelId() == null ? "None"
                : MessageUtil.stripFormatting(e.getJDA().getVoiceChannelById(guild.getAfkChannelId()).getName())));
    }

    @Override
    public String getName() {
        return "guildinfo";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "serverinfo");
    }

    @Override
    public String getDescription() {
        return "Returns information related to the guild.";
    }
}
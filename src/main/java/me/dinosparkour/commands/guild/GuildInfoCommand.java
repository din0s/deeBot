package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class GuildInfoCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        Guild guild = e.getGuild();
        chat.sendMessage("**``Guild Information`` [** " + MessageUtil.stripFormatting(guild.getName()) + " (" + guild.getId() + ") **]**\n\n"
                + "**Owner**: " + MessageUtil.userDiscrimSet(guild.getOwner().getUser()) + " (" + guild.getOwner().getUser().getId() + ")\n"
                + "**Verification Level**: " + guild.getVerificationLevel() + "\n"
                + "\n"
                + "**User Count**: " + guild.getMembers().size() + "\n"
                + "**TextChannel Count**: " + guild.getTextChannels().size() + "\n"
                + "**VoiceChannel Count**: " + guild.getVoiceChannels().size() + "\n"
                + "\n"
                + "**Voice Region**: " + guild.getRegion().getName() + "\n"
                + "**AFK Timeout**: " + (guild.getAfkTimeout().getSeconds() < 60 ? guild.getAfkTimeout() + " seconds"
                : guild.getAfkTimeout().getSeconds() / 60 + " minutes") + "\n"
                + "**AFK Channel**: " + (guild.getAfkChannel() == null ? "None"
                : MessageUtil.stripFormatting(guild.getAfkChannel().getName())));
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
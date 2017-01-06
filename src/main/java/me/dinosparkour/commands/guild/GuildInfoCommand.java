package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class GuildInfoCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        Guild guild = e.getGuild();
        String title = "Guild Info for **" + MessageUtil.stripFormatting(guild.getName()) + "**\n (ID: " + guild.getId() + ")\n";
        String owner = MessageUtil.userDiscrimSet(guild.getOwner().getUser());
        String afkChannel = guild.getAfkChannel() == null ? "None" : MessageUtil.stripFormatting(guild.getAfkChannel().getName());

        Guild.VerificationLevel lvl = guild.getVerificationLevel();
        String afkTimeout = (guild.getAfkTimeout().getSeconds() < 60
                ? guild.getAfkTimeout() + " seconds"
                : guild.getAfkTimeout().getSeconds() / 60 + " minutes");

        if (!guild.getSelfMember().hasPermission(e.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
            chat.sendMessage(title + "\n"
                    + "\uD83D\uDC51 | **Owner** › " + owner + "\n"
                    + "\uD83D\uDC6E" + getSkinColor(lvl) + " | **Verification Level** › " + lvl + "\n"
                    + "\uD83D\uDDD3 | **Creation Date** › " + MessageUtil.formatDate(guild.getCreationTime()) + "\n"
                    + "\uD83D\uDDBC | **Guild Icon** › <" + guild.getIconUrl() + ">\n"
                    + "\n"
                    + "\uD83D\uDC65 | **User Count** › " + guild.getMembers().size() + "\n"
                    + "\uD83D\uDCDD | **Text Channel Count** › " + guild.getTextChannels().size() + "\n"
                    + "\uD83D\uDCE2 | **Voice Channel Count** › " + guild.getVoiceChannels().size() + "\n"
                    + "\n"
                    + "\uD83D\uDDFA | **Voice Region** › " + guild.getRegion().getName() + "\n"
                    + "\uD83D\uDD07 | **AFK Channel** › " + afkChannel + "\n"
                    + "\u23F0 | **AFK Timeout** › " + afkTimeout);
        } else {
            EmbedBuilder builder = new EmbedBuilder().setTitle(title).setDescription("_ _").setThumbnail(guild.getIconUrl());

            builder.addField("Owner", owner, true);
            builder.addField("Verification Level", guild.getVerificationLevel().toString(), true);

            builder.addField("User Count", String.valueOf(guild.getMembers().size()), true);
            builder.addField("Text Channel Count", String.valueOf(guild.getTextChannels().size()), true);

            builder.addField("Voice Channel Count", String.valueOf(guild.getVoiceChannels().size()), true);
            builder.addField("Voice Region", guild.getRegion().getName(), true);

            builder.addField("AFK Channel", afkChannel, true);
            builder.addField("AFK Timeout", afkTimeout, true);

            builder.setFooter("Guild Creation Date", null);
            builder.setTimestamp(guild.getCreationTime());
            chat.sendEmbed(builder);
        }
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

    private String getSkinColor(Guild.VerificationLevel lvl) {
        switch (lvl) {
            case NONE:
                return "\uD83C\uDFFB";

            case LOW:
                return "\uD83C\uDFFD";

            case MEDIUM:
                return "\uD83C\uDFFE";

            case HIGH:
                return "\uD83C\uDFFF";

            default:
                return "";
        }
    }
}
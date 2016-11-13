package me.dinosparkour.managers.listeners;

/* JDA 3.x doesn't support InviteReceivedEvent yet
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class InviteManager extends ListenerAdapter {

    @Override
    public void onInviteReceived(InviteReceivedEvent e) {
        if (!e.isFromType(ChannelType.PRIVATE) || e.getAuthor().isBot())
            return; // Ignore if the invite was sent either in a guild or by a bot

        String guildId = e.getInvite().getGuildId();
        Guild g = e.getJDA().getGuildById(guildId);
        MessageUtil.sendMessage(g == null
                ? "Please click on this link:\nhttp://invite.deebot.xyz"
                : "I'm already in **" + MessageUtil.stripFormatting(g.getName()) + "**!", e.getPrivateChannel());
    }
}
*/
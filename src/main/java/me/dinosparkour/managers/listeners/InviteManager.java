package me.dinosparkour.managers.listeners;

import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class InviteManager extends ListenerAdapter {

    @Override
    public void onInviteReceived(InviteReceivedEvent e) {
        if (!e.isPrivate() || e.getAuthor().isBot())
            return; // Ignore if the invite was sent either in a guild or by a bot

        String guildId = e.getInvite().getGuildId();
        Guild g = e.getJDA().getGuildById(guildId);
        e.getAuthor().getPrivateChannel().sendMessageAsync(g == null
                ? "Please click on this link:\n" + MessageUtil.getAuthInvite(e.getJDA(), guildId)
                : "I'm already in **" + MessageUtil.stripFormatting(g.getName()) + "**!", null);
    }
}
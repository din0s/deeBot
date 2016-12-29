package me.dinosparkour.managers.listeners;

import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parts of this code were taken from JDA 2.X
public class InviteManager extends ListenerAdapter {

    private static final Pattern INVITE_PATTERN = Pattern.compile("\\b(?:http(?:s)?://)?(?:www\\.)?discord(?:\\.gg|app\\.com/invite)/([a-zA-Z0-9-]+)\\b");

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
        if (e.getAuthor().isBot())
            return; // Ignore if the invite was sent by a bot

        Matcher matcher = INVITE_PATTERN.matcher(e.getMessage().getContent());
        while (matcher.find()) {
            Invite.resolve(e.getJDA(), matcher.group(1)).queue(invite -> {
                if (invite != null) {
                    Invite.Guild g = invite.getGuild();
                    MessageUtil.sendMessage(ShardManager.getGlobalGuildById(g.getId()) == null
                            ? "Please click on this link:\nhttp://invite.deebot.xyz"
                            : "I'm already in **" + MessageUtil.stripFormatting(g.getName()) + "**!", e.getChannel());
                }
            });
        }
    }
}
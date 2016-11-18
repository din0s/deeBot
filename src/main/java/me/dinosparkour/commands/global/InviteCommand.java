package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class InviteCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage(e.getAuthor().getAsMention() + ":\nIf you want to invite me to your guild, click on this link: <http://invite.deebot.xyz>"
                + "\n\nIn case something goes wrong, feel free to join my support server and ask fror help!\nhttps://discord.gg/0wEZsVCXid2URhDY");
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns the invite link for the bot.";
    }
}
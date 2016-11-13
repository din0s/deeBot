package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class SupportCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage(e.getAuthor().getAsMention() + ": Follow the invite below to join my support server."
                + "\nhttps://discord.gg/0wEZsVCXid2URhDY");
    }

    @Override
    public String getName() {
        return "support";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "bug");
    }

    @Override
    public String getDescription() {
        return "Returns an invite to the support server.";
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class PingCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("Pinging..", m -> {
            long diff = e.getMessage().getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS);
            m.editMessage("Ping: **" + (diff / 2) + "ms**").queue();
        });
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList("ping");
    }

    @Override
    public String getDescription() {
        return "Returns an estimated ping to Discord's servers.";
    }
}
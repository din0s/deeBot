package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class PingCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        long start = System.currentTimeMillis();
        sendMessage("Pinging..", m -> m.updateMessageAsync("Ping: **" + (System.currentTimeMillis() - start) / 2 + "ms**", null));
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
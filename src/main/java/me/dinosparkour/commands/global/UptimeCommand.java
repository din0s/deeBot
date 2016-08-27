package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UptimeCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        sendMessage("**Current Uptime:** `" + Info.getUptime() + "`");
    }

    @Override
    public String getName() {
        return "uptime";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns the bot's current uptime.";
    }
}
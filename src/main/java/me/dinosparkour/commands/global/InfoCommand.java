package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class InfoCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        Runtime runtime = Runtime.getRuntime();
        int unit = 1024;
        long memoryTotal = runtime.totalMemory() / unit;       // in KB
        long memoryFree = runtime.freeMemory() / unit;         // in KB
        long memoryInUse = (memoryTotal - memoryFree) / unit;  // in MB

        chat.sendMessage("__Bot Info__:\n"
                + "**Bot Creator**: dinos#0649\n"
                + "**Bot Version**: v" + Info.VERSION + "\n"
                + "**Library**: " + Info.LIBRARY + "\n"
                + "**Memory Used**: " + memoryInUse + "MB/" + memoryTotal / unit + "MB\n"
                + "**Uptime**: " + Info.getUptime() + "\n\n"
                + "*Do you have any suggestions for the bot?*\n"
                + "*Join the guild and let me know!*\n"
                + "https://discord.gg/0wEZsVCXid2URhDY");
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Displays information related to the bot.";
    }
}
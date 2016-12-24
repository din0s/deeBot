package me.dinosparkour.commands.admin;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.AdminCommand;
import me.dinosparkour.managers.listeners.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ShutdownCommand extends AdminCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        if (args[0].equalsIgnoreCase(Info.VERSION)) {
            ShardManager.getInstances().forEach(JDA::shutdown);
            System.exit(0);
            // Goodbye!
        }
    }

    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Shuts the current instance of the bot down.";
    }

    @Override
    public boolean allowsPrivate() {
        return true;
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("version");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public int getArgMax() {
        return 1;
    }
}
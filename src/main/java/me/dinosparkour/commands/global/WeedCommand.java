package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class WeedCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("(:̲̅:̲̅:̲̅м̲̅a̲̅я̲̅i̲̅j̲̅u̲̅a̲̅n̲̅a̲̅:̲̅:̲̅:̲̅()ด้็็็็");
    }

    @Override
    public String getName() {
        return "weed";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "marijuana");
    }

    @Override
    public String getDescription() {
        return "Returns a weed zalgo text.";
    }
}
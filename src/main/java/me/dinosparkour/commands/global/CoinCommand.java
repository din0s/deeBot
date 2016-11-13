package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CoinCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("**" + (new Random().nextDouble() > 0.5 ? "Heads!" : "Tails!") + "**");
    }

    @Override
    public String getName() {
        return "coinflip";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "coin", "flip");
    }

    @Override
    public String getDescription() {
        return "Flips a coin and returns the result";
    }

    @Override
    public int getArgMax() {
        return 0;
    }
}
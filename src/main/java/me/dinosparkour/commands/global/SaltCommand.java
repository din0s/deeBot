package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class SaltCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("▒▒▒▒▒▒▒▒▒▒▒▒▒▒▄▄██████▄\n" +
                "▒▒▒▒▒▒▒▒▒▒▄▄████████████▄\n" +
                "▒▒▒▒▒▒▄▄██████████████████\n" +
                "▒▒▒▄████▀▀▀██▀██▌███▀▀▀████\n" +
                "▒▒▐▀████▌▀██▌▀▐█▌████▌█████▌\n" +
                "▒▒█▒▒▀██▀▀▐█▐█▌█▌▀▀██▌██████\n" +
                "▒▒█▒▒▒▒████████████████████▌\n" +
                "▒▒▒▌▒▒▒▒█████░░░░░░░██████▀\n" +
                "▒▒▒▀▄▓▓▓▒███░░░░░░█████▀▀\n" +
                "▒▒▒▒▀░▓▓▒▐█████████▀▀▒\n" +
                "▒▒▒▒▒░░▒▒▐█████▀▀▒▒▒▒▒▒\n" +
                "▒▒░░░░░▀▀▀▀▀▀▒▒▒▒▒▒▒▒▒\n" +
                "▒▒▒░░░░░░░░▒▒");
    }

    @Override
    public String getName() {
        return "salt";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "salty");
    }

    @Override
    public String getDescription() {
        return "Returns the 'Salt' copypasta.";
    }
}
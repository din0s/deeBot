package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.IOUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ChangelogCommand extends GlobalCommand {

    private final List<String> changelogContents = IOUtil.readLinesFromResource("changelog.txt");

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage(String.join("\n", changelogContents));
    }

    @Override
    public String getName() {
        return "changelog";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "cl");
    }

    @Override
    public String getDescription() {
        return "Displays the latest changelog with the most recent changes.";
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.CommandRegistry;
import me.dinosparkour.commands.impls.Command;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends GlobalCommand {

    private final List<String> helpList = new ArrayList<>();

    @Override
    public void onReady(ReadyEvent e) {
        helpList.addAll(CommandRegistry.getPublicCommands().stream().map(this::getHelpMessage).collect(Collectors.toList()));
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        int pageNum = 1;
        int cmdSize = CommandRegistry.getPublicCommands().size();
        int totalPageCount = cmdSize % 5 == 0 ? cmdSize / 5 : Math.floorDiv(cmdSize, 5) + 1;
        StringBuilder page = new StringBuilder("```diff\n");
        String allArgs = String.join(" ", Arrays.asList(args));
        String prefix = getPrefix(e.getGuild());

        if (args.length > 0) {
            String cmdName = args[0].toLowerCase().startsWith(prefix) && !args[0].equalsIgnoreCase(prefix)
                    ? args[0].substring(prefix.length()).trim() : args[0];
            Command argCmd = CommandRegistry.getCommand(cmdName);
            if (argCmd != null) { // Valid command passed as parameter
                chat.sendMessage("**Usage: `" + prefix + argCmd.getUsage() + "`**"
                        + "\n\n**Info:** " + argCmd.getDescription().replace("\n", " ")
                        + (argCmd.getAlias().size() > 1 ? "\n\n**Alias:** `" + String.join("`, `", argCmd.getAlias()) + "`" : "")
                        + (argCmd.getFlags() != null ? "\n\n**Flags:** " + MessageUtil.formatMap(argCmd.getFlags(), ", ", true) : "")
                        + (argCmd.getVariables() != null ? "\n\n**Variables:**```\n" + MessageUtil.formatMap(argCmd.getVariables(), "\n", false) + "```" : "")
                        + (argCmd.getExample() != null ? "\n\n**Example:** `" + prefix + argCmd.getName() + " " + argCmd.getExample() + "`" : ""));
                return;
            }

            if (!NumberUtils.isDigits(allArgs)) { // Non-existent command passed as parameter
                chat.sendMessage("**That's not a valid command!**");
                return;
            }

            // Page number passed as parameter
            try {
                pageNum = Integer.parseInt(allArgs);
                if (pageNum > totalPageCount)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                chat.sendMessage("**That's not a valid page number!** [1 - " + totalPageCount + "]");
                return;
            }
        }

        for (int i = 0; i < 5; i++) {
            int index = i + (pageNum - 1) * 5;
            if (index >= helpList.size()) break;
            page.append(helpList.get(index).replace("%PREFIX%", MessageUtil.breakCodeBlocks(prefix)));
        }
        page.append("! Page ").append(pageNum).append("/").append(totalPageCount).append("```");
        chat.sendMessage(page.toString());
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "commands", "cmds");
    }

    @Override
    public String getDescription() {
        return "Displays help information related to all commands.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("command");
    }

    @Override
    public int getArgMax() {
        return 1;
    }

    private String getHelpMessage(Command cmd) {
        return "- %PREFIX%" + cmd.getUsage() + ":\n+ " + cmd.getDescription().replace("\n", "\n+ ") + "\n\n";
    }
}
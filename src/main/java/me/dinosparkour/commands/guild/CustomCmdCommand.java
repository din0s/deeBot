package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.CommandRegistry;
import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class CustomCmdCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        ServerManager sm = new ServerManager(e.getGuild());
        String inputArgs = String.join(" ", Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        switch (args[0].toLowerCase()) {
            case "create": // Create new command
                if (isNotAuthorized(chat, e.getAuthor(), e.getGuild())) return;
                else if (!String.join(" ", Arrays.asList(args)).contains("|")) {
                    chat.sendUsageMessage();
                    return;
                } else {
                    String name = inputArgs.substring(0, inputArgs.indexOf("|")).trim();
                    String allResponses = e.getMessage()
                            .getRawContent().substring(e.getMessage().getRawContent().indexOf("|") + 1)
                            .trim().replace("\n", "\\n");

                    if (name.isEmpty()) {
                        chat.sendMessage("Your command's name cannot be null!");
                        return;
                    } else if (name.contains(" ")) {
                        chat.sendMessage("Your command's name cannot contain spaces!");
                        return;
                    } else if (CommandRegistry.getCommand(name) != null) {
                        chat.sendMessage("You cannot override the default commands! "
                                + "**" + MessageUtil.stripFormatting(getPrefix(e.getGuild())) + "help " + name + "**");
                        return;
                    } else if (sm.isValid(name)) {
                        chat.sendMessage("You already have a command registered by that name!");
                        return;
                    } else if (allResponses.isEmpty()) {
                        chat.sendMessage("**Please include at least one response!**");
                        return;
                    } else if (allResponses.trim().equalsIgnoreCase("--private")) {
                        chat.sendMessage("**Please include a message before the `Private` tag!**");
                        return;
                    }

                    String[] responses = allResponses.split("(,(\\s+)?)");
                    JSONObject obj = new JSONObject();
                    obj.put("name", name);
                    obj.put("responses", new JSONArray(responses));
                    sm.addCommand(obj).update();
                    chat.sendMessage(getSuccessMessage("created", name));
                }
                break;

            case "list": // List either all commands or command info
                StringBuilder sb = new StringBuilder();
                sm.getCommands().entrySet().stream()
                        .map(set -> "Command \"" + MessageUtil.stripFormatting(set.getKey()) + "\" - Responses " + set.getValue() + "\n")
                        .forEach(sb::append);
                chat.sendMessage(sb.length() > 0 ? sb.toString() : "No custom commands have been registered!");
                break;

            case "delete": // Delete a command
                if (isNotAuthorized(chat, e.getAuthor(), e.getGuild())) return;
                else if (inputArgs.isEmpty()) chat.sendUsageMessage();
                else if (sm.isValid(inputArgs)) {
                    sm.deleteCommand(inputArgs).update();
                    chat.sendMessage(getSuccessMessage("deleted", inputArgs));
                } else chat.sendMessage("That's not a valid command name!");
                break;
        }
    }

    @Override
    public String getName() {
        return "customcommands";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "customcmds");
    }

    @Override
    public String getDescription() {
        return "Manages the custom commands of the server.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("create / list / delete");
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("command name | random, responses");
    }

    @Override
    public Map<String, String> getFlags() {
        Map<String, String> flags = new HashMap<>();
        flags.put("--private", "Send the reply in a private message");
        return flags;
    }

    @Override
    public Map<String, String> getVariables() {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("%user%", "the user's name");
        variables.put("%userId%", "the user's id");
        variables.put("%input%", "the user's command input");
        variables.put("$random{arg0;arg1;arg2...}", "a random argument out of the specified array");
        return variables;
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "create derp | you said %input%, hey %user% $random{one of these; will be picked}";
    }

    private boolean isNotAuthorized(MessageSender chat, User u, Guild g) {
        if (!PermissionUtil.checkPermission(g, u, Permission.MANAGE_SERVER)) {
            chat.sendMessage("You need `[MANAGE_SERVER]` to modify the Custom Commands on this guild!");
            return true;
        } else return false;
    }

    private String getSuccessMessage(String action, String commandName) {
        return "Successfully " + action + " the command \"" + MessageUtil.stripFormatting(commandName) + "\"!";
    }
}
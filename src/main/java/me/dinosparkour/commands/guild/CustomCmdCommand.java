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
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        ServerManager sm = new ServerManager(e.getGuild());
        String inputArgs = String.join(" ", Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        switch (args[0].toLowerCase()) {
            case "create": // Create new command
                if (isNotAuthorized(e.getAuthor(), e.getGuild())) return;
                else if (!String.join(" ", Arrays.asList(args)).contains("|")) {
                    sendUsageMessage();
                    return;
                } else {
                    String name = inputArgs.substring(0, inputArgs.indexOf("|"));
                    String allResponses = inputArgs.substring(name.length() + 1).trim();
                    name = name.trim();

                    if (CommandRegistry.getCommand(name) != null) {
                        sendMessage("You cannot override the default commands! "
                                + "**" + MessageUtil.stripFormatting(getPrefix()) + "help " + name + "**");
                        return;
                    } else if (sm.isValid(name)) {
                        sendMessage("You already have a command registered by that name!");
                        return;
                    } else if (allResponses.equals("")) {
                        sendMessage("**Please include at least one response!**");
                        return;
                    }

                    String[] responses = allResponses.split("(,(\\s+)?)");
                    JSONObject obj = new JSONObject();
                    obj.put("name", name);
                    obj.put("responses", new JSONArray(responses));
                    sm.addCommand(obj).update();
                    sendMessage(getSuccessMessage("created", name));
                }
                break;

            case "list": // List either all commands or command info
                StringBuilder sb = new StringBuilder();
                sm.getCommands().entrySet().stream()
                        .map(set -> "Command \"" + MessageUtil.stripFormatting(set.getKey()) + "\" - Responses " + set.getValue() + "\n")
                        .forEach(sb::append);
                sendMessage(sb.length() > 0 ? sb.toString() : "No custom commands have been registered!");
                break;

            case "delete": // Delete a command
                if (isNotAuthorized(e.getAuthor(), e.getGuild())) return;
                else if (inputArgs.equals("")) sendUsageMessage();
                else if (sm.isValid(inputArgs)) {
                    sm.deleteCommand(inputArgs).update();
                    sendMessage(getSuccessMessage("delted", inputArgs));
                } else sendMessage("That's not a valid command name!");
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
    public Map<String, String> getVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("%user%", "the user's name");
        variables.put("%userId%", "the user's id");
        return variables;
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    private boolean isNotAuthorized(User u, Guild g) {
        if (!PermissionUtil.checkPermission(g, u, Permission.MANAGE_SERVER)) {
            sendMessage("You need `[ADMINISTRATOR]` to modify the Custom Commands on this guild!");
            return true;
        } else return false;
    }

    private String getSuccessMessage(String action, String commandName) {
        return "Succesfully " + action + " the command \"" + MessageUtil.stripFormatting(commandName) + "\"!";
    }
}
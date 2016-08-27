package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class UserSearchCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        String allArgs = String.join(" ", Arrays.asList(args));
        boolean includeNicks = args[args.length - 1].equalsIgnoreCase("--N");
        boolean isGlobal = args[args.length - 1].equalsIgnoreCase("--G");
        List<User> baseCollection = isGlobal ? e.getJDA().getUsers() : e.getGuild().getUsers();
        String query = isGlobal || includeNicks ? allArgs.substring(0, allArgs.length() - 4) : allArgs;
        List<String> results = baseCollection.stream()
                .filter(u -> {
                    String nick = e.getGuild().getNicknameForUser(u);
                    String sample = includeNicks ? nick == null ? u.getUsername() : nick : u.getUsername();
                    return sample.toLowerCase().contains(query);
                })
                .map(MessageUtil::userDiscrimSet)
                .collect(Collectors.toList());

        if (results.size() > 20)
            sendMessage("Your query returned " + results.size() + " users! Please narrow down your search.");
        else if (results.size() == 0)
            sendMessage("Your query returned no results!");
        else
            sendMessage((isGlobal ? "Global " : "") + (includeNicks ? "Nicknamed " : "")
                    + "Users matching \"" + MessageUtil.stripFormatting(query) + "\":\n" + String.join("\n", results));
    }

    @Override
    public String getName() {
        return "find";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "search", "finduser", "searchuser", "usersearch", "userfind");
    }

    @Override
    public String getDescription() {
        return "Retrieves all users matching the given criteria";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("query");
    }

    @Override
    public Map<String, String> getFlags() {
        Map<String, String> flags = new HashMap<>();
        flags.put("--N", "Include nicknames");
        flags.put("--G", "Perform global search");
        return flags;
    }

    @Override
    public int getArgMin() {
        return 1;
    }
}
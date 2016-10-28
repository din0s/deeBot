package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class UserSearchCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        List<String> argArray = Arrays.asList(args);
        String allArgs = String.join(" ", argArray);
        Set<String> flags = argArray.stream().filter(arg -> arg.startsWith("-")).map(String::toLowerCase).collect(Collectors.toSet());
        boolean includeNicks = flags.contains("-n");
        boolean isGlobal = flags.contains("-g");
        boolean isCaseSensitive = flags.contains("--case-sensitive");
        List<User> baseCollection = isGlobal ? e.getJDA().getUsers() : e.getGuild().getUsers();
        String query = allArgs.replaceAll(" (?i)-n", "")
                .replaceAll(" (?i)-g", "")
                .replaceAll(" (?i)--case-sensitive", "").trim();

        List<String> results = baseCollection.stream()
                .filter(u -> {
                    if (isGlobal && includeNicks)
                        return e.getJDA().getGuilds().stream().anyMatch(g -> {
                            String nick = g.getNicknameForUser(u);
                            String name = isCaseSensitive ? query : query.toLowerCase();
                            if (nick == null) return u.getUsername().contains(name);

                            if (!isCaseSensitive) nick = nick.toLowerCase();
                            return nick.contains(name);
                        });
                    else {
                        String nick = e.getGuild().getNicknameForUser(u);
                        String sample = includeNicks ? nick == null ? u.getUsername() : nick : u.getUsername();
                        if (!isCaseSensitive) sample = sample.toLowerCase();
                        return sample.contains(isCaseSensitive ? query : query.toLowerCase());
                    }
                }).map(MessageUtil::userDiscrimSet)
                .collect(Collectors.toList());

        if (results.size() > 20)
            chat.sendMessage("Your query returned " + results.size() + " users! Please narrow down your search.");
        else if (results.size() == 0)
            chat.sendMessage("Your query returned no results! (You searched for _" + MessageUtil.stripFormatting(query) + "_)");
        else
            chat.sendMessage((isGlobal ? "Global " : "") + (includeNicks ? "Nicknamed " : "")
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
        Map<String, String> flags = new LinkedHashMap<>();
        flags.put("-N", "Include nicknames");
        flags.put("-G", "Perform global search");
        flags.put("--case-sensitive", "Strictly match Case Sensitivity");
        return flags;
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "dinos";
    }
}
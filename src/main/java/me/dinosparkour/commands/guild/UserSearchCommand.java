package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.listeners.ShardManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class UserSearchCommand extends GuildCommand {

    private final Map<String, String> flags = new LinkedHashMap<>();

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        Set<String> flagSet = MessageUtil.parseFlags(args, getFlags().keySet());
        boolean includeNicks = flagSet.contains("-n");
        boolean isGlobal = flagSet.contains("-g");
        boolean isCaseSensitive = flagSet.contains("--case-sensitive");

        if (isGlobal && includeNicks) {
            chat.sendMessage("You can only search for nicknamed users in the current guild!");
            return;
        }

        List<User> baseCollection = isGlobal
                ? ShardManager.getGlobalUsers()
                : e.getGuild().getMembers().stream().map(Member::getUser).collect(Collectors.toList());
        String query = MessageUtil.stripFlags(allArgs, flagSet);
        List<String> results = baseCollection.stream()
                .filter(u -> {
                    Member member = e.getGuild().getMember(u);
                    String nick = member == null ? null : member.getNickname();
                    String sample = includeNicks ? nick == null ? u.getName() : nick : u.getName();
                    if (!isCaseSensitive) {
                        sample = sample.toLowerCase();
                    }

                    return sample.contains(isCaseSensitive ? query : query.toLowerCase());
                }).map(MessageUtil::userDiscrimSet)
                .collect(Collectors.toList());

        if (results.size() > 20) {
            chat.sendMessage("Your query returned " + results.size() + " users! Please narrow down your search.");
        } else if (results.size() == 0) {
            chat.sendMessage("Your query returned no results! (You searched for _" + MessageUtil.stripFormatting(query) + "_)");
        } else {
            chat.sendMessage((isGlobal ? "Global " : "") + (includeNicks ? "Nicknamed " : "")
                    + "Users matching \"" + MessageUtil.stripFormatting(query) + "\":\n" + String.join("\n", results));
        }
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
        if (flags.isEmpty()) {
            flags.put("-N", "Include nicknames");
            flags.put("-G", "Perform global search");
            flags.put("--case-sensitive", "Strictly match Case Sensitivity");
        }
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
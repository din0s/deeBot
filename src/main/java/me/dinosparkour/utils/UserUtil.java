package me.dinosparkour.utils;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserUtil {

    private static final Pattern USER_DISCRIM_PATTERN = Pattern.compile("(.*)#(\\d{4})");
    private Set<User> results;
    private String unmatchedArgs;
    private Message msg;
    private List<User> baseCollection;
    private boolean allowNicknames;

    public List<User> getMentionedUsers(Message msg, String[] args, List<User> baseCollection, boolean allowNicknames) {
        this.msg = msg;
        this.baseCollection = baseCollection;
        this.allowNicknames = allowNicknames;
        results = new HashSet<>(msg.getMentionedUsers());
        unmatchedArgs = "";

        for (String s : args) {
            check(s);
            if (!unmatchedArgs.isEmpty()) check(unmatchedArgs + s);
        }
        return new ArrayList<>(results);
    }

    public List<User> getMentionedUsers(Message msg, String[] args, List<User> baseCollection) {
        return getMentionedUsers(msg, args, baseCollection, true);
    }

    public List<User> getMentionedUsers(Message msg, String[] args) {
        return getMentionedUsers(msg, args, msg.getJDA().getUsers(), true);
    }

    private void check(String s) {
        Matcher m = USER_DISCRIM_PATTERN.matcher(s);
        if (m.matches()) {
            User u = getUserWithDiscrim(m.group(1), m.group(2));
            if (u != null) results.add(u);
            else unmatchedArgs += s;
        } else {
            List<User> matchedUsers = getUsersWithNameOrId(s);
            if (matchedUsers.isEmpty()) unmatchedArgs += s;
            else results.addAll(matchedUsers);
        }
    }

    private User getUserWithDiscrim(String name, String discrim) {
        return baseCollection.stream()
                .filter(u -> name == null || u.getUsername().equalsIgnoreCase(name))
                .filter(u -> discrim == null || u.getDiscriminator().equals(discrim))
                .findFirst().orElse(null);
    }

    private List<User> getUsersWithNameOrId(String nameOrId) {
        return baseCollection.stream()
                .filter(u -> {
                    Guild g = msg.isPrivate() ? null : ((TextChannel) msg.getChannel()).getGuild();
                    return allowNicknames && g != null && g.getNicknameForUser(u) != null && g.getNicknameForUser(u).equalsIgnoreCase(nameOrId)
                            || (u.getUsername().equalsIgnoreCase(nameOrId) || u.getId().equals(nameOrId));
                }).collect(Collectors.toList());
    }
}
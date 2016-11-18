package me.dinosparkour.utils;

import me.dinosparkour.managers.listeners.ShardManager;
import net.dv8tion.jda.core.entities.*;

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
    private Message msg;
    private List<User> baseCollection;
    private boolean allowNicknames;

    public List<User> getMentionedUsers(Message msg, String[] args, List<User> baseCollection, boolean allowNicknames) {
        this.msg = msg;
        this.baseCollection = baseCollection;
        this.allowNicknames = allowNicknames;
        results = new HashSet<>(msg.getMentionedUsers());
        String unmatchedArgs = "";

        for (String s : args) {
            if (noMatch(s)) {
                if (!unmatchedArgs.isEmpty()) {
                    if (noMatch(unmatchedArgs + " " + s)) {
                        unmatchedArgs += " " + s;
                    }
                } else {
                    unmatchedArgs = s;
                }
            }
        }
        return new ArrayList<>(results);
    }

    public List<User> getMentionedUsers(Message msg, String[] args, List<User> baseCollection) {
        return getMentionedUsers(msg, args, baseCollection, true);
    }

    public List<User> getMentionedUsers(Message msg, String[] args) {
        return getMentionedUsers(msg, args, ShardManager.getGlobalUsers(), true);
    }

    public List<Member> getMentionedMembers(Message msg, String[] args, List<Member> baseCollection, boolean allowNicknames) {
        return switchToMembers(getMentionedUsers(msg, args, switchToUsers(baseCollection), allowNicknames));
    }

    public List<Member> getMentionedMembers(Message msg, String[] args, List<Member> baseCollection) {
        return getMentionedMembers(msg, args, baseCollection, true);
    }

    public List<Member> getMentionedMembers(Message msg, String[] args) {
        return getMentionedMembers(msg, args, msg.getGuild().getMembers());
    }

    private boolean noMatch(String s) {
        Matcher m = USER_DISCRIM_PATTERN.matcher(s);
        if (m.matches()) {
            User u = getUserWithDiscrim(m.group(1), m.group(2));
            if (u != null) {
                results.add(u);
            }
            else return true;
        } else {
            List<User> matchedUsers = getUsersWithNameOrId(s);
            if (!matchedUsers.isEmpty()) {
                results.addAll(matchedUsers);
            } else return true;
        }
        return false;
    }

    private User getUserWithDiscrim(String name, String discrim) {
        return baseCollection.stream()
                .filter(u -> name == null || u.getName().equalsIgnoreCase(name))
                .filter(u -> discrim == null || u.getDiscriminator().equals(discrim))
                .findFirst().orElse(null);
    }

    private List<User> getUsersWithNameOrId(String nameOrId) {
        return baseCollection.stream()
                .filter(u -> {
                    Guild g = msg.isFromType(ChannelType.TEXT) ? msg.getGuild() : null;
                    return allowNicknames
                            && g != null
                            && g.getMember(u) != null
                            && g.getMember(u).getNickname() != null
                            && g.getMember(u).getNickname().equalsIgnoreCase(nameOrId)
                            || (u.getName().equalsIgnoreCase(nameOrId)
                            || u.getId().equals(nameOrId));
                }).collect(Collectors.toList());
    }

    private List<User> switchToUsers(List<Member> memberList) {
        return memberList.stream().map(Member::getUser).collect(Collectors.toList());
    }

    private List<Member> switchToMembers(List<User> userList) {
        return userList.stream().map(u -> msg.getGuild().getMember(u)).collect(Collectors.toList());
    }
}
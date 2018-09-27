package me.dinosparkour.utils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MessageUtil {

    public static boolean canNotTalk(TextChannel channel) {
        if (channel == null) return true;
        Member member = channel.getGuild().getSelfMember();
        return member == null
                || !member.hasPermission(channel, Permission.MESSAGE_READ)
                || !member.hasPermission(channel, Permission.MESSAGE_WRITE);
    }

    public static void sendMessage(Message message, MessageChannel channel, Consumer<Message> success, Consumer<Throwable> failure) {
        if (channel instanceof TextChannel && canNotTalk((TextChannel) channel)) return;
        channel.sendMessage(message).queue(success, failure);
    }


    public static void sendMessage(MessageEmbed embed, MessageChannel channel) {
        sendMessage(new MessageBuilder().setEmbed(embed).build(), channel, null, null);
    }

    public static void sendMessage(String message, MessageChannel channel, Consumer<Message> success, Consumer<Throwable> failure) {
        sendMessage(new MessageBuilder().append(filter(message)).build(), channel, success, failure);
    }

    public static void sendMessage(String message, MessageChannel channel) {
        sendMessage(message, channel, null, null);
    }

    private static String filter(String msgContent) {
        return msgContent.length() > 2000
                ? "*The output message is over 2000 characters!*"
                : msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");
    }

    public static String formatDate(OffsetDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " UTC";
    }

    public static String formatTime(long time) {
        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);

        return (days > 0 ? days + " " + (days != 1L ? "days" : "day") + " " : "") +
                (hours > 0 ? hours + " " + (hours != 1L ? "hours" : "hour") + " " : "") +
                (minutes > 0 ? minutes + " " + (minutes != 1L ? "minutes" : "minute") + " " : "") +
                (seconds > 0 ? seconds + " " + (seconds != 1L ? "seconds" : "second") : "");
    }

    public static String formatMap(Map<String, String> map, String separator, boolean codeKey) {
        return String.join(separator, map.entrySet().stream()
                .map(set -> {
                    String s = "";
                    if (codeKey)
                        s += "`" + set.getKey() + "`";
                    else
                        s += set.getKey();
                    return s += ": " + set.getValue();
                })
                .collect(Collectors.toList()));
    }

    public static String userDiscrimSet(User u) {
        return stripFormatting(u.getName()) + "#" + u.getDiscriminator();
    }

    public static String stripFormatting(String s) {
        return s.replace("*", "\\*")
                .replace("`", "\\`")
                .replace("_", "\\_")
                .replace("~~", "\\~\\~")
                .replace(">", "\u180E>");
    }

    public static String breakCodeBlocks(String s) {
        return s.replace("```", "\u180e``\u180e`");
    }

    private static String replace(String input, String key, String value) {
        boolean hasValue = !value.isEmpty();
        String keyTag = hasValue ? "%" : "";
        return input.replaceAll((hasValue ? "" : " ") + "(?i)" + keyTag + key + keyTag, value);
    }

    public static String replaceVars(String message, Map<String, String> vars) {
        message = message.replace("\\", Matcher.quoteReplacement("\\"))
                .replace("$", Matcher.quoteReplacement("$"));

        String[] varArray = vars.keySet().toArray(new String[0]);
        for (String var : varArray) {
            message = replace(message, var, vars.get(var));
        }
        return message;
    }

    public static Set<String> parseFlags(String[] args, Set<String> flags) {
        flags = flags.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return Arrays.stream(args)
                .map(String::toLowerCase)
                .filter(flags::contains)
                .collect(Collectors.toSet());
    }

    public static Set<String> parseFlags(String message, Set<String> flags) {
        return parseFlags(message.split("\\s+"), flags);
    }

    public static String stripFlags(String message, Set<String> flags) {
        String[] flagArray = flags.toArray(new String[0]);
        for (String flag : flagArray) {
            message = replace(message, flag, "");
        }
        return message;
    }
}
package me.dinosparkour.utils;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.ApplicationUtil;
import net.dv8tion.jda.utils.PermissionUtil;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageUtil {

    public static void sendMessage(String message, MessageChannel channel, Consumer<Message> callback) {
        if (channel instanceof TextChannel && !PermissionUtil.canTalk((TextChannel) channel)) return;
        channel.sendMessageAsync(MessageUtil.filter(message), callback);
    }

    public static void sendMessage(String message, MessageChannel channel) {
        sendMessage(message, channel, null);
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
        return stripFormatting(u.getUsername()) + "#" + u.getDiscriminator();
    }

    public static String stripFormatting(String s) {
        return s.replace("*", "\\*")
                .replace("`", "\\`")
                .replace("_", "\\_")
                .replace("~~", "\\~\\~")
                .replace(">", "\\>");
    }

    public static String breakCodeBlocks(String s) {
        return s.replace("```", "\u180e``\u180e`");
    }

    public static String getAuthInvite(JDA jda, String guildId) {
        return ApplicationUtil.getAuthInvite(jda, Permission.ADMINISTRATOR, Permission.MANAGE_ROLES,
                Permission.KICK_MEMBERS, Permission.BAN_MEMBERS,
                Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY) + (guildId == null ? "" : "&guild_id=" + guildId);
    }

    public static String parseVariables(String message, User user) {
        return message.replaceAll("(?i)%userid%", user.getId())
                .replaceAll("(?i)%user%", stripFormatting(user.getUsername()));
    }
}
package me.dinosparkour.commands.impls;

import me.dinosparkour.Info;
import me.dinosparkour.managers.BlacklistManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Command extends ListenerAdapter {

    private static int read = 0;
    private static int sent = 0;
    private MessageReceivedEvent e;

    protected static int amountRead() {
        return read;
    }

    protected static int amountSent() {
        return sent;
    }

    public abstract void executeCommand(String[] args, MessageReceivedEvent e);

    public abstract String getName();

    public abstract List<String> getAlias();

    public abstract String getDescription();

    public abstract boolean allowsPrivate();

    public List<String> getRequiredParams() {
        return null;
    }

    public List<String> getOptionalParams() {
        return null;
    }

    public Map<String, String> getFlags() {
        return null;
    }

    public Map<String, String> getVariables() {
        return null;
    }

    public boolean authorExclusive() {
        return false;
    }

    public int getArgMin() {
        return 0;
    }

    public int getArgMax() {
        return -1;
    }

    public List<Permission> requiredPermissions() {
        return null;
    }

    public final String getUsage() {
        return getName()
                + (getRequiredParams() != null ? " " + String.join(" ", getRequiredParams().stream()
                .map(param -> "[" + param + "]").collect(Collectors.toList())) : "")
                + (getOptionalParams() != null ? " " + String.join(" ", getOptionalParams().stream()
                .map(param -> "(" + param + ")").collect(Collectors.toList())) : "");
    }

    protected final String getPrefix() {
        return !e.isPrivate() && ServerManager.getPrefixes().containsKey(e.getGuild().getId())
                ? ServerManager.getPrefixes().get(e.getGuild().getId())
                : Info.DEFAULT_PREFIX;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        this.e = e;
        read++;

        if (e.getAuthor().isBot() || !isValidCommand(e.getMessage()))
            return; // Ignore message if it's not a command or sent by a bot
        if (authorExclusive() && !e.getAuthor().getId().equals(Info.AUTHOR_ID))
            return; // Ignore if the command is meant to be used by the owner only
        if (!e.isPrivate() && !PermissionUtil.canTalk(e.getJDA().getSelfInfo(), e.getTextChannel()))
            return; // Ignore if we cannot talk in the channel anyway
        if (BlacklistManager.isBlacklisted(e.getChannel())                                         // Ignonre if the channel is blacklisted
                && (!e.getTextChannel().checkPermission(e.getAuthor(), Permission.MESSAGE_MANAGE)  // except for the people with MESSAGE_MANAGE
                || !getName().equals("blacklist")))                                                // when the blacklist command is executed
            return;

        String[] args = commandArgs(e.getMessage());

        if (e.isPrivate() && !allowsPrivate()) // Check if the command is guild-only
            sendMessage("**This command can only be used in a guild!**");
        else if (permissionCheck(e.getAuthor())) { // Check if the user is authorized to execute the command
            if (!permissionCheck(e.getJDA().getSelfInfo())) // Check if the bot can execute the actions needed
                sendMessage("The bot doesn't have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
            if ((getArgMax() > -1                 // A maximum argument limit has been set AND
                    && args.length > getArgMax()) // There are more arguments than we expected, OR
                    || args.length < getArgMin()) // There are fewer arguments than we required
                sendUsageMessage();
            else executeCommand(args, e);
        } else
            sendMessage("You do not have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
    }

    private boolean isValidCommand(Message msg) {
        if (!msg.getRawContent().startsWith(getPrefix()))
            return false; // It's not a command if it doesn't start with our prefix
        String cmdName = msg.getRawContent().substring(getPrefix().length());
        if (cmdName.contains(" "))
            cmdName = cmdName.substring(0, cmdName.indexOf(" ")); // If there are paremeters, remove them
        return getAlias().contains(cmdName.toLowerCase());
    }

    private String[] commandArgs(Message msg) {
        String noPrefix = msg.getRawContent().substring(getPrefix().length());
        if (!noPrefix.contains(" ")) // No whitespaces -> No args
            return new String[]{};
        return noPrefix.substring(noPrefix.indexOf(" ") + 1).split("\\s+");
    }

    protected void sendMessage(String msgContent, MessageChannel tChannel, Consumer<Message> callback) {
        msgContent = msgContent.length() > 2000
                ? "*The output message is over 2000 characters!*"
                : msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");

        if (tChannel == null || (tChannel.getClass().equals(TextChannel.class) && !PermissionUtil.canTalk((TextChannel) tChannel)))
            return;
        tChannel.sendMessageAsync(msgContent, callback);
        sent++;
    }

    protected void sendMessage(String msgContent, Consumer<Message> callback) {
        sendMessage(msgContent, e.getChannel(), callback);
    }

    protected void sendMessage(String msgContent, MessageChannel channel) {
        sendMessage(msgContent, channel, null);
    }

    protected void sendMessage(String msgContent) {
        sendMessage(msgContent, e.getChannel());
    }

    protected void sendUsageMessage() {
        sendMessage("**Usage:** " + getPrefix() + getUsage());
    }

    protected void sendTargettedMessage(String content, String[] args) {
        List<User> base = e.isPrivate() ? Collections.singletonList(e.getAuthor()) : e.getGuild().getUsers();
        List<User> mentionedUsers = new UserUtil().getMentionedUsers(e.getMessage(), args, base);
        if (mentionedUsers.isEmpty())
            sendMessage(content);
        else if (mentionedUsers.size() > 5)
            sendMessage("Please don't mention so many users! \uD83E\uDD10");
        else {
            StringBuilder sb = new StringBuilder();
            mentionedUsers.stream()
                    .map(u -> " <@" + u.getId() + ">")
                    .forEach(sb::append);
            sendMessage(sb.append(": ").append(content).toString());
        }
    }

    protected void sendPrivateMessage(String content, TextChannel fallbackChannel) {
        sendMessage(content, e.getAuthor().getPrivateChannel(), m -> {
            if (m == null) // Something went wrong while sending the message
                sendMessage("Please allow the bot to be able to send you Private Messages.", fallbackChannel);
            else sendMessage("✅ Check your DMs!", fallbackChannel);
        });
    }

    private boolean permissionCheck(User u) {
        return e.isPrivate() // Private Commands have no permissions
                || requiredPermissions() == null // No permissions are required to execute the command
                || requiredPermissions().stream().noneMatch(p -> !PermissionUtil.checkPermission(e.getTextChannel(), u, p)) // The user has all permissions needed
                || u.getId().equals(Info.AUTHOR_ID); // The user is the bot's author
    }
}
package me.dinosparkour.commands.impls;

import me.dinosparkour.Info;
import me.dinosparkour.managers.BlacklistManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
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

    protected static int amountRead() {
        return read;
    }

    protected static int amountSent() {
        return sent;
    }

    public abstract void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat);

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

    protected final String getPrefix(Guild guild) {
        return guild != null && ServerManager.getPrefixes().containsKey(guild.getId())
                ? ServerManager.getPrefixes().get(guild.getId())
                : Info.DEFAULT_PREFIX;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        read++;
        if (!e.isPrivate() && e.getTextChannel() == null) { // Check if the channel is null before getting the guild to prevent NPEs due to concurrency issues
            System.err.println("Received NULL TextChannel in a GuildMessage Event, skipping.");
            return;
        }
        String prefix = getPrefix(e.getGuild());

        if (e.getAuthor().isBot() || !isValidCommand(prefix, e.getMessage()))
            return; // Ignore message if it's not a command or sent by a bot
        if (authorExclusive() && !e.getAuthor().getId().equals(Info.AUTHOR_ID))
            return; // Ignore if the command is meant to be used by the owner only
        if (!e.isPrivate() && !PermissionUtil.canTalk(e.getJDA().getSelfInfo(), e.getTextChannel()))
            return; // Ignore if we cannot talk in the channel anyway
        if (BlacklistManager.isBlacklisted(e.getChannel())                                         // Ignore if the channel is blacklisted
                && (!e.getTextChannel().checkPermission(e.getAuthor(), Permission.MESSAGE_MANAGE)  // except for the people with MESSAGE_MANAGE
                || !getName().equals("blacklist")))                                                // when the blacklist command is executed
            return;

        String[] args = commandArgs(prefix, e.getMessage());
        MessageSender chat = new MessageSender(e);

        if (e.isPrivate() && !allowsPrivate()) // Check if the command is guild-only
            chat.sendMessage("**This command can only be used in a guild!**");
        else if (permissionCheck(e, e.getAuthor())) { // Check if the user is authorized to execute the command
            if (!permissionCheck(e, e.getJDA().getSelfInfo())) // Check if the bot can execute the actions needed
                chat.sendMessage("The bot doesn't have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
            if ((getArgMax() > -1                 // A maximum argument limit has been set AND
                    && args.length > getArgMax()) // There are more arguments than we expected, OR
                    || args.length < getArgMin()) // There are fewer arguments than we required
                chat.sendUsageMessage();
            else executeCommand(args, e, chat);
        } else
            chat.sendMessage("You do not have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
    }

    private boolean isValidCommand(String prefix, Message msg) {
        if (!msg.getRawContent().startsWith(prefix))
            return false; // It's not a command if it doesn't start with our prefix
        String cmdName = msg.getRawContent().substring(prefix.length());
        if (cmdName.contains(" "))
            cmdName = cmdName.substring(0, cmdName.indexOf(" ")); // If there are parameters, remove them
        return getAlias().contains(cmdName.toLowerCase());
    }

    private String[] commandArgs(String prefix, Message msg) {
        String noPrefix = msg.getRawContent().substring(prefix.length());
        if (!noPrefix.contains(" ")) // No whitespaces -> No args
            return new String[]{};
        return noPrefix.substring(noPrefix.indexOf(" ") + 1).split("\\s+");
    }

    private boolean permissionCheck(MessageReceivedEvent e, User u) {
        return e.isPrivate() // Private Commands have no permissions
                || requiredPermissions() == null // No permissions are required to execute the command
                || requiredPermissions().stream().noneMatch(p -> !PermissionUtil.checkPermission(e.getTextChannel(), u, p)) // The user has all permissions needed
                || u.getId().equals(Info.AUTHOR_ID); // The user is the bot's author
    }

    // Credits to Kantenkugel for wasting his time on me
    protected class MessageSender {

        private final MessageReceivedEvent event;

        MessageSender(MessageReceivedEvent event) {
            this.event = event;
        }

        public void sendMessage(String msgContent, MessageChannel tChannel, Consumer<Message> callback) {
            if (tChannel == null || (tChannel.getClass().equals(TextChannel.class) && !PermissionUtil.canTalk((TextChannel) tChannel)))
                return;
            tChannel.sendMessageAsync(MessageUtil.filter(msgContent), callback);
            sent++;
        }

        public void sendMessage(String msgContent, Consumer<Message> callback) {
            sendMessage(msgContent, event.getChannel(), callback);
        }

        public void sendMessage(String msgContent, MessageChannel channel) {
            sendMessage(msgContent, channel, null);
        }

        public void sendMessage(String msgContent) {
            sendMessage(msgContent, event.getChannel());
        }

        public void sendUsageMessage() {
            sendMessage("**Usage:** " + getPrefix(event.getGuild()) + getUsage());
        }

        public void sendMessageWithMentions(String content, String[] args) {
            List<User> base = event.isPrivate() ? Collections.singletonList(event.getAuthor()) : event.getGuild().getUsers();
            List<User> mentionedUsers = new UserUtil().getMentionedUsers(event.getMessage(), args, base);
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

        public void sendPrivateMessage(String content, TextChannel fallbackChannel) {
            sendMessage(content, event.getAuthor().getPrivateChannel(), m -> {
                if (m == null) // Something went wrong while sending the message
                    sendMessage("Please allow the bot to be able to send you Private Messages.", fallbackChannel);
                else sendMessage("✅ Check your DMs!", fallbackChannel);
            });
        }
    }
}
package me.dinosparkour.commands.impls;

import me.dinosparkour.Info;
import me.dinosparkour.commands.admin.CleanupCommand;
import me.dinosparkour.commands.admin.EvalCommand;
import me.dinosparkour.commands.global.DiscordStatusCommand;
import me.dinosparkour.commands.global.HastebinCommand;
import me.dinosparkour.managers.BlacklistManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Command extends ListenerAdapter {

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

    public String getExample() {
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
        // Checks related to the Event's objects, to prevent concurrency issues.
        if (e.getAuthor() == null || e.getChannel() == null)
            return;

        String prefix = getPrefix(e.getGuild());
        if (e.getAuthor().isBot() || !isValidCommand(prefix, e.getMessage()))
            return; // Ignore message if it's not a command or sent by a bot
        if (authorExclusive() && !e.getAuthor().getId().equals(Info.AUTHOR_ID))
            return; // Ignore if the command is meant to be used by the owner only
        if (e.isFromType(ChannelType.TEXT) && MessageUtil.canNotTalk(e.getTextChannel()))
            return; // Ignore if we cannot talk in the channel anyway
        if (BlacklistManager.isBlacklisted(e.getChannel())                                       // Ignore if the channel is blacklisted
                && (!e.getMember().hasPermission(e.getTextChannel(), Permission.MESSAGE_MANAGE)  // except for the people with MESSAGE_MANAGE
                || !getName().equals("blacklist")))                                              // when the blacklist command is executed
            return;

        String[] args = commandArgs(prefix, e.getMessage());
        MessageSender chat = new MessageSender(e);

        if (e.isFromType(ChannelType.PRIVATE) && !allowsPrivate()) { // Check if the command is guild-only
            chat.sendMessage("**This command can only be used in a guild!**");
        } else if (permissionCheck(e, e.getAuthor())) { // Check if the user is authorized to execute the command
            if (!permissionCheck(e, e.getJDA().getSelfUser())) { // Check if the bot can execute the actions needed
                chat.sendMessage("The bot doesn't have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
            } else if ((getArgMax() > -1            // A maximum argument limit has been set AND
                    && args.length > getArgMax())   // There are more arguments than we expected, OR
                    || args.length < getArgMin()) { // There are fewer arguments than we required
                chat.sendUsageMessage();
            } else if (!e.getChannel().getId().equals("125227483518861312") || jdaCheck()) { // Of all commands, only JDAVersion can be issued in JDA #general
                try {
                    executeCommand(args, e, chat);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String msg = "User: **" + MessageUtil.userDiscrimSet(e.getAuthor())
                            + "**\nMessage:\n*" + MessageUtil.stripFormatting(e.getMessage().getContent())
                            + "*\n\nError:```java\n" + ex.getMessage() + "```";
                    if (msg.length() <= 2000) {
                        chat.sendPrivateMessageToUser(msg, e.getJDA().getUserById(Info.AUTHOR_ID));
                    }
                }
            }
        } else {
            chat.sendMessage("You do not have the required permissions to execute this command!\n`" + requiredPermissions() + "`");
        }
    }

    private boolean isValidCommand(String prefix, Message msg) {
        if (!msg.getRawContent().startsWith(prefix))
            return false; // It's not a command if it doesn't start with our prefix
        String cmdName = msg.getRawContent().substring(prefix.length());
        if (cmdName.contains(" ")) {
            cmdName = cmdName.substring(0, cmdName.indexOf(" ")); // If there are parameters, remove them
        }
        if (cmdName.contains("\n")) {
            cmdName = cmdName.substring(0, cmdName.indexOf("\n"));
        }
        return getAlias().contains(cmdName.toLowerCase());
    }

    private String[] commandArgs(String prefix, Message msg) {
        String noPrefix = msg.getRawContent().substring(prefix.length());
        if (!noPrefix.contains(" ")) { // No whitespaces -> No args
            return new String[]{};
        }
        return noPrefix.substring(noPrefix.indexOf(" ") + 1).split("\\s+");
    }

    private boolean permissionCheck(MessageReceivedEvent e, User u) {
        return e.isFromType(ChannelType.PRIVATE) // Private Commands have no permissions
                || requiredPermissions() == null // No permissions are required to execute the command
                || requiredPermissions().stream().allMatch(p -> e.getGuild().getMember(u).hasPermission(e.getTextChannel(), p)) // The user has all permissions needed
                || u.getId().equals(Info.AUTHOR_ID); // The user is the bot's author
    }

    private boolean jdaCheck() {
        return this instanceof CleanupCommand
                || this instanceof EvalCommand
                || this instanceof DiscordStatusCommand
                || this instanceof HastebinCommand;
    }

    public class MessageSender {
        private final MessageReceivedEvent event;

        MessageSender(MessageReceivedEvent event) {
            this.event = event;
        }

        public void sendMessage(String msgContent, MessageChannel tChannel, Consumer<Message> success, Consumer<Throwable> failure) {
            if (tChannel == null) return;
            MessageUtil.sendMessage(msgContent, tChannel, success, failure);
        }

        public void sendMessage(String msgContent, Consumer<Message> success, Consumer<Throwable> failure) {
            sendMessage(msgContent, event.getChannel(), success, failure);
        }

        public void sendMessage(String msgContent, Consumer<Message> success) {
            sendMessage(msgContent, success, null);
        }

        public void sendMessage(String msgContent, MessageChannel channel) {
            sendMessage(msgContent, channel, null, null);
        }

        public void sendMessage(String msgContent) {
            sendMessage(msgContent, event.getChannel());
        }

        public void sendUsageMessage() {
            sendMessage("**Usage:** " + getPrefix(event.getGuild()) + getUsage());
        }

        public void sendMessageWithMentions(String[] args, String content) {
            List<User> base = event.isFromType(ChannelType.PRIVATE)
                    ? Collections.singletonList(event.getAuthor())
                    : event.getGuild().getMembers().stream().map(Member::getUser).collect(Collectors.toList());
            List<User> mentionedUsers = new UserUtil().getMentionedUsers(event.getMessage(), args, base);
            if (mentionedUsers.isEmpty()) {
                sendMessage(content);
            } else if (mentionedUsers.size() > 5) {
                sendMessage("Please don't mention so many users! \uD83E\uDD10");
            } else {
                StringBuilder sb = new StringBuilder();
                mentionedUsers.stream()
                        .map(u -> " <@" + u.getId() + ">")
                        .forEach(sb::append);
                sendMessage(sb.append(": ").append(content).toString());
            }
        }

        public void sendEmbed(EmbedBuilder builder) {
            if (event.isFromType(ChannelType.TEXT) && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                MessageUtil.sendMessage(builder.build(), event.getChannel());
            } else {
                sendMessage("Please give the bot permissions to `EMBED LINKS`.");
            }
        }

        public void sendPrivateMessage(String content, TextChannel fallbackChannel) {
            event.getAuthor().openPrivateChannel().queue(channel -> sendMessage(content, channel,
                    s -> sendMessage("✅ Check your DMs!", fallbackChannel),
                    f -> sendMessage("Please allow the bot to be able to send you Private Messages.", fallbackChannel)));
        }

        public void sendPrivateMessageToUser(String content, User user) {
            if (user == null)
                return;
            user.openPrivateChannel().queue(c -> sendMessage(content, c));
        }
    }
}
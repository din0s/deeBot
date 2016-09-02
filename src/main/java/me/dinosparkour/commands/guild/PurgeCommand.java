package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PurgeCommand extends GuildCommand {

    private final ScheduledExecutorService ratelimitScheduler = Executors.newScheduledThreadPool(1);
    private final Set<String> ratelimitedGuilds = new HashSet<>();

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        int input;
        try {
            input = Integer.parseInt(args[0]);
            if (input > 100 || input <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            chat.sendMessage("**That's not a valid amount!** [1-100]");
            return;
        }

        User user = null;
        if (args.length > 1) {
            List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), Arrays.copyOfRange(args, 1, args.length));
            switch (userList.size()) {
                case 0:
                    break;

                case 1:
                    user = userList.get(0);
                    break;

                default:
                    chat.sendMessage("You can only purge 1 user's messages at a time!");
                    return;
            }
        }

        int amount;
        if (input == 100) {
            e.getMessage().deleteMessage();
            amount = input;
        } else
            amount = input + 1;

        List<Message> history = e.getChannel().getHistory().retrieve(amount);
        if (user != null) {
            User fUser = user; // Final User Object
            history = history.stream().filter(msg -> msg.getAuthor().equals(fUser)).collect(Collectors.toList());
        }

        if (history.size() == 1)
            history.get(0).deleteMessage();
        else if (!history.isEmpty())
            if (ratelimitedGuilds.contains(e.getGuild().getId())) {
                chat.sendMessage("**Please wait a second before deleting more messages!** [Rate limits]");
                return;
            } else {
                e.getTextChannel().deleteMessages(history);
                ratelimitedGuilds.add(e.getGuild().getId());
                ratelimitScheduler.schedule(() -> ratelimitedGuilds.remove(e.getGuild().getId()), 1, TimeUnit.SECONDS);
            }

        chat.sendMessage("Successfully deleted "
                + (user == null ? (e.getTextChannel().getHistory().retrieve(1) == null ? "all" : input) + " messages!"
                : "**" + MessageUtil.stripFormatting(user.getUsername()) + "**'s messages from the past " + input + " lines!"));
    }

    @Override
    public String getName() {
        return "purge";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "prune");
    }

    @Override
    public String getDescription() {
        return "Deletes the specified amount of recent messages\n(from a specific user if mentioned).";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("amount");
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("user");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public List<Permission> requiredPermissions() {
        return Arrays.asList(Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE);
    }

    @Override
    public String getExample() {
        return "50 dinos#0649";
    }
}
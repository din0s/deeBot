package me.dinosparkour.commands.admin;

import me.dinosparkour.commands.impls.AdminCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CleanupCommand extends AdminCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        int amount = 10;
        boolean bulk = e.isFromType(ChannelType.TEXT)
                && e.getGuild().getSelfMember().hasPermission(e.getTextChannel(), Permission.MESSAGE_MANAGE);

        if (args.length == 1) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount > 100)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                chat.sendMessage("**That's not a valid amount!** [1 - 100]");
                return;
            }
        }

        e.getTextChannel().getHistory().retrievePast(amount).queue(history -> {
            history.remove(0);
            history = history.stream()
                    .filter(msg -> msg.getAuthor().equals(e.getJDA().getSelfUser()))
                    .collect(Collectors.toList());
            if (bulk && history.size() > 1) {
                e.getTextChannel().deleteMessages(history).queue();
            } else {
                history.forEach(msg -> msg.delete().queue());
            }
        });
    }

    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "clean");
    }

    @Override
    public String getDescription() {
        return "Removes the bot's most recent messages";
    }

    @Override
    public boolean allowsPrivate() {
        return false;
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("amount");
    }

    @Override
    public int getArgMax() {
        return 1;
    }
}
package me.dinosparkour.commands.admin;

import me.dinosparkour.commands.impls.AdminCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CleanupCommand extends AdminCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        int amount = 5;
        User selfInfo = e.getJDA().getSelfInfo();
        boolean bulk = PermissionUtil.checkPermission(e.getTextChannel(), selfInfo, Permission.MESSAGE_MANAGE);

        if (args.length == 1)
            try {
                amount = Integer.parseInt(args[0]);
                if (amount > 100)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                sendMessage("**That's not a valid amount!** [1 - 100]");
                return;
            }

        List<Message> history = e.getTextChannel().getHistory().retrieve(amount).stream()
                .filter(msg -> msg.getAuthor().equals(selfInfo)).collect(Collectors.toList());
        if (bulk && history.size() > 1)
            e.getTextChannel().deleteMessages(history);
        else
            history.forEach(Message::deleteMessage);
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
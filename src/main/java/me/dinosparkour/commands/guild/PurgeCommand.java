package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PurgeCommand extends GuildCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        int input;
        try {
            input = Integer.parseInt(args[0]);
            if (input > 100 || input <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            chat.sendMessage("**That's not a valid amount!** [1-100]");
            return;
        }

        Member member = null;
        if (args.length > 1) {
            int len = isSilent(args) ? args.length - 1 : args.length;
            List<Member> memberList = new UserUtil().getMentionedMembers(e.getMessage(), Arrays.copyOfRange(args, 1, len));
            switch (memberList.size()) {
                case 0:
                    break;

                case 1:
                    member = memberList.get(0);
                    break;

                default:
                    chat.sendMessage("You can only purge 1 user's messages at a time!");
                    return;
            }
        }

        int amount;
        if (input == 100) {
            e.getMessage().delete().queue();
            amount = input;
        } else {
            amount = input + 1;
        }

        Member fMember = member;
        e.getChannel().getHistory().retrievePast(amount).queue(history -> {
            if (!history.isEmpty() && fMember != null) {
                history = history.stream().filter(msg -> msg.getAuthor().equals(fMember.getUser())).collect(Collectors.toList());
            }

            int initialCount = history.size();
            String out = history.removeIf(msg -> msg.getCreationTime().isBefore(OffsetDateTime.now().minus(2, ChronoUnit.WEEKS)))
                    ? "Found " + (initialCount - history.size()) + " messages older than 2 weeks in history.. Discarding!\n"
                    : "";
            int finalCount = history.size();

            if (history.isEmpty()) {
                chat.sendMessage("*There are no messages to delete!*");
                return;
            }

            Consumer<Void> consumer = null;
            if (!isSilent(args)) {
                consumer = success ->
                        e.getTextChannel().getHistory().retrievePast(1).queue(h ->
                                chat.sendMessage(out + "Successfully deleted " + (fMember == null
                                        ? (h.isEmpty() ? "all" : finalCount - 1) + " messages!"
                                        : "**" + MessageUtil.stripFormatting(fMember.getUser().getName()) + "**'s messages from the past " + input + " lines!")
                                )
                        );
            }

            if (finalCount == 1) {
                history.get(0).delete().queue(consumer);
            } else if (!history.isEmpty()) {
                e.getTextChannel().deleteMessages(history).queue(consumer);
            }
        });
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
    public Map<String, String> getFlags() {
        Map<String, String> flags = new HashMap<>();
        flags.put("--silent", "Do not print the success message in chat");
        return flags;
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

    private boolean isSilent(String[] args) {
        return args[args.length - 1].equalsIgnoreCase("--silent");
    }
}
package me.dinosparkour.commands.guild.actions;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.ServerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;

abstract class ActionCommandImpl extends GuildCommand {

    protected abstract Action getAction();

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = e.getMessage().getRawContent().substring(getPrefix(e.getGuild()).length()); // Custom argument parsing
        if (allArgs.contains(" "))                                                                   //    to allow the use
            allArgs = allArgs.substring(allArgs.indexOf(" ") + 1);                                   //      of newlines

        ServerManager sm = new ServerManager(e.getGuild());
        switch (args.length) {
            case 0: // Get the current message
                String message = isJoin() ? sm.getWelcomeMessage() : sm.getFarewellMessage();
                chat.sendMessage("__Current message sent upon user " + getAction().gerund + " the guild__: "
                        + (message == null ? "None" : message));
                break;

            default:
                if (e.getMember().hasPermission(e.getTextChannel(), Permission.MESSAGE_MANAGE)) {
                    if (allArgs.equalsIgnoreCase("reset")) { // Clear the message
                        if (isJoin())
                            sm.setWelcomeMessage(null).update();
                        else
                            sm.setFarewellMessage(null).update();
                        chat.sendMessage("__Reset the " + getAction().noun + " message!__");
                    } else if (args[0].equalsIgnoreCase("channel")) { // Set the broadcast channel
                        List<TextChannel> textChannels = e.getMessage().getMentionedChannels();
                        switch (textChannels.size()) {
                            case 0:
                                if (allArgs.equalsIgnoreCase("channel reset")) { // Reset the current channel
                                    if (isJoin())
                                        sm.setWelcomeChannel(null).update();
                                    else
                                        sm.setFarewellChannel(null).update();
                                    chat.sendMessage("__Reset the " + getAction().noun + " channel!__");
                                } else {
                                    TextChannel tc = e.getGuild().getTextChannels().stream() // Get the current channel
                                            .filter(textChannel ->
                                                    textChannel.getId().equals(isJoin()
                                                            ? sm.getWelcomeChannelId() : sm.getFarewellChannelId()))
                                            .findFirst().orElse(e.getGuild().getPublicChannel());
                                    chat.sendMessage("__Current " + getAction().noun + " channel__: " + tc.getAsMention());
                                }
                                break;

                            case 1: // Exactly one mentioned channel
                                if (isJoin())
                                    sm.setWelcomeChannel(textChannels.get(0)).update();
                                else
                                    sm.setFarewellChannel(textChannels.get(0)).update();
                                chat.sendMessage("__Set the " + getAction().noun + " channel to__: " + textChannels.get(0).getAsMention());
                                break;

                            default: // Too many mentioned channels
                                chat.sendUsageMessage();
                                break;
                        }
                    } else { // Set the broadcast message
                        if (isJoin() && (sm.getWelcomeMessage() == null || !sm.getWelcomeMessage().equals(allArgs)))
                            sm.setWelcomeMessage(allArgs).update();
                        else if (!isJoin() && (sm.getFarewellMessage() == null || !sm.getFarewellMessage().equals(allArgs)))
                            sm.setFarewellMessage(allArgs).update();
                        chat.sendMessage("__Set the message sent upon " + getAction().gerund + " to__:\n" + allArgs);
                    }
                } else // No permission to set msg
                    chat.sendMessage("You need `[MESSAGE_MANAGE]` in order to set the " + getAction().noun + " message!");
                break;
        }
    }

    @Override
    public String getName() {
        return getAction().name().toLowerCase() + "msg";
    }

    @Override
    public List<String> getAlias() {
        String action = getAction().name().toLowerCase();
        String noun = getAction().noun;
        return Arrays.asList(action + "message", noun + "message", action + "msg", noun + "msg");
    }

    @Override
    public String getDescription() {
        return "Manages the message being broadcast when a new user " + getAction().name().toLowerCase() + "s the guild.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("message / channel #channel_mention / reset / channel reset");
    }

    @Override
    public Map<String, String> getVariables() {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("%user%", "the user's name");
        variables.put("%userId%", "the user's id");
        variables.put("%guild%", "the guild's name");
        variables.put("%mention%", "mention the user");
        variables.put("%usercount%", "the guild's user count");
        return variables;
    }

    private boolean isJoin() {
        return getAction() == Action.JOIN;
    }

    protected enum Action {
        JOIN("joining", "welcome"),
        LEAVE("leaving", "farewell");

        private final String gerund;
        private final String noun;

        Action(String gerund, String noun) {
            this.gerund = gerund;
            this.noun = noun;
        }
    }
}
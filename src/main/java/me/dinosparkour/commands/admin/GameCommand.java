package me.dinosparkour.commands.admin;

import me.dinosparkour.commands.impls.AdminCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.Presence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameCommand extends AdminCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        Presence p = e.getJDA().getPresence();
        switch (allArgs) {
            case "null":
            case "reset":
                p.setGame(null);
                chat.sendMessage("Reset the game!");
                break;

            default:
                p.setGame(Game.playing(allArgs));
                chat.sendMessage("Set the game to \"" + MessageUtil.stripFormatting(e.getJDA().getPresence().getGame().getName()) + "\"");
                break;
        }
    }

    @Override
    public String getName() {
        return "game";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "setgame");
    }

    @Override
    public String getDescription() {
        return "Sets the bot's current game.";
    }

    @Override
    public boolean allowsPrivate() {
        return true;
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("new game / reset");
    }

    @Override
    public int getArgMin() {
        return 1;
    }
}
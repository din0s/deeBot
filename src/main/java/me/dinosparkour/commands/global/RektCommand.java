package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class RektCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage("REKT checklist"
                + "\n"
                + "\n⬜ Not Rekt"
                + "\n✅ REKT"
                + "\n✅ REKTangle"
                + "\n✅ Tyrannosaurus REKT"
                + "\n✅ Caught REKT handed"
                + "\n✅ Singing in the REKT"
                + "\n✅ The REKT Prince of Bel-Air"
                + "\n✅ REKTflix"
                + "\n✅ REKT it like it's hot"
                + "\n✅ REKT and Roll"
                + "\n✅ REKT Paper Scissors"
                + "\n✅ REKTcraft"
                + "\n✅ Grand REKT Auto V"
                + "\n✅ Left 4 REKT"
                + "\n✅ www.REKT.com"
                + "\n✅ Pokemon: Fire REKT"
                + "\n✅ The Good, the bad, and the REKT"
                + "\n✅ shREKT"
                + "\n✅ eREKTile dysfunction");
    }

    @Override
    public String getName() {
        return "rekt";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "erekt");
    }

    @Override
    public String getDescription() {
        return "Returns the 'Rekt' copypasta.";
    }
}
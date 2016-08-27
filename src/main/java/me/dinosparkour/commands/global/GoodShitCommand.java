package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class GoodShitCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        sendMessage("\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd" +
                "\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40\ud83d\udc4c\ud83c\udffd\uD83D\uDC40 good shit go౦ԁ sHit" +
                "\ud83d\udc4c\ud83c\udffd thats ✔ some good\ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdshit right\ud83d" +
                "\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdthere\ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffd\ud83d\udc4c" +
                "\ud83c\udffd right✔there ✔✔if i do ƽaү so my self \uD83D\uDCAF i say so \uD83D\uDCAF thats what im talking " +
                "about right there right there (chorus: ʳᶦᵍʰᵗ ᵗʰᵉʳᵉ) mMMMMᎷМ\uD83D\uDCAF \ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c" +
                "\udffd \ud83d\udc4c\ud83c\udffdНO0ОଠOOOOOОଠଠOoooᵒᵒᵒᵒᵒᵒᵒᵒᵒ\ud83d\udc4c\ud83c\udffd \ud83d\udc4c\ud83c\udffd\ud83d" +
                "\udc4c\ud83c\udffd \ud83d\udc4c\ud83c\udffd \uD83D\uDCAF \ud83d\udc4c\ud83c\udffd \uD83D\uDC40 \uD83D\uDC40 " +
                "\uD83D\uDC40 \ud83d\udc4c\ud83c\udffd\ud83d\udc4c\ud83c\udffdGood shit");
    }

    @Override
    public String getName() {
        return "goodshit";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns the 'good shit' coypasta.";
    }
}
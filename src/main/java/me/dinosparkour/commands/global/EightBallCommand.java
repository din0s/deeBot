package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EightBallCommand extends GlobalCommand {

    private static final List<String> REPLIES = Arrays.asList(
            "Yes, definitely!",
            "No way..",
            "Absolutely!",
            "Absolutely.. not.",
            "Most likely.",
            "Very doubtful :<",
            "Don't count on it.",
            "My sources say no.",
            "I have my doubts..",
            "That's a terrible idea..",
            "As I see it, yes ^.^",
            "I wouldn't say so.",
            "It's scientifically proven!"
    );

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        String question = allArgs.endsWith("?") ? allArgs : allArgs.trim() + "?";
        int i = new Random().nextInt(REPLIES.size());
        String answer = REPLIES.get(i);
        chat.sendMessage((randomBool() ? "\uD83D\uDCAB" : "\uD83C\uDF1F") + " `"
                + MessageUtil.stripFormatting(question.replace("`", "")) + "` "
                + answer + " " + (randomBool() ? "\uD83C\uDFB1" : "\uD83D\uDD2E"));
    }

    @Override
    public String getName() {
        return "8ball";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns an answer to your question by the almighty 8-ball.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("question");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "will I ever win the lottery?";
    }

    private boolean randomBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
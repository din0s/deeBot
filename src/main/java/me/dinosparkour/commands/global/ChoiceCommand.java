package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.*;

public class ChoiceCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        if (!allArgs.contains(";") || allArgs.equals(":"))
            chat.sendUsageMessage();
        else {
            List<String> choices = new ArrayList<>();
            for (String s : allArgs.split(";"))
                if (!s.trim().isEmpty())
                    choices.add(s.trim());
            int count = choices.size();
            if (count == 1)
                chat.sendMessage("Please give me more than 1 option to choose from..");
            else {
                int rand = new Random().nextInt(count);
                String choice = choices.get(rand).replace("`", "");
                chat.sendMessage("\ud83e\udd14 *I'd say..* `" + MessageUtil.stripFormatting(choice) + "`!");
            }
        }
    }

    @Override
    public String getName() {
        return "choice";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "choose", "select");
    }

    @Override
    public String getDescription() {
        return "Returns a random option out of given choices.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("choices separated by ;");
    }

    @Override
    public String getExample() {
        return "my first option; second option; none!";
    }
}
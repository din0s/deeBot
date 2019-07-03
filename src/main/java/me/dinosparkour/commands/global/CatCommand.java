package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CatCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        JSONObject obj = HttpRequestUtil.getData("https://catfact.ninja/fact");
        chat.sendMessage(obj != null && obj.has("fact")
                ? obj.getJSONArray("fact").get(0).toString()
                : "There was an issue while contacting the database, try again later.");
    }

    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "catfact");
    }

    @Override
    public String getDescription() {
        return "Returns a fact about cats.";
    }

    @Override
    public int getArgMax() {
        return 0;
    }
}

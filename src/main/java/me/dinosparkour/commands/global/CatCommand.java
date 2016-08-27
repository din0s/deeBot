package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CatCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        JSONObject obj = HttpRequestUtil.getData("http://catfacts-api.appspot.com/api/facts");
        sendMessage(obj != null ? obj.getJSONArray("facts").getString(0) : "There was an issue while contacting the database, try again later.");
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
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ChuckNorrisCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        JSONObject obj = HttpRequestUtil.getData("http://api.icndb.com/jokes/random");
        if (obj != null && obj.getString("type").equals("success"))
            chat.sendMessage(obj.getJSONObject("value").getString("joke"));
        else // GET request returned error, use static joke.
            chat.sendMessage("Chuck Norris had a car accident. We have yet to find out if the car survived after the crash..");
    }

    @Override
    public String getName() {
        return "chuck";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "chucknorris", "norris");
    }

    @Override
    public String getDescription() {
        return "Returns a fact about Chuck Norris.";
    }

    @Override
    public int getArgMax() {
        return 0;
    }
}
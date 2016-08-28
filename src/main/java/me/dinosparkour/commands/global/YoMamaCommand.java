package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YoMamaCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        JSONObject obj = HttpRequestUtil.getData("http://api.yomomma.info/");
        String joke;
        if (obj != null && obj.has("joke"))
            joke = obj.getString("joke");
        else // GET request returned error, use static joke.
            joke = "Yo mama so fat her blood type is Nutella.";
        chat.sendMessageWithMentions(joke, args);
    }

    @Override
    public String getName() {
        return "joke";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "yomama", "yourmom", "yomamma", "yomomma");
    }

    @Override
    public String getDescription() {
        return "Returns a 'Yo Mamma' joke.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("user");
    }
}
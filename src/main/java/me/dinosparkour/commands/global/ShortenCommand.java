package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShortenCommand extends GlobalCommand {

    private static final String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?key=" + Info.GOOGLE_KEY;
    private final JSONObject body = new JSONObject();

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        body.put("longUrl", String.join(" ", Arrays.asList(args)));
        e.getChannel().sendTyping();
        JSONObject response = new JSONObject(HttpRequestUtil.postData(REQUEST_URL, body).getBody().toString());
        chat.sendMessage("**Shortened URL:**: <" + response.getString("id") + ">");
    }

    @Override
    public String getName() {
        return "shorten";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "shortenurl");
    }

    @Override
    public String getDescription() {
        return "Shortens the provided long URL using goo.gl";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("long URL");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "https://google.com";
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.*;

public class UnshortenCommand extends GlobalCommand {

    private static final String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?shortUrl={url}&key={key}&projection=FULL";
    private final Map<String, String> params = new HashMap<>(2);

    public UnshortenCommand() {
        params.put("key", Info.GOOGLE_KEY);
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        params.put("url", String.join(" ", Arrays.asList(args)));
        e.getChannel().sendTyping();
        JSONObject response = HttpRequestUtil.getData(REQUEST_URL, params);

        if (!response.has("longUrl")) {
            chat.sendMessage("The provided URL is invalid!");
            return;
        }

        String longUrl = response.getString("longUrl");
        String creationDate = response.getString("created");
        JSONObject analytics = response.getJSONObject("analytics").getJSONObject("allTime");
        String shortClicks = analytics.getString("shortUrlClicks");
        String output = "**Expanded URL:** <" + longUrl + ">"
                + "\nThe URL was created on **" + MessageUtil.formatDate(OffsetDateTime.parse(creationDate)) + "**"
                + "\nand it has been clicked **" + shortClicks + " times**.";
        chat.sendMessage(output);
    }

    @Override
    public String getName() {
        return "unshorten";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "unshortenurl", "expandurl", "expand");
    }

    @Override
    public String getDescription() {
        return "Unshortens the provided goo.gl URL.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("goo.gl URL");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "https://goo.gl/mR2d";
    }
}
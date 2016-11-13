package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;

import java.util.*;

public class YouTubeCommand extends GlobalCommand {

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q={q}&type=video&key={key}";
    private final Map<String, String> params = new HashMap<>(2);

    public YouTubeCommand() {
        params.put("key", Info.GOOGLE_KEY);
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        params.put("q", String.join(" ", Arrays.asList(args)));
        JSONArray arr = HttpRequestUtil.getData(API_URL, params).getJSONArray("items");

        if (arr.length() == 0)
            chat.sendMessage("Your query returned zero results from YouTube!");
        else {
            String watchUrl = arr.getJSONObject(0).getJSONObject("id").getString("videoId");
            chat.sendMessage("https://youtu.be/" + watchUrl);
        }
    }

    @Override
    public String getName() {
        return "ytsearch";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "yt", "youtube", "youtubesearch");
    }

    @Override
    public String getDescription() {
        return "Returns the first video from YouTube that matches the given query.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("query");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    @Override
    public String getExample() {
        return "discord don't be a broom";
    }
}
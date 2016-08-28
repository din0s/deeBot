package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class YouTubeCommand extends GlobalCommand {

    private static final String API_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q={q}&type=video&key={key}";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        Map<String, String> params = new HashMap<>();
        try {
            params.put("key", Info.GOOGLE_KEY);
            params.put("q", URLEncoder.encode(allArgs, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        } // UTF-8 is valid and will not throw an exception
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
}
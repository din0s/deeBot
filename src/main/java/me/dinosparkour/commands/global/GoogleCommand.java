package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GoogleCommand extends GlobalCommand {

    private static final String REQUEST_URL = "https://www.googleapis.com/customsearch/v1?q={query}&cx={cx}&num=1&key={key}";
    private final Map<String, String> params = new HashMap<>(3);

    public GoogleCommand() {
        params.put("key", Info.GOOGLE_KEY);
        params.put("cx", Info.GOOGLE_CX);
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String q = String.join(" ", Arrays.asList(args));
        params.put("query", q);

        e.getChannel().sendTyping();
        JSONObject response = HttpRequestUtil.getData(REQUEST_URL, params);

        if (!response.has("items")) {
            chat.sendMessage("The bot has reached its daily quota for today! :(");
            return;
        }

        JSONArray items = response.getJSONArray("items");
        if (items.length() == 0)
            chat.sendMessage("Your query returned zero results from Google!");
        else {
            JSONObject result = items.getJSONObject(0);
            String snippet = result.getString("snippet");
            String link = result.getString("link");
            String output = "**`Google Search Result for:`** " + MessageUtil.stripFormatting(q) + "\n<" + link + ">\n" + snippet.replace("\n", "");
            if (output.endsWith("..."))
                output += " _[read more]_";
            chat.sendMessage(output);
        }
    }

    @Override
    public String getName() {
        return "google";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "g");
    }

    @Override
    public String getDescription() {
        return "Returns information obtained by Google about the defined query.";
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
        return "Discord";
    }
}
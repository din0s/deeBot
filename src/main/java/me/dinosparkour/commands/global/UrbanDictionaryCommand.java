package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UrbanDictionaryCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        chat.sendMessage(getUrbanDefinition(String.join(" ", Arrays.asList(args))));
    }

    @Override
    public String getName() {
        return "define";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "urban", "ud", "definition", "urbandictionary");
    }

    @Override
    public String getDescription() {
        return "Returns the definition of the requested word/phrase.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("query");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    private String getUrbanDefinition(String query) {
        if (query.equals("+")) query = "%2B";

        JSONObject obj = null;
        try {
            obj = HttpRequestUtil.getData("http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        } // UTF-8 is valid and will not throw an exception

        assert obj != null;
        if (!obj.has("list"))
            return "*Couldn't fetch results for \"" + MessageUtil.stripFormatting(query) + "\"!*";
        JSONArray arr = obj.getJSONArray("list");
        if (arr.length() == 0)
            return "*No results found for \"" + MessageUtil.stripFormatting(query) + "\"!*";

        JSONObject result = arr.getJSONObject(0);
        String word = result.getString("word");
        String def = result.getString("definition");
        String example = result.getString("example");
        //int thumbsUp = result.getInt("thumbs_up");
        //int thumbsDown = result.getInt("thumbs_down");

        StringBuilder definition = new StringBuilder("__**`-=Urban Dictionary: " + word + "=-`**__\n");
        definition.append("\n**Definition:**\n").append(def).append("\n");
        assert example != null;
        if (!example.equals(""))
            definition.append("\n**Example:**\n").append(example).append("\n");
        //definition.append("\n[**+**] " + thumbsUp + " / [**-**] " + thumbsDown);

        if (definition.toString().length() > 2000)
            return "The requested definition cannot fit in one message, "
                    + "please click on this link: <" + result.getString("permalink") + ">";

        return definition.toString();
    }
}
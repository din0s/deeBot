package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YoMamaCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        JSONObject obj = HttpRequestUtil.getData("http://api.yomomma.info/");
        chat.sendMessageWithMentions(args, (obj != null && obj.has("joke")
                ? obj.getString("joke")
                : "Yo mama so fat her blood type is Nutella."));
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
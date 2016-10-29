package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HastebinCommand extends GlobalCommand {

    private static final String HASTEBIN_URL = "http://hastebin.com/documents";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String flag = "txt";
        String lastArg = args[args.length - 1];
        String input = stripFirstArg(e.getMessage().getContent());
        if (lastArg.startsWith("--") && args.length > 1) { // Flag detection
            flag = lastArg.substring(2);
            input = stripLastArg(input);
        }

        String key = new JSONObject(HttpRequestUtil.postData(HASTEBIN_URL, input).getBody().toString()).getString("key");
        chat.sendMessage(e.getAuthor().getAsMention() + ": http://hastebin.com/" + key + "." + flag, msg -> {
            if (e.getTextChannel().checkPermission(e.getJDA().getSelfInfo(), Permission.MESSAGE_MANAGE)) {
                e.getMessage().deleteMessage();
            }
        });
    }

    @Override
    public String getName() {
        return "hastebin";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Upload text directly to HasteBin.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("text");
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("--language flag");
    }

    @Override
    public Map<String, String> getFlags() {
        return Collections.singletonMap("--language", "The language extension to be used for the result");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    private String stripFirstArg(String input) {
        return input.substring(input.indexOf(' ') + 1);
    }

    private String stripLastArg(String input) {
        return input.substring(0, input.lastIndexOf(' '));
    }
}
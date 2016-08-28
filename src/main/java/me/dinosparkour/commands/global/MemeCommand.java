package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class MemeCommand extends GlobalCommand {

    private static final Set<String> MEMES = new TreeSet<>();
    private String allArgs;

    public MemeCommand() {
        JSONObject obj = HttpRequestUtil.getData("http://memegen.link/templates/");
        obj.names().forEach(v -> {
            String url = obj.getString(String.valueOf(v));
            MEMES.add(url.substring(url.lastIndexOf("/") + 1));
        });
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        this.allArgs = String.join(" ", Arrays.asList(args));
        String type = args[0].contains("|") ? args[0].substring(0, args[0].indexOf("|")) : args[0];
        if (type.equalsIgnoreCase("help") || type.equalsIgnoreCase("list") || invalidType(type)) {
            chat.sendMessage("Here are the valid meme types.```xl\n"
                    + String.join(", ", MEMES.stream().collect(Collectors.toList())) + "```");
            return;
        }

        if (!enoughArgs()) { // Make sure there are exactly two separators
            chat.sendUsageMessage();
            return;
        }

        String[] lines = allArgs.substring(type.length()).split("\\|");
        String line1 = urlify(lines.length > 1 ? lines[1] : "");
        String line2 = urlify(lines.length > 2 ? lines[2] : "");

        String requestUrl = String.format("http://memegen.link/%s/%s/%s.jpg", type, line1, line2);

        File imgFile = new File(e.getAuthorName() + System.currentTimeMillis() + ".jpg");
        e.getChannel().sendTyping();
        try {
            FileUtils.copyInputStreamToFile(HttpRequestUtil.getInputStream(requestUrl), imgFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        e.getChannel().sendFileAsync(imgFile, new MessageBuilder().appendString("Here's your meme:").build(), m -> imgFile.delete());
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Returns a meme generated based on the type.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList(" type | first line | second line ");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

    private boolean enoughArgs() {
        return StringUtils.countMatches(allArgs, "|") == 2;
    }

    private boolean invalidType(String type) {
        return enoughArgs() && !MEMES.contains(type);
    }

    private String urlify(String input) {
        String output = null;
        switch (input) {
            case "":
                output = "_";
                break;

            default:
                try {
                    output = URLEncoder.encode(input.replace("\"", "''")
                                    .replace("~q", "~-q")
                                    .replace("~p", "~-p")
                                    .replace("?", "~q")
                                    .replace("%", "~p")
                                    .replace("-", "--")
                                    .replace("_", "__")
                                    .trim().replace(" ", "_"),
                            "UTF-8");

                } catch (UnsupportedEncodingException ignored) {
                }
                break;
        }
        return output;
    }
}
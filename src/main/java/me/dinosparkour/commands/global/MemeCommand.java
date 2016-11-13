package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

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

        if (e.isFromType(ChannelType.PRIVATE)
                || e.getGuild().getSelfMember().hasPermission(e.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            String[] lines = allArgs.substring(type.length()).split("\\|");
            String line1 = urlify(lines.length > 1 ? lines[1] : "");
            String line2 = urlify(lines.length > 2 ? lines[2] : "");

            String requestUrl = String.format("http://memegen.link/%s/%s/%s.jpg", type, line1, line2);

            e.getChannel().sendTyping().queue();
            e.getChannel().sendFile(HttpRequestUtil.getInputStream(requestUrl), "PNG",
                    new MessageBuilder().appendString("Here's your meme:").build()).queue();

        } else chat.sendMessage("The bot needs `[ATTACH_FILES]` in order to be able to send memes.");
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

    @Override
    public String getExample() {
        return "kermit | but that's | none of my business";
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
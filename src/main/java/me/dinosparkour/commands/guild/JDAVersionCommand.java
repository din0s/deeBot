package me.dinosparkour.commands.guild;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDAVersionCommand extends GuildCommand {

    //private static final String BINTRAY_URL = "https://api.bintray.com/packages/dv8fromtheworld/maven/{package}/versions/_latest";
    private static final String DOWNLOADS_URL = "http://home.dv8tion.net:8080/job/{package}/lastBuild/api/json";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        if (e.getGuild().getId().equals("125227483518861312")
                || e.getGuild().getId().equals("81384788765712384")
                || e.getAuthor().getId().equals(Info.AUTHOR_ID)) {

            e.getChannel().sendTyping().queue();
            StringBuilder sb = new StringBuilder();

            Map<String, String> params = new HashMap<>(1);
            params.put("package", "JDA");
            sendRequest(sb, params);

            params.replace("package", "JDA-Player");
            sendRequest(sb, params);

            // No further updates to the Legacy build
            sb.append("__**JDA Legacy**__\nLatest Build: **2.3.0_379**\nRelease Date: `12/11/2016 00:02:58 UTC`");
            chat.sendMessage(sb.toString());
        }
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "jdaversion");
    }

    @Override
    public String getDescription() {
        return "Returns information about JDA's latest builds.";
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    private void sendRequest(StringBuilder sb, Map<String, String> params) {
        //JSONObject obj = HttpRequestUtil.getData(BINTRAY_URL, params);
        //sb.append(getInfo(params.get("package"), obj.getString("name"), OffsetDateTime.parse(obj.getString("updated"))));

        JSONObject obj = HttpRequestUtil.getData(DOWNLOADS_URL, params);
        String name = params.get("package");
        final String[] version = new String[1];
        JSONArray artifacts = obj.getJSONArray("artifacts");
        artifacts.forEach(artifact -> {
            String fileName = ((JSONObject) artifact).getString("fileName");
            if (version[0] == null
                    || StringUtils.countMatches(fileName, '-') < StringUtils.countMatches(version[0], '-'))
                version[0] = fileName;
        });
        version[0] = version[0].substring(version[0].lastIndexOf("-") + 1, version[0].length() - 4);
        OffsetDateTime updatedAt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(obj.getLong("timestamp")), ZoneId.of("UTC"));
        sb.append(getInfo(name, version[0], updatedAt));
    }

    private String getInfo(String name, String version, OffsetDateTime updatedAt) {
        return "__**" + name + "**__\n"
                + "Latest Build: **" + version + "**\n"
                + "Release Date: **`" + MessageUtil.formatDate(updatedAt) + "`**\n\n";
    }
}
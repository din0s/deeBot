package me.dinosparkour.managers.listeners;

import me.dinosparkour.Info;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StatsManager extends ListenerAdapter {

    private static final String DISCORD_BOTS = "https://bots.discord.pw/api/bots/" + Info.BOT_ID + "/stats";
    private static final String CARBONITEX = "https://www.carbonitex.net/discord/data/botdata.php";

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        String serverCount = String.valueOf(e.getJDA().getGuilds().size());
        String abalKey = Info.ABAL_KEY;
        String carbonKey = Info.CARBON_KEY;

        if (!abalKey.isEmpty()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", abalKey);

            JSONObject data = new JSONObject()
                    .put("server_count", serverCount);

            HttpRequestUtil.postData(DISCORD_BOTS, headers, data);
        }

        if (!carbonKey.isEmpty()) {
            JSONObject data = new JSONObject()
                    .put("servercount", serverCount)
                    .put("key", carbonKey);

            HttpRequestUtil.postData(CARBONITEX, data);
        }
    }
}
package me.dinosparkour.managers.listeners;

import me.dinosparkour.Info;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StatsManager extends ListenerAdapter {

    private static final String DISCORD_BOTS = "https://bots.discord.pw/api/bots/" + Info.BOT_ID + "/stats";
    private static final String CARBONITEX = "https://www.carbonitex.net/discord/data/botdata.php";
    private static long read = 0;
    private static long sent = 0;

    public static long amountRead() {
        return read;
    }

    public static long amountSent() {
        return sent;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        String abalKey = Info.ABAL_KEY;
        String carbonKey = Info.CARBON_KEY;

        if (!abalKey.isEmpty()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", abalKey);

            JSONObject data = new JSONObject()
                    .put("shard_id", e.getJDA().getShardInfo().getShardId())
                    .put("shard_count", e.getJDA().getShardInfo().getShardTotal())
                    .put("server_count", e.getJDA().getGuilds().size());

            HttpRequestUtil.postData(DISCORD_BOTS, headers, data);
        }

        if (!carbonKey.isEmpty()) {
            JSONObject data = new JSONObject()
                    .put("servercount", ShardManager.getInstances().stream().mapToLong(jda -> jda.getGuilds().size()).sum())
                    .put("key", carbonKey);

            HttpRequestUtil.postData(CARBONITEX, data);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor() != null && e.getAuthor().equals(e.getJDA().getSelfUser())) {
            sent++;
        } else {
            read++;
        }
    }
}
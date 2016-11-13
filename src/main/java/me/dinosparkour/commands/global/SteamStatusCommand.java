package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SteamStatusCommand extends GlobalCommand {

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        e.getChannel().sendTyping().queue();
        JSONObject statusObj = HttpRequestUtil.getData("https://steamgaug.es/api/v2");
        if (statusObj == null) {
            chat.sendMessage("**The Steam API is offline.** ❌");
            return;
        }

        StringBuilder sb = new StringBuilder("```diff\n");
        Arrays.stream(App.values())
                .filter(App::isNotGame)
                .forEach(app -> sb.append(getStatus(app, statusObj)));

        if (args.length > 0) {
            String param = String.join(" ", Arrays.asList(args)).toLowerCase();
            try {
                App game = App.valueOf(param.toUpperCase());
                sb.append("+\n").append(getStatus(game, statusObj));
            } catch (IllegalArgumentException ignored) {
            }
        }
        chat.sendMessage(sb.append("```").toString());
    }

    @Override
    public String getName() {
        return "steamstatus";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "steam");
    }

    @Override
    public String getDescription() {
        return "Checks Steam's server status.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("tf2 / dota2 / csgo");
    }

    private String getStatus(App app, JSONObject statusObj) {
        JSONObject obj = statusObj.getJSONObject(app.getKey());
        if (!app.isNotGame()) obj = obj.getJSONObject(app.getId());
        boolean online = obj.getInt("online") == 1;
        boolean hasError = obj.has("error") && !obj.get("error").equals("No Error");
        boolean hasTime = !hasError && obj.has("time");
        return (online ? "+ " : "- ")
                + app.getName() + " is "
                + (online ? "online." : "offline.")
                + (hasTime ? " (" + obj.getInt("time") + "ms)" : "")
                + (hasError ? " (" + obj.getString("error") + ")" : "")
                + "\n";
    }

    private enum App {
        STEAM("Steam", "ISteamClient"),
        STORE("Store", "SteamStore"),
        COMMUNITY("Community", "SteamCommunity"),
        API("API", "ISteamUser"),
        TF2("TF2", 440),
        DOTA2("Dota 2", 570),
        CSGO("CS:GO", 730);

        private final String name;
        private final String key;
        private final int id;

        App(String name, String key) {
            this.name = name;
            this.key = key;
            this.id = 0;
        }

        App(String name, int id) {
            this.name = name;
            this.key = "ISteamGameCoordinator";
            this.id = id;
        }

        String getName() {
            return name;
        }

        String getKey() {
            return key;
        }

        String getId() {
            return String.valueOf(id);
        }

        boolean isNotGame() {
            return id == 0;
        }
    }
}
package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.utils.HttpRequestUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DiscordStatusCommand extends GlobalCommand {

    private final static String STATUS_WEBSITE = "**https://status.discordapp.com**";
    private final static String CHECK = " \u2705";
    private final static String EXCLAMATION = "\u2757";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        e.getChannel().sendTyping().queue();
        JSONObject response = HttpRequestUtil.getData("https://srhpyqt94yxb.statuspage.io/api/v2/summary.json");

        // OVERVIEW
        JSONObject status = response.getJSONObject("status");
        String desc = status.getString("description");
        StringBuilder report = new StringBuilder(STATUS_WEBSITE);
        if (!isOperational(desc)) {
            report.append("\n_")
                    .append("                          ")
                    .append("~~~")
                    .append("                          ")
                    .append("\n")
                    .append(desc)
                    .append("_");
            appendImpact(report, status.getString("indicator"), false);
        }
        report.append("\n\n");

        // INCIDENTS
        JSONArray incidents = response.getJSONArray("incidents");
        incidents.forEach(obj -> {
            JSONObject json = (JSONObject) obj;
            String name = json.getString("name");
            report.append("**~ ").append(name).append("**");
            if (!isOperational(name)) {
                appendImpact(report, json.getString("impact"), true);
            }
            report.append("\n\n");
        });

        // COMPONENTS
        JSONArray components = response.getJSONArray("components");
        Arrays.asList(Component.values()).forEach(component -> {
            if (components.length() < component.index) return; // GET Request failed to return enough components
            report.append("__").append(component.name).append(" Status__: ")
                    .append(getComponentStatus(components.getJSONObject(component.index)).replace("_", " "))
                    .append("\n");
        });

        // LENGTH CHECK & SEND
        if (report.length() > 2000) {
            report.delete(0, report.length())
                    .append("The detailed report is too long!")
                    .append("Please visit " + STATUS_WEBSITE);
        }
        chat.sendMessage(report.toString());
    }

    @Override
    public String getName() {
        return "discordstatus";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "apistatus");
    }

    @Override
    public String getDescription() {
        return "Provides information related to the Discord API.";
    }

    private String capitalize(String s) {
        String first = s.charAt(0) + "";
        return first.toUpperCase() + s.substring(1);
    }

    private boolean isOperational(String s) {
        return s.endsWith("Operational");
    }

    private String getComponentStatus(JSONObject comp) {
        String status = capitalize(comp.getString("status"));
        return status + (isOperational(status) ? CHECK : EXCLAMATION);
    }

    private void appendImpact(StringBuilder sb, String impact, boolean excl) {
        sb.append(" - `").append(capitalize(impact)).append(" Impact`");
        if (excl) {
            sb.append(EXCLAMATION);
        }
    }

    private enum Component {
        API("API", 0),
        GATEWAY("Gateway", 3),
        CLOUDFLARE("CloudFlare", 4),
        VOICE("Voice", 7);

        private final String name;
        private final int index;

        Component(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}
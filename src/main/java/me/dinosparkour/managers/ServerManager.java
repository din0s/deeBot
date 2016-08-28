package me.dinosparkour.managers;

import me.dinosparkour.Info;
import me.dinosparkour.utils.IOUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ServerManager {

    private static final File DATA_FOLDER = new File("data");
    private static final Map<String, JSONObject> DATABASE = new HashMap<>();            // Map<GuildId, Data>
    private static final Map<String, String> PREFIXES = new HashMap<>();                // Map<GuildId, Prefix> (never-null prefix)
    private static final Map<String, JSONArray> CUSTOM_COMMANDS = new HashMap<>();      // Map<GuildId, CommandList>
    private final Map<DataType, String> dataMap = new HashMap<>();                      // Map<DataType, Value> (possibly-null value)
    private final String guildId;

    public ServerManager(Guild guild) {
        this.guildId = guild.getId();
        JSONObject obj = DATABASE.containsKey(guildId) ? DATABASE.get(guildId) : new JSONObject();
        Arrays.stream(DataType.values())
                .filter(data -> data != DataType.COMMANDS)
                .forEach(dataType -> dataMap.put(dataType, obj.has(dataType.configEntry)
                        ? obj.getString(dataType.configEntry) : null));
    }

    // Database Initializer
    public static void init() {
        if (IOUtil.createFolder(DATA_FOLDER))
            IOUtil.readDataFileBlocking("Guild Data", () -> {
                try {
                    Files.walk(Paths.get(DATA_FOLDER.getPath()))
                            .filter(path -> path.getFileName().toString().endsWith(".json")) // Collect readable JSON files
                            .forEach(path -> {
                                String fileName = path.getFileName().toString();
                                String guildId = fileName.substring(0, fileName.length() - ".json".length()); // Extract the id by removing the extension
                                DATABASE.put(guildId, IOUtil.readJsonFromFile(path.toFile()));

                                // Add the prefix to our map
                                String prefix = DATABASE.get(guildId).has(DataType.PREFIX.configEntry)
                                        ? DATABASE.get(guildId).getString(DataType.PREFIX.configEntry) : null;
                                if (prefix != null) PREFIXES.put(guildId, prefix);

                                // Add the custom commands to our map
                                JSONArray array = DATABASE.get(guildId).has(DataType.COMMANDS.configEntry)
                                        ? DATABASE.get(guildId).getJSONArray(DataType.COMMANDS.configEntry) : null;
                                if (array != null) CUSTOM_COMMANDS.put(guildId, array);
                            });
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
    }

    // Static Getters
    public static String getDataDir() {
        return DATA_FOLDER + File.separator;
    }

    public static Map<String, String> getPrefixes() {
        return Collections.unmodifiableMap(PREFIXES);
    }

    // Data Existence Check
    public static boolean hasData(Guild g) {
        return DATABASE.containsKey(g.getId());
    }

    // Auto Role
    public String getAutoRoleId() {
        return dataMap.get(DataType.AUTOROLE_ID);
    }

    public ServerManager setAutoRole(Role r) {
        dataMap.replace(DataType.AUTOROLE_ID, r != null ? r.getId() : null);
        return this;
    }

    // Welcome Message
    public String getWelcomeMessage() {
        return dataMap.get(DataType.WELCOME_MESSAGE);
    }

    public ServerManager setWelcomeMessage(String msg) {
        dataMap.replace(DataType.WELCOME_MESSAGE, msg);
        return this;
    }

    // Farewell Message
    public String getFarewellMessage() {
        return dataMap.get(DataType.FAREWELL_MESSAGE);
    }

    public ServerManager setFarewellMessage(String msg) {
        dataMap.replace(DataType.FAREWELL_MESSAGE, msg);
        return this;
    }

    // Welcome Channel
    public String getWelcomeChannelId() {
        return dataMap.get(DataType.WELCOME_CHANNEL_ID);
    }

    public ServerManager setWelcomeChannel(TextChannel channel) {
        dataMap.put(DataType.WELCOME_CHANNEL_ID, channel != null ? channel.getId() : null);
        return this;
    }

    // Farewell Channel
    public String getFarewellChannelId() {
        return dataMap.get(DataType.FAREWELL_CHANNEL_ID);
    }

    public ServerManager setFarewellChannel(TextChannel channel) {
        dataMap.put(DataType.FAREWELL_CHANNEL_ID, channel != null ? channel.getId() : null);
        return this;
    }

    // Prefix
    public String getPrefix() {
        return PREFIXES.containsKey(guildId) ? PREFIXES.get(guildId) : Info.DEFAULT_PREFIX;
    }

    public ServerManager setPrefix(String prefix) {
        switch (prefix) {
            case Info.DEFAULT_PREFIX:
                dataMap.put(DataType.PREFIX, null);
                PREFIXES.remove(guildId);
                break;

            default:
                dataMap.put(DataType.PREFIX, prefix);
                PREFIXES.put(guildId, prefix);
                break;
        }
        return this;
    }

    // Custom Command Existence Check
    public boolean isValid(String name) {
        if (name == null || name.isEmpty())
            return false;
        if (!CUSTOM_COMMANDS.containsKey(guildId))
            return false;
        for (Object o : CUSTOM_COMMANDS.get(guildId)) {
            if (((JSONObject) o).getString("name").equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    // Custom Command Getters
    public Map<String, List<String>> getCommands() {
        Map<String, List<String>> cmds = new HashMap<>();
        if (!CUSTOM_COMMANDS.containsKey(guildId))
            return cmds;

        CUSTOM_COMMANDS.get(guildId).forEach(obj -> {
            String name = ((JSONObject) obj).getString("name");
            cmds.put(name, getCommandResponses(name));
        });
        return cmds;
    }

    public List<String> getCommandResponses(String cmdName) {
        List<String> responses = new ArrayList<>();
        CUSTOM_COMMANDS.get(guildId).forEach(o -> {
            JSONObject obj = (JSONObject) o;
            if (!cmdName.equalsIgnoreCase(obj.getString("name"))) return;
            obj.getJSONArray("responses").forEach(response -> responses.add(String.valueOf(response)));
        });
        return responses;
    }

    // Custom Command Creator
    public ServerManager addCommand(JSONObject obj) {
        JSONArray array = CUSTOM_COMMANDS.containsKey(guildId)
                ? CUSTOM_COMMANDS.get(guildId) : new JSONArray();
        array.put(obj);
        CUSTOM_COMMANDS.put(guildId, array);
        return this;
    }

    // Custom Command Deleter
    public ServerManager deleteCommand(String name) {
        if (isValid(name)) {
            JSONArray array = CUSTOM_COMMANDS.get(guildId);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getString("name").equalsIgnoreCase(name))
                    array.remove(i);
            }

            if (array.length() == 0)
                CUSTOM_COMMANDS.remove(guildId);
            else
                CUSTOM_COMMANDS.put(guildId, array);
        }
        return this;
    }

    // Update Method
    public void update() {
        JSONObject obj = new JSONObject();
        dataMap.entrySet().stream()
                .filter(set -> set.getValue() != null)
                .forEach(set -> obj.put(set.getKey().configEntry, set.getValue()));

        CUSTOM_COMMANDS.values()
                .forEach(array -> obj.put(DataType.COMMANDS.configEntry, array));

        File guildFile = new File(DATA_FOLDER.getAbsolutePath() + File.separator + guildId + ".json");
        if (obj.length() != 0) { // JSONObject isn't empty
            DATABASE.put(guildId, obj);
            IOUtil.writeJsonToFile(guildFile, obj, true);
        } else IOUtil.deleteFile(guildFile); // Delete the empty file
    }

    private enum DataType {
        WELCOME_MESSAGE("welcome"),
        FAREWELL_MESSAGE("farewell"),
        AUTOROLE_ID("role"),
        WELCOME_CHANNEL_ID("join_channel"),
        FAREWELL_CHANNEL_ID("leave_channel"),
        PREFIX("prefix"),
        COMMANDS("cmds");

        private final String configEntry;

        DataType(String configEntry) {
            this.configEntry = configEntry;
        }
    }
}
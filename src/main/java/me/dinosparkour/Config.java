package me.dinosparkour;

import me.dinosparkour.utils.IOUtil;
import org.json.JSONObject;

import java.io.File;

class Config {

    private final File configFile = new File("config.json");
    private JSONObject configObject;

    Config() {
        if (!configFile.exists()) {
            create(); // If the config.json file doesn't exist, generate it.
            System.out.println("Created a config file. Please fill in the credentials.");
            System.exit(0);
        }

        JSONObject object = IOUtil.readJsonFromFile(configFile);
        if (object.has("token")) {
            configObject = object;
        } else {
            create(); // If the token value is missing, regenerate the config file.
            System.err.println("The token value is missing in the config file! Regenerating..");
            System.exit(1);
        }
    }

    String getValue(String key) {
        return configObject == null ? null : configObject.getString(key);
    }

    private void create() {
        IOUtil.writeJsonToFile(configFile, new JSONObject()
                .put("abal", "")
                .put("carbon", "")
                .put("cx", "")
                .put("google", "")
                .put("token", ""));
    }
}
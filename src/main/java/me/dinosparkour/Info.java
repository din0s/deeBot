package me.dinosparkour;

import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.JDAInfo;

import java.lang.management.ManagementFactory;

public class Info {

    public static final String DEFAULT_PREFIX = "!!";
    public static final String VERSION = "2.2.6";
    public static final String LIBRARY = "JDA #" + JDAInfo.VERSION;
    public static final String AUTHOR_ID = "98457903660269568";
    public static final String BOT_ID = "137904252214444032";

    private static final Config CONFIG = new Config();                 // config.json
    public static final String ABAL_KEY = CONFIG.getValue("abal");     // https://bots.discord.pw
    public static final String CARBON_KEY = CONFIG.getValue("carbon"); // https://www.carbonitex.net/discord/bots
    public static final String GOOGLE_CX = CONFIG.getValue("cx");      //
    public static final String GOOGLE_KEY = CONFIG.getValue("google"); // https://developers.google.com/
    static final String TOKEN = CONFIG.getValue("token");

    public static String getUptime() {
        return MessageUtil.formatTime(ManagementFactory.getRuntimeMXBean().getUptime());
    }
}
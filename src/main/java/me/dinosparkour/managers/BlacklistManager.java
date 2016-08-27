package me.dinosparkour.managers;

import me.dinosparkour.utils.IOUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistManager {

    private static final List<String> BLACKLIST = new ArrayList<>();
    private static final File BLACKLIST_FILE = new File(ServerManager.getDataDir() + "blacklist.txt");

    public static void init() {
        try {
            if (!BLACKLIST_FILE.exists() && !BLACKLIST_FILE.createNewFile()) {
                System.err.println("Failed to create the blacklist file!");
                return;
            }
            BLACKLIST.addAll(IOUtil.readLinesFromFile(BLACKLIST_FILE));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isBlacklisted(MessageChannel channel) {
        return !(channel instanceof PrivateChannel) && BLACKLIST.contains(channel.getId());
    }

    public static void addToBlacklist(TextChannel channel) {
        if (isBlacklisted(channel)) return;
        BLACKLIST.add(channel.getId());
        IOUtil.writeLinesToFile(BLACKLIST_FILE, Collections.singletonList(channel.getId()), true);
    }

    public static void removeFromBlackList(TextChannel channel) {
        BLACKLIST.remove(channel.getId());
        IOUtil.writeLinesToFile(BLACKLIST_FILE, BLACKLIST, false);
    }

    public static List<String> getBlacklistedChannelIds(Guild guild) {
        return BLACKLIST.stream()
                .filter(id -> {
                    TextChannel tc = guild.getJDA().getTextChannelById(id);
                    return tc != null && guild.getTextChannels().contains(tc);
                }).collect(Collectors.toList());
    }
}
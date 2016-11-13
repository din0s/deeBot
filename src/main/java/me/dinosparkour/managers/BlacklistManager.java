package me.dinosparkour.managers;

import me.dinosparkour.utils.IOUtil;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistManager {

    private static final List<String> BLACKLIST = new ArrayList<>();
    private static final File BLACKLIST_FILE = new File(ServerManager.getDataDir() + "blacklist.txt");

    public static void init() {
        if (IOUtil.createFile(BLACKLIST_FILE)) {
            BLACKLIST.addAll(IOUtil.readLinesFromFile(BLACKLIST_FILE));
        }
    }

    public static boolean isBlacklisted(MessageChannel channel) {
        return channel.getType().equals(ChannelType.TEXT) && BLACKLIST.contains(channel.getId());
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
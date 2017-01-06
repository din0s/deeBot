package me.dinosparkour.managers.listeners;

import me.dinosparkour.Info;
import me.dinosparkour.commands.CommandRegistry;
import me.dinosparkour.commands.admin.*;
import me.dinosparkour.commands.global.*;
import me.dinosparkour.commands.guild.*;
import me.dinosparkour.commands.guild.actions.AutoRoleCommand;
import me.dinosparkour.commands.guild.actions.FarewellMessageCommand;
import me.dinosparkour.commands.guild.actions.WelcomeMessageCommand;
import me.dinosparkour.commands.guild.roles.AddRoleCommand;
import me.dinosparkour.commands.guild.roles.RemoveRoleCommand;
import me.dinosparkour.commands.guild.roles.RoleCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;
import java.util.stream.Collectors;

public class ShardManager extends ListenerAdapter {

    private static final CommandRegistry REGISTRY = new CommandRegistry();
    private static final ListenerAdapter[] LISTENERS = {
            //Commands
            // ADMIN
            REGISTRY.addCommand(new CleanupCommand()),
            REGISTRY.addCommand(new EvalCommand()),
            REGISTRY.addCommand(new GameCommand()),
            REGISTRY.addCommand(new GetInviteCommand()),
            REGISTRY.addCommand(new ShutdownCommand()),

            // GLOBAL
            REGISTRY.addCommand(new CatCommand()),
            REGISTRY.addCommand(new ChangelogCommand()),
            REGISTRY.addCommand(new ChoiceCommand()),
            REGISTRY.addCommand(new ChuckNorrisCommand()),
            REGISTRY.addCommand(new CoinCommand()),
            REGISTRY.addCommand(new DiscordStatusCommand()),
            REGISTRY.addCommand(new EchoCommand()),
            REGISTRY.addCommand(new EightBallCommand()),
            REGISTRY.addCommand(new GoodShitCommand()),
            REGISTRY.addCommand(new GoogleCommand()),
            REGISTRY.addCommand(new HastebinCommand()),
            REGISTRY.addCommand(new HelpCommand()),
            REGISTRY.addCommand(new InfoCommand()),
            REGISTRY.addCommand(new InviteCommand()),
            REGISTRY.addCommand(new JDAVersionCommand()),
            REGISTRY.addCommand(new LennyCommand()),
            REGISTRY.addCommand(new MemeCommand()),
            REGISTRY.addCommand(new PatreonCommand()),
            REGISTRY.addCommand(new PingCommand()),
            REGISTRY.addCommand(new RektCommand()),
            REGISTRY.addCommand(new ReminderCommand()),
            REGISTRY.addCommand(new ReverseCommand()),
            REGISTRY.addCommand(new SaltCommand()),
            REGISTRY.addCommand(new ShardCommand()),
            REGISTRY.addCommand(new ShortenCommand()),
            REGISTRY.addCommand(new StatsCommand()),
            REGISTRY.addCommand(new SteamStatusCommand()),
            REGISTRY.addCommand(new SupportCommand()),
            REGISTRY.addCommand(new UnshortenCommand()),
            REGISTRY.addCommand(new UptimeCommand()),
            REGISTRY.addCommand(new UrbanDictionaryCommand()),
            REGISTRY.addCommand(new WeedCommand()),
            REGISTRY.addCommand(new WhoisCommand()),
            REGISTRY.addCommand(new YoMamaCommand()),
            REGISTRY.addCommand(new YouTubeCommand()),

            // GUILD
            REGISTRY.addCommand(new AddRoleCommand()),
            REGISTRY.addCommand(new AnnouncementCommand()),
            REGISTRY.addCommand(new AutoRoleCommand()),
            REGISTRY.addCommand(new BanCommand()),
            REGISTRY.addCommand(new BanListCommand()),
            REGISTRY.addCommand(new BlacklistCommand()),
            REGISTRY.addCommand(new CustomCmdCommand()),
            REGISTRY.addCommand(new FarewellMessageCommand()),
            REGISTRY.addCommand(new GuildInfoCommand()),
            REGISTRY.addCommand(new KickCommand()),
            REGISTRY.addCommand(new LeaveCommand()),
            REGISTRY.addCommand(new PrefixCommand()),
            REGISTRY.addCommand(new PurgeCommand()),
            REGISTRY.addCommand(new RemoveRoleCommand()),
            REGISTRY.addCommand(new RoleCommand()),
            REGISTRY.addCommand(new UnbanCommand()),
            REGISTRY.addCommand(new UserSearchCommand()),
            REGISTRY.addCommand(new WelcomeMessageCommand()),

            // Managers
            new ActionManager(),
            new CustomCmdManager(),
            new InviteManager(),
            new StatsManager()
    };

    private static final Map<Integer, JDA> instances = new HashMap<>();

    public static Collection<JDA> getInstances() {
        return instances.values();
    }

    public static List<JDA> getInstanceList() {
        return new LinkedList<>(getInstances());
    }

    public static List<User> getGlobalUsers() {
        return getInstances().stream().flatMap(jda -> jda.getUsers().stream()).collect(Collectors.toList());
    }

    public static JDA getInstanceWithChannel(String id) {
        return getInstances().stream().filter(jda -> jda.getTextChannelById(id) != null
                || jda.getPrivateChannelById(id) != null
                || jda.getVoiceChannelById(id) != null).findAny().orElse(null);
    }

    public static JDA getInstanceWithGuild(String id) {
        return getInstances().stream().filter(jda -> jda.getGuildById(id) != null)
                .findAny().orElse(null);
    }

    /*
    public static JDA getInstanceWithUser(String id) {
        return getInstances().stream().filter(jda -> jda.getUserById(id) != null)
                .findAny().orElse(null);
    }

    public static User getGlobalUserById(String id) {
        JDA instance = getInstanceWithUser(id);
        if (instance == null) return null;
        return instance.getUserById(id);
    }
    */

    public static Guild getGlobalGuildById(String id) {
        JDA instance = getInstanceWithGuild(id);
        if (instance == null) return null;
        return instance.getGuildById(id);
    }

    public static TextChannel getGlobalTextChannelById(String id) {
        JDA instance = getInstanceWithChannel(id);
        if (instance == null) return null;
        return instance.getTextChannelById(id);
    }

    public static PrivateChannel getGlobalPrivateChannelById(String id) {
        JDA instance = getInstanceWithChannel(id);
        if (instance == null) return null;
        return instance.getPrivateChannelById(id);
    }

    @Override
    public void onReady(ReadyEvent e) {
        e.getJDA().addEventListener((Object[]) LISTENERS);
        if (e.getJDA().getShardInfo() != null) {
            int id = e.getJDA().getShardInfo().getShardId();
            instances.put(id, e.getJDA());
            e.getJDA().getPresence().setGame(Game.of("on Shard [" + id + "]"));
        }

        // Check if the current guild contains @deeBot Central
        Guild botGuild = e.getJDA().getGuildById("168154663932264448"); // @deeBot Central
        TextChannel welcomeChan = e.getJDA().getTextChannelById("168160579322642432"); // #welcome channel
        if (welcomeChan != null && botGuild.getSelfMember().hasPermission(welcomeChan, Permission.MANAGE_CHANNEL)) {
            welcomeChan.getManager().setTopic("**deeBot** | "
                    + "*Author:* <@" + Info.AUTHOR_ID + "> | "
                    + "*Latest Ver:* " + Info.VERSION).queue();
        }
    }
}
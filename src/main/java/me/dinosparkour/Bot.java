package me.dinosparkour;

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
import me.dinosparkour.managers.LogManager;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.managers.listeners.ActionManager;
import me.dinosparkour.managers.listeners.CustomCmdManager;
import me.dinosparkour.managers.listeners.InviteManager;
import me.dinosparkour.managers.listeners.StatsManager;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.hooks.InterfacedEventManager;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {

    public static void main(String[] args) throws LoginException, InterruptedException {
        ServerManager.init(); // Initialize the ServerManager and load the files
        CommandRegistry registry = new CommandRegistry(); // Create a new Command Registry

        JDA jda = new JDABuilder()
                // Options
                .setAudioEnabled(false) // We don't utilise JDA's audio subsystem
                .setBotToken(Info.TOKEN) // Set the Authentication Token
                .setBulkDeleteSplittingEnabled(false) // Performance reasons
                .setEventManager(new ThreadedEventManager()) // Allow for simultaneous command processing

                // Commands
                // ADMIN
                .addListener(registry.addCommand(new CleanupCommand()))
                .addListener(registry.addCommand(new EvalCommand()))
                .addListener(registry.addCommand(new GameCommand()))
                .addListener(registry.addCommand(new GetInviteCommand()))
                .addListener(registry.addCommand(new ShutdownCommand()))

                // GLOBAL
                .addListener(registry.addCommand(new CatCommand()))
                .addListener(registry.addCommand(new ChangelogCommand()))
                .addListener(registry.addCommand(new ChoiceCommand()))
                .addListener(registry.addCommand(new ChuckNorrisCommand()))
                .addListener(registry.addCommand(new CoinCommand()))
                .addListener(registry.addCommand(new DiscordStatusCommand()))
                .addListener(registry.addCommand(new EchoCommand()))
                .addListener(registry.addCommand(new EightBallCommand()))
                .addListener(registry.addCommand(new GoodShitCommand()))
                .addListener(registry.addCommand(new GoogleCommand()))
                .addListener(registry.addCommand(new HastebinCommand()))
                .addListener(registry.addCommand(new HelpCommand()))
                .addListener(registry.addCommand(new InfoCommand()))
                .addListener(registry.addCommand(new InviteCommand()))
                .addListener(registry.addCommand(new LennyCommand()))
                .addListener(registry.addCommand(new MemeCommand()))
                .addListener(registry.addCommand(new PatreonCommand()))
                .addListener(registry.addCommand(new PingCommand()))
                .addListener(registry.addCommand(new RektCommand()))
                .addListener(registry.addCommand(new ReminderCommand()))
                .addListener(registry.addCommand(new ReverseCommand()))
                .addListener(registry.addCommand(new SaltCommand()))
                .addListener(registry.addCommand(new ShortenCommand()))
                .addListener(registry.addCommand(new StatsCommand()))
                .addListener(registry.addCommand(new SteamStatusCommand()))
                .addListener(registry.addCommand(new SupportCommand()))
                .addListener(registry.addCommand(new UnshortenCommand()))
                .addListener(registry.addCommand(new UptimeCommand()))
                .addListener(registry.addCommand(new UrbanDictionaryCommand()))
                .addListener(registry.addCommand(new WeedCommand()))
                .addListener(registry.addCommand(new WhoisCommand()))
                .addListener(registry.addCommand(new YoMamaCommand()))
                .addListener(registry.addCommand(new YouTubeCommand()))

                // GUILD
                .addListener(registry.addCommand(new AddRoleCommand()))
                .addListener(registry.addCommand(new AnnouncementCommand()))
                .addListener(registry.addCommand(new AutoRoleCommand()))
                .addListener(registry.addCommand(new BanCommand()))
                .addListener(registry.addCommand(new BanListCommand()))
                .addListener(registry.addCommand(new BlacklistCommand()))
                .addListener(registry.addCommand(new CustomCmdCommand()))
                .addListener(registry.addCommand(new FarewellMessageCommand()))
                .addListener(registry.addCommand(new GuildInfoCommand()))
                .addListener(registry.addCommand(new KickCommand()))
                .addListener(registry.addCommand(new LeaveCommand()))
                .addListener(registry.addCommand(new PrefixCommand()))
                .addListener(registry.addCommand(new PurgeCommand()))
                .addListener(registry.addCommand(new RemoveRoleCommand()))
                .addListener(registry.addCommand(new RoleCommand()))
                .addListener(registry.addCommand(new UnbanCommand()))
                .addListener(registry.addCommand(new UserSearchCommand()))
                .addListener(registry.addCommand(new WelcomeMessageCommand()))

                // Managers
                .addListener(new ActionManager())
                .addListener(new CustomCmdManager())
                .addListener(new InviteManager())
                .addListener(new StatsManager())

                // Login
                .buildBlocking(); // Finally establish a connection to Discord's servers!

        jda.getTextChannelById("168160579322642432").getManager() // Hardcoded  #welcome channel in @deeBot Central
                .setTopic("**deeBot** | *Author:* <@" + Info.AUTHOR_ID + "> | *Latest Ver:* " + Info.VERSION).update();

        LogManager.init(); // Initialize the log manager after everything's been set up
    }

    private static class ThreadedEventManager extends InterfacedEventManager {
        private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        @Override
        public void handle(Event e) {
            threadPool.submit(() -> super.handle(e));
        }
    }
}
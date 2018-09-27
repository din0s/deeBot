package me.dinosparkour;

import me.dinosparkour.commands.global.HelpCommand;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.managers.listeners.ShardManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// import me.dinosparkour.managers.LogManager;

public class Bot {


    public static void main(String[] args) throws LoginException, InterruptedException {
        ServerManager.init(); // Initialize the ServerManager and load the files

        for (int shardNum = 0; shardNum < Info.SHARD_COUNT; shardNum++) {
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .addEventListener(new ShardManager()) // Handle the Ready Event separately
                    .setAudioEnabled(false) // We don't utilise JDA's audio subsystem
                    .setToken(Info.TOKEN) // Set the Authentication Token
                    .setBulkDeleteSplittingEnabled(false) // Performance reasons
                    .setEventManager(new ThreadedEventManager()); // Allow for simultaneous command processing

            if (Info.SHARD_COUNT > 1) {
                builder.useSharding(shardNum, Info.SHARD_COUNT); // Create a shard
            }

            builder.build(); // Finally establish a connection to Discord's servers!
            Thread.sleep(5000);
        }
//        LogManager.init(); // Initialize the log manager after everything's been set up
        HelpCommand.loadMessage(); // Build the help message once all commands have been registered
    }

    private static class ThreadedEventManager extends InterfacedEventManager {
        private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        @Override
        public void handle(Event e) {
            threadPool.submit(() -> super.handle(e));
        }
    }
}
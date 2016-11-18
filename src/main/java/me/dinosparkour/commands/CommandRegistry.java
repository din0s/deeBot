package me.dinosparkour.commands;

import me.dinosparkour.commands.impls.Command;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CommandRegistry {

    private static Map<String, Command> commands;

    public CommandRegistry() {
        commands = new TreeMap<>();
    }

    public static List<Command> getPublicCommands() {
        return commands.values().stream().filter(cmd -> !cmd.authorExclusive() && cmd.isPublic()).collect(Collectors.toList());
    }

    public static Command getCommand(String name) {
        return commands.values().stream().filter(cmd -> cmd.getAlias().contains(name)).findFirst().orElse(null);
    }

    public Command addCommand(Command cmd) {
        if (!commands.containsKey(cmd.getName())) {
            commands.put(cmd.getName(), cmd);
        }
        return cmd;
    }
}
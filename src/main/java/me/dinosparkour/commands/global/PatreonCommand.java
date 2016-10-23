package me.dinosparkour.commands.global;

import me.dinosparkour.Info;
import me.dinosparkour.commands.impls.GlobalCommand;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.IOUtil;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PatreonCommand extends GlobalCommand {

    private final static String SUCCESS_PLUS = "_Success!_ \uD83C\uDF89\t";
    private final static String SUCCESS_MINUS = "_Done!_ \uD83D\uDC4B\uD83C\uDFFD";
    private Set<String> patrons;
    private File patreonFile = new File(ServerManager.getDataDir() + "patreon.txt");

    public PatreonCommand() {
        load();
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        switch (args.length) {
            case 0:
                break;

            case 1:
                if (isOwner(e.getAuthor()) && args[0].equalsIgnoreCase("reload")) {
                    load();
                    chat.sendMessage(SUCCESS_PLUS);
                    return;
                }
                break;

            default:
                if (!isOwner(e.getAuthor())
                        || (!args[0].equalsIgnoreCase("add")
                        && !args[0].equalsIgnoreCase("remove")))
                    break;

                if (args.length < 2) {
                    chat.sendMessage("Please specify at least one entry!");
                    return;
                }

                int size = patrons.size();
                String input = String.join(" ", Arrays.asList(args)).substring(args[0].length()).trim();
                List<User> users = new UserUtil().getMentionedUsers(e.getMessage(), args);
                switch (args[0]) {
                    case "add":
                        if (users.isEmpty()) {
                            chat.sendMessage("Unknown user!");
                            return;
                        }

                        users.stream().map(MessageUtil::userDiscrimSet).forEach(patrons::add);
                        if (size < patrons.size()) {
                            saveToFile();
                            chat.sendMessage(SUCCESS_PLUS);
                        } else
                            chat.sendMessage("_" + (users.size() > 1 ? "These users are" : "That user is") + " is already in the list!_");
                        return;

                    case "remove":
                        if (users.isEmpty()) {
                            if (patrons.contains(input)) {
                                patrons.remove(input);
                                saveToFile();
                                chat.sendMessage(SUCCESS_MINUS);
                            } else
                                chat.sendMessage("_That user is not in the patron list!_");
                            return;
                        }

                        users.stream().map(MessageUtil::userDiscrimSet).forEach(patrons::remove);
                        if (size > patrons.size()) {
                            saveToFile();
                            chat.sendMessage(SUCCESS_MINUS);
                        } else
                            chat.sendMessage("_" + (users.size() > 1 ? "These users are" : "That user is") + " aren't in the list!_");
                        return;
                }
                break;
        }
        chat.sendMessage("**Patreon Link:** <https://www.patreon.com/dinos>"
                + "\n*Current Patrons:* "
                + (patrons.isEmpty() ? "None :(" : String.join(", ", patrons)));
    }

    @Override
    public String getName() {
        return "patreon";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "patrons");
    }

    @Override
    public String getDescription() {
        return "Returns information related to Patreon.";
    }

    private void load() {
        try {
            if (patreonFile.exists() || patreonFile.createNewFile()) {
                patrons = new LinkedHashSet<>(IOUtil.readLinesFromFile(patreonFile));
                patrons.stream().filter(String::isEmpty).forEach(patrons::remove); // Get rid of empty lines..
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isOwner(User u) {
        return u.getId().equals(Info.AUTHOR_ID);
    }

    private void saveToFile() {
        IOUtil.writeTextToFile(patreonFile, String.join("\n", patrons), false);
    }
}
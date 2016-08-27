package me.dinosparkour.commands.guild.roles;

import me.dinosparkour.utils.IOUtil;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.RoleUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.managers.RoleManager;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.*;

public class RoleCommand extends RoleCommandImpl {

    private final List<String> helpContents = IOUtil.readLinesFromResource("rolecommand.txt");
    private User author;
    private Guild guild;
    private User selfInfo;
    private Message message;
    private Role targetRole;
    private String inputArgs;
    private List<Role> mentionedRoles;
    private TextChannel fallbackChannel;

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        guild = e.getGuild();
        fallbackChannel = e.getTextChannel();

        switch (args.length) {
            case 0: // Command Help
                sendHelpMessage();
                break;

            case 1:
                switch (args[0].toLowerCase()) {
                    case "help": // Command help
                        sendHelpMessage();
                        break;

                    case "list": // Send role list
                        List<Role> roleList = guild.getRoles();
                        if (roleList.size() > 1) {
                            StringBuilder sb = new StringBuilder("```\n");
                            roleList.stream()
                                    .filter(r -> !r.equals(guild.getPublicRole()))
                                    .map(r -> "\u25ba " + r.getName())
                                    .forEach(r -> {
                                        if (sb.length() + r.length() > 2000 - "```".length()) {
                                            sendMessage(sb.append("```").toString());
                                            sb.setLength(0);
                                        } else
                                            sb.append(r).append("\n");
                                    });
                            if (sb.length() > 0)
                                sendMessage(sb.append("```").toString());
                        } else
                            sendMessage("*The rolelist is empty!*");
                        break;

                    case "perms":
                    case "permissions": // Send permissions list
                        StringBuilder permsList = new StringBuilder("__List of valid permissions__:```xl\n");
                        Arrays.stream(Permission.values())
                                .filter(p -> !p.equals(Permission.UNKNOWN))
                                .map(p -> p + "\n")
                                .forEach(permsList::append);
                        sendPrivateMessage(permsList.append("```").toString(), fallbackChannel);
                        break;

                    default:
                        sendUsageMessage();
                        break;
                }
                break;

            default:
                author = e.getAuthor();
                selfInfo = e.getJDA().getSelfInfo();
                message = e.getMessage();
                inputArgs = String.join(" ", Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                loadRoles(inputArgs);

                switch (args[0].toLowerCase()) {
                    case "create": // Create new role
                        RoleManager roleCreator = guild.createRole();
                        if (hasFullFlag() || hasNullFlag()) {
                            roleCreator.setName(inputArgs.substring(0, inputArgs.length() - 7));
                            if (hasFullFlag()) {
                                if (!PermissionUtil.checkPermission(guild, author, Permission.ADMINISTRATOR)) {
                                    sendMessage("You need the `[ADMINISTRATOR]` permission in order to apply the --full flag.");
                                    roleCreator.delete();
                                    return;
                                } else if (!PermissionUtil.checkPermission(guild, selfInfo, Permission.ADMINISTRATOR)) {
                                    sendMessage("The bot needs the `[ADMINISTRATOR]` permission in order to apply the --full flag.");
                                    roleCreator.delete();
                                    return;
                                } else {
                                    roleCreator.give(Arrays.stream(Permission.values())
                                            .filter(p -> !p.equals(Permission.UNKNOWN))
                                            .toArray(Permission[]::new));
                                }
                            } else
                                roleCreator.getRole().getPermissions().forEach(roleCreator::revoke);
                        } else roleCreator.setName(inputArgs);
                        roleCreator.update();
                        break;

                    case "delete": // Delete existing role
                        if (!canInteract(true)) return;
                        targetRole.getManager().delete();
                        break;

                    case "hoist":
                    case "separate": // Toggle userlist separation
                        if (!canInteract(true)) return;
                        targetRole.getManager().setGrouped(!targetRole.isGrouped()).update();
                        sendMessage("Now set to **" + targetRole.isGrouped() + "**.");
                        return;

                    case "getperms":
                    case "info": // Get role info
                        if (!canInteract(false)) return;
                        if (!targetRole.getPermissions().isEmpty()) {
                            StringBuilder rolePerms = new StringBuilder("```");
                            targetRole.getPermissions().stream().map(p -> p + "\n").forEach(rolePerms::append);
                            sendMessage(rolePerms.append("```").toString());
                        } else sendMessage("`This role has no permissions!`");
                        return;

                    case "userinfo": // User role info
                        List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args, e.getGuild().getUsers(), true);
                        User targetUser;
                        switch (userList.size()) {
                            case 0:
                                sendMessage("No users were found that meet the criteria.");
                                return;

                            case 1:
                                targetUser = userList.get(0);
                                break;

                            default:
                                sendMessage("Your query returned too many results. Please narrow down your search!");
                                return;
                        }
                        List<Role> roleList = guild.getRolesForUser(targetUser);
                        if (!roleList.isEmpty()) {
                            StringBuilder roleBuilder = new StringBuilder("```");
                            roleList.stream().map(r -> r.getName() + "\n").forEach(roleBuilder::append);
                            sendMessage(roleBuilder.append("```").toString());
                        } else sendMessage("`This user has no roles!`");
                        return;

                    case "addperm": // Add permission to role
                        if (cannotModifyPerm(args, true)) return;
                        break;

                    case "removeperm":
                    case "deleteperm": // Remove permission from role
                        if (cannotModifyPerm(args, false)) return;
                        break;

                    case "color": // (Re)Set role color
                        if (insufficientArgs(args)) return;
                        stripFirstArg();
                        if (!canInteract(true)) return;

                        int colorHex;
                        String hexCode = args[1].startsWith("#") ? args[1].substring(1) : args[1];
                        try {
                            colorHex = Integer.parseInt(hexCode, 16);
                            if (colorHex == 0) colorHex = 1;
                        } catch (NumberFormatException ex) {
                            if (!args[1].equalsIgnoreCase("reset")) {
                                sendMessage("__Invalid color!__\nPlease use this tool to get a valid HEX value: <http://color-hex.com>");
                                return;
                            } else colorHex = 0;
                        }
                        targetRole.getManager().setColor(colorHex).update();
                        break;

                    case "rename": // Rename role
                        if (!inputArgs.contains("|")) {
                            sendUsageMessage();
                            return;
                        }

                        String oldName = inputArgs.substring(0, inputArgs.indexOf("|")).trim();
                        String newName = inputArgs.substring(inputArgs.indexOf("|") + 1).trim();

                        loadRoles(oldName);
                        if (!canInteract(true)) return;

                        if (newName.equals("")) {
                            sendMessage("Please select a valid name.");
                            return;
                        } else targetRole.getManager().setName(newName).update();
                        break;

                    case "getpos":
                    case "getposition": // Return role position (int)
                        if (canInteract(false))
                            sendMessage("`" + targetRole.getPosition() + "`");
                        return;

                    default:
                        sendUsageMessage();
                        return;
                }
                sendMessage("\uD83D\uDC4D\uD83C\uDFFD"); // THUMBS UP!
                break;
        }
    }

    @Override
    public String getName() {
        return "role";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "roles");
    }

    @Override
    public String getDescription() {
        return "Role Management command.";
    }

    @Override
    public List<String> getOptionalParams() {
        return Collections.singletonList("help");
    }

    @Override
    public Map<String, String> getFlags() {
        Map<String, String> flags = new HashMap<>();
        flags.put("--null", "Create a role with no permissions");
        flags.put("--full", "Create a role with all permissions => Requires ADMINISTRATOR");
        return flags;
    }

    private void sendHelpMessage() {
        sendPrivateMessage("```diff\n"
                + MessageUtil.breakCodeBlocks(String.join("\n\n", helpContents))
                .replace("%P%", getPrefix())
                .replace("\n<br>\n", "\n")
                + "```", fallbackChannel);
    }

    private void loadRoles(String roleName) {
        mentionedRoles = new RoleUtil().getMentionedRoles(message, roleName);
        targetRole = mentionedRoles.isEmpty() ? null : mentionedRoles.get(0);
    }

    private boolean hasNullFlag() {
        return inputArgs.toLowerCase().endsWith(" --null");
    }

    private boolean hasFullFlag() {
        return inputArgs.toLowerCase().endsWith(" --full");
    }

    private boolean canInteract(boolean modify) {
        if (mentionedRoles.isEmpty()) {
            sendMessage("No roles were found that meet the criteria!");
            return false;
        } else if (modify) {
            if (!PermissionUtil.canInteract(author, targetRole)) {
                sendMessage("You cannot interract with a role higher in the hierarchy than your top role!");
                return false;
            }

            List<Role> botRoles = guild.getRolesForUser(selfInfo);
            if (!botRoles.isEmpty() && botRoles.get(0).equals(targetRole)) {
                sendMessage("The bot cannot interact with its highest role!");
                return false;
            } else if (!PermissionUtil.canInteract(selfInfo, targetRole)) {
                sendMessage("The bot's role is lower in hierarchy than the specified role!"
                        + "\nPlease move it to the top of the list to fix this issue.");
                return false;
            }
        }
        return true;
    }

    private void stripFirstArg() {
        inputArgs = inputArgs.substring(inputArgs.indexOf(" ") + 1);
        loadRoles(inputArgs);
    }

    private boolean insufficientArgs(String[] args) {
        if (args.length < 3) {
            sendUsageMessage();
            return true;
        } else return false;
    }

    private boolean cannotModifyPerm(String[] args, boolean give) {
        if (insufficientArgs(args)) return true;

        stripFirstArg();
        if (!canInteract(true)) return true;

        Permission perm;
        try {
            perm = Permission.valueOf(args[1]);
        } catch (IllegalArgumentException ex) {
            sendMessage("That's not a valid permission name!\n"
                    + "**Use " + MessageUtil.stripFormatting(getPrefix()) + "role permissions**");
            return true;
        }

        RoleManager rm = targetRole.getManager();
        if (give) rm.give(perm).update();
        else rm.revoke(perm).update();
        return false;
    }
}
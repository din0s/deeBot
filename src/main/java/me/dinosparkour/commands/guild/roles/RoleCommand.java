package me.dinosparkour.commands.guild.roles;

import me.dinosparkour.utils.IOUtil;
import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.RoleUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RoleCommand extends RoleCommandImpl {

    private static final long ALL_PERMS = Permission.getRaw(Arrays.stream(Permission.values()).filter(p -> p != Permission.UNKNOWN).toArray(Permission[]::new));
    private final List<String> helpContents = IOUtil.readLinesFromResource("rolecommand.txt");

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        switch (args.length) {
            case 0: // Command Help
                sendHelpMessage(chat, e.getTextChannel());
                break;

            case 1:
                switch (args[0].toLowerCase()) {
                    case "help": // Command help
                        sendHelpMessage(chat, e.getTextChannel());
                        break;

                    case "list": // Send role list
                        List<Role> roleList = e.getGuild().getRoles();
                        if (roleList.size() > 1) {
                            StringBuilder sb = new StringBuilder("```\n");
                            roleList.stream()
                                    .filter(r -> !r.equals(e.getGuild().getPublicRole()))
                                    .map(r -> "\u25ba " + r.getName() + " - " + r.getId())
                                    .forEach(r -> {
                                        if (sb.length() + r.length() > 2000 - "```".length()) {
                                            chat.sendMessage(sb.append("```").toString());
                                            sb.setLength(0);
                                        } else {
                                            sb.append(r).append("\n");
                                        }
                                    });
                            if (sb.length() > 0) {
                                chat.sendMessage(sb.append("```").toString());
                            }
                        } else
                            chat.sendMessage("*The rolelist is empty!*");
                        break;

                    case "perms":
                    case "permissions": // Send permissions list
                        StringBuilder permsList = new StringBuilder("__List of valid permissions__:```prolog\n");
                        Arrays.stream(Permission.values())
                                .filter(p -> !p.equals(Permission.UNKNOWN))
                                .map(p -> p + "\n")
                                .forEach(permsList::append);
                        chat.sendPrivateMessage(permsList.append("```").toString(), e.getTextChannel());
                        break;

                    default:
                        chat.sendUsageMessage();
                        break;
                }
                break;

            default:
                String inputArgs = String.join(" ", Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                List<Role> mentionedRoles = loadRoles(e.getMessage(), inputArgs);
                Role targetRole = getTargetRole(mentionedRoles);

                switch (args[0].toLowerCase()) {
                    case "create": // Create new role
                        e.getGuild().getController().createRole().queue(role -> {
                            RoleManagerUpdatable roleCreator = role.getManagerUpdatable();
                            if (hasFullFlag(inputArgs) || hasNullFlag(inputArgs)) {
                                roleCreator.getNameField().setValue(inputArgs.substring(0, inputArgs.length() - 7));
                                if (hasFullFlag(inputArgs)) {
                                    if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                        chat.sendMessage("You need the `[ADMINISTRATOR]` permission in order to apply the --full flag.");
                                        role.delete().queue();
                                        return;
                                    } else if (!e.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                                        chat.sendMessage("The bot needs the `[ADMINISTRATOR]` permission in order to apply the --full flag.");
                                        role.delete().queue();
                                        return;
                                    } else {
                                        roleCreator.getPermissionField().setValue(ALL_PERMS);
                                    }
                                } else {
                                    roleCreator.getPermissionField().setValue(0L);
                                }
                            } else {
                                roleCreator.getNameField().setValue(inputArgs);
                            }
                            roleCreator.update().queue();
                        });
                        break;

                    case "delete": // Delete existing role
                        if (cannotInteract(chat, e.getMessage(), mentionedRoles)) return;
                        assert targetRole != null;
                        targetRole.delete().queue();
                        break;

                    case "hoist":
                    case "separate": // Toggle userlist separation
                        if (cannotInteract(chat, e.getMessage(), mentionedRoles)) return;
                        assert targetRole != null;
                        targetRole.getManager().setHoisted(!targetRole.isHoisted()).queue();
                        chat.sendMessage("Now set to **" + !targetRole.isHoisted() + "**.");
                        return;

                    case "roleinfo":
                    case "info": // Get role info
                        if (!isRoleUnique(chat, mentionedRoles)) return;
                        mentionedRoles = loadRoles(e.getMessage(), inputArgs);
                        targetRole = getTargetRole(mentionedRoles);
                        assert targetRole != null;

                        String roleName = targetRole.getName();
                        String roleId = targetRole.getId();
                        String roleColor = getHex(targetRole.getColor());
                        int rolePosition = targetRole.getPosition();
                        boolean isHoisted = targetRole.isHoisted();
                        boolean isMentionable = targetRole.isMentionable();
                        boolean isManaged = targetRole.isManaged();
                        List<String> rolePermissions = targetRole.getPermissions().stream().map(Enum::name).collect(Collectors.toList());

                        chat.sendMessage("**Name:** " + MessageUtil.stripFormatting(roleName) + "\n"
                                + "**ID:** " + roleId + "\n"
                                + "**Color:** " + roleColor + "\n"
                                + "**Position:** " + rolePosition + "\n"
                                + "\n"
                                + "**Is Hoisted?:** " + (isHoisted ? "Yes" : "No") + "\n"
                                + "**Is Mentionable?:** " + (isMentionable ? "Yes" : "No") + "\n"
                                + "**Is Managed by an Integration?:** " + (isManaged ? "Yes" : "No") + "\n"
                                + "\n"
                                + "**Permissions:**```prolog\n" + (rolePermissions.isEmpty() ? "None" : String.join(" - ", rolePermissions)) + "```");
                        return;

                    case "userinfo": // User role info
                        List<Member> memberList = new UserUtil().getMentionedMembers(e.getMessage(), Arrays.copyOfRange(args, 1, args.length), e.getGuild().getMembers(), true);
                        Member targetMember;
                        switch (memberList.size()) {
                            case 0:
                                chat.sendMessage("No users were found that meet the criteria.");
                                return;

                            case 1:
                                targetMember = memberList.get(0);
                                break;

                            default:
                                chat.sendMessage("Your query returned too many results. Please narrow down your search!");
                                return;
                        }
                        List<String> roleList = targetMember.getRoles().stream().map(r -> MessageUtil.stripFormatting(r.getName())).collect(Collectors.toList());
                        chat.sendMessage(roleList.isEmpty() ? "`This user has no roles!`" : "```\n" + String.join(", ", roleList) + "```");
                        return;

                    case "addperm": // Add permission to role
                        if (cannotModifyPerm(chat, args, e.getMessage(), inputArgs, true)) return;
                        break;

                    case "remperm":
                    case "delperm":
                    case "removeperm":
                    case "deleteperm": // Remove permission from role
                        if (cannotModifyPerm(chat, args, e.getMessage(), inputArgs, false)) return;
                        break;

                    case "color": // (Re)Set role color
                        if (insufficientArgs(chat, args)) return;
                        mentionedRoles = loadRoles(e.getMessage(), stripFirstArg(inputArgs));
                        targetRole = getTargetRole(mentionedRoles);
                        if (cannotInteract(chat, e.getMessage(), mentionedRoles)) return;

                        int colorHex;
                        String hexCode = args[1].startsWith("#") ? args[1].substring(1) : args[1];
                        try {
                            colorHex = Integer.parseInt(hexCode, 16);
                            if (colorHex == 0) {
                                colorHex = 1;
                            }
                        } catch (NumberFormatException ex) {
                            if (!args[1].equalsIgnoreCase("reset")) {
                                chat.sendMessage("__Invalid color!__\nPlease use this tool to get a valid HEX value: <http://color-hex.com>");
                                return;
                            } else {
                                colorHex = 0;
                            }
                        }
                        assert targetRole != null;
                        targetRole.getManager().setColor(new Color(colorHex)).queue();
                        break;

                    case "getcolor": // Get role color
                        if (isRoleUnique(chat, mentionedRoles)) {
                            assert targetRole != null;
                            chat.sendMessage("**`" + getHex(targetRole.getColor()) + "`**");
                        }
                        return;

                    case "rename": // Rename role
                        if (!inputArgs.contains("|")) {
                            chat.sendUsageMessage();
                            return;
                        }

                        String oldName = inputArgs.substring(0, inputArgs.indexOf("|")).trim();
                        String newName = inputArgs.substring(inputArgs.indexOf("|") + 1).trim();

                        mentionedRoles = loadRoles(e.getMessage(), oldName);
                        targetRole = getTargetRole(mentionedRoles);
                        if (cannotInteract(chat, e.getMessage(), mentionedRoles)) return;

                        if (newName.isEmpty()) {
                            chat.sendMessage("Please select a valid name.");
                            return;
                        } else {
                            assert targetRole != null;
                            targetRole.getManager().setName(newName).queue();
                        }
                        break;

                    case "getpos":
                    case "getposition": // Return role position (int)
                        if (isRoleUnique(chat, mentionedRoles)) {
                            assert targetRole != null;
                            chat.sendMessage("`" + targetRole.getPosition() + "`");
                        }
                        return;

                    default:
                        chat.sendUsageMessage();
                        return;
                }
                chat.sendMessage("\uD83D\uDC4D\uD83C\uDFFD"); // THUMBS UP!
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

    private void sendHelpMessage(MessageSender chat, TextChannel fallbackChannel) {
        chat.sendPrivateMessage("```diff\n"
                + MessageUtil.breakCodeBlocks(String.join("\n\n", helpContents))
                .replace("%P%", getPrefix(fallbackChannel.getGuild()))
                .replace("\n<br>\n", "\n")
                + "```", fallbackChannel);
    }

    private List<Role> loadRoles(Message message, String roleName) {
        return new RoleUtil().getMentionedRoles(message, roleName);
    }

    private Role getTargetRole(List<Role> mentionedRoles) {
        return mentionedRoles.size() != 1 ? null : mentionedRoles.get(0);
    }

    private boolean hasNullFlag(String inputArgs) {
        return inputArgs.toLowerCase().endsWith(" --null");
    }

    private boolean hasFullFlag(String inputArgs) {
        return inputArgs.toLowerCase().endsWith(" --full");
    }

    private boolean isRoleUnique(MessageSender chat, List<Role> mentionedRoles) {
        switch (mentionedRoles.size()) {
            case 0:
                chat.sendMessage("No roles were found that meet the criteria!");
                return false;

            case 1:
                return true;

            default:
                chat.sendMessage("Your query returned too many results. Please narrow down your search!");
                return false;
        }
    }

    private boolean cannotInteract(MessageSender chat, Message msg, List<Role> mentionedRoles) {
        if (!isRoleUnique(chat, mentionedRoles)) return true;
        Role targetRole = getTargetRole(mentionedRoles);
        assert targetRole != null;
        if (!msg.getGuild().getMember(msg.getAuthor()).canInteract(targetRole)) {
            chat.sendMessage("You cannot interact with a role higher in the hierarchy than your top role!");
            return true;
        }

        List<Role> botRoles = msg.getGuild().getSelfMember().getRoles();
        if (!botRoles.isEmpty() && botRoles.get(0).equals(targetRole)) {
            chat.sendMessage("The bot cannot interact with its highest role!");
            return true;
        } else if (!msg.getGuild().getSelfMember().canInteract(targetRole)) {
            chat.sendMessage("The bot's role is lower in hierarchy than the specified role!"
                    + "\nPlease move it to the top of the list to fix this issue.");
            return true;
        } else return false;
    }

    private String stripFirstArg(String inputArgs) {
        return inputArgs.substring(inputArgs.indexOf(" ") + 1);
    }

    private boolean insufficientArgs(MessageSender chat, String[] args) {
        if (args.length < 3) {
            chat.sendUsageMessage();
            return true;
        } else return false;
    }

    private boolean cannotModifyPerm(MessageSender chat, String[] args, Message msg, String inputArgs, boolean give) {
        if (insufficientArgs(chat, args)) return true;

        List<Role> mentionedRoles = loadRoles(msg, stripFirstArg(inputArgs));
        if (!isRoleUnique(chat, mentionedRoles)) return true;
        if (cannotInteract(chat, msg, mentionedRoles)) return true;

        Permission perm;
        try {
            perm = Permission.valueOf(args[1]);
        } catch (IllegalArgumentException ex) {
            chat.sendMessage("That's not a valid permission name!\n"
                    + "**Use " + MessageUtil.stripFormatting(getPrefix(msg.getGuild())) + "role permissions**");
            return true;
        }

        if (!msg.getGuild().getMember(msg.getAuthor()).hasPermission(perm)) {
            chat.sendMessage("You do not have that permission! ❌");
            return true;
        }

        Role targetRole = getTargetRole(mentionedRoles);
        assert targetRole != null;
        RoleManager rm = targetRole.getManager();
        if (give) {
            rm.givePermissions(perm).queue();
        } else {
            rm.revokePermissions(perm).queue();
        }
        return false;
    }

    private String getHex(Color color) {
        return color == null ? "None." : "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
    }
}
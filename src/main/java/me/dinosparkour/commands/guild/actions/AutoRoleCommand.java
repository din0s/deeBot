package me.dinosparkour.commands.guild.actions;

import me.dinosparkour.commands.impls.GuildCommand;
import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AutoRoleCommand extends GuildCommand {

    private static final String SUCCESS = "__Set the role given upon joining to__: ";

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        ServerManager sm = new ServerManager(e.getGuild());
        switch (args.length) {
            case 0: // Get the current autorole
                Role joinRole = e.getGuild().getRoleById(sm.getAutoRoleId());
                chat.sendMessage("__Current role given upon joining__: " + (joinRole == null ? "None" : MessageUtil.stripFormatting(joinRole.getName())));
                break;

            case 1:
                // Check if args[0] is an ID
                Role tmpRole = e.getGuild().getRoleById(args[0]);
                if (tmpRole != null) {
                    sm.setAutoRole(tmpRole).update();
                    chat.sendMessage(SUCCESS + MessageUtil.stripFormatting(tmpRole.getName()));
                    break;
                }
                // It's not an ID, continue;

            default:
                if (allArgs.equalsIgnoreCase("reset")) { // Clear the autorole
                    sm.setAutoRole(null).update();
                    chat.sendMessage("__Reset the role given upon joining!__");
                    return;
                }

                List<Role> roles = e.getGuild().getRoles().stream()
                        .filter(role -> role.getName().equals(allArgs))
                        .collect(Collectors.toList());

                if (roles.size() > 1) // More than one roles share the given name
                    chat.sendMessage("There are more than 1 roles that meet the criteria! Please use IDs.");
                else if (roles.size() == 0) // No roles have the given name
                    chat.sendMessage("No roles were found that meet the criteria!");
                else { // A unique role has been found
                    Role newRole = roles.get(0);
                    User selfInfo = e.getJDA().getSelfInfo();

                    StringBuilder sb = new StringBuilder();
                    if (!PermissionUtil.canInteract(e.getAuthor(), newRole)) {
                        chat.sendMessage("**ERROR:** You cannot select a role that's higher in the hierarchy than your own top role!");
                        return;
                    } else if (!PermissionUtil.canInteract(selfInfo, newRole))
                        sb.append("**➤ WARNING \uD83C\uDD98**\n")
                                .append(selfInfo.getAsMention()).append("'s role is lower in the hierarchy than the given role.\n")
                                .append("This means that the bot will be unable to set the role upon joining!\n")
                                .append("To fix the issue, drag deeBot's role to the top of the role list.\n\n");

                    if (sm.getAutoRoleId() == null || !sm.getAutoRoleId().equals(newRole.getId()))
                        sm.setAutoRole(newRole).update();
                    chat.sendMessage(sb.append(SUCCESS).append(MessageUtil.stripFormatting(newRole.getName())).toString());
                }
                break;
        }
    }

    @Override
    public String getName() {
        return "autorole";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "joinrole");
    }

    @Override
    public String getDescription() {
        return "Manages the role given to new users upon joining.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("rolename / id / reset");
    }

    @Override
    public List<Permission> requiredPermissions() {
        return Collections.singletonList(Permission.MANAGE_ROLES);
    }

    @Override
    public String getExample() {
        return "Cool Person";
    }
}
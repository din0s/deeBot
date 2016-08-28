package me.dinosparkour.commands.guild.roles;

import me.dinosparkour.utils.MessageUtil;
import me.dinosparkour.utils.RoleUtil;
import me.dinosparkour.utils.UserUtil;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class ModifyRoleCommandImpl extends RoleCommandImpl {

    protected abstract Task getTask();

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        List<User> userList = new UserUtil().getMentionedUsers(e.getMessage(), args, e.getGuild().getUsers(), false);
        switch (userList.size()) {
            case 0:
                chat.sendMessage(getNotEnoughArguments("user"));
                break;

            case 1:
                User user = userList.get(0);
                String stripUser = String.join(" ", Arrays.asList(args))
                        .replace(user.getAsMention(), "")
                        .replace(MessageUtil.userDiscrimSet(user), "")
                        .replace(user.getUsername(), "").trim();

                List<Role> roleList = new RoleUtil().getMentionedRoles(e.getMessage(), stripUser);
                switch (roleList.size()) {
                    case 0:
                        chat.sendMessage(getNotEnoughArguments("role"));
                        break;

                    case 1:
                        Role role = roleList.get(0);
                        if (e.getGuild().getRolesForUser(e.getJDA().getSelfInfo()).get(0).equals(role)) {
                            chat.sendMessage("The bot cannot interact with its own highest role!");
                            return;
                        }
                        if (!PermissionUtil.canInteract(e.getJDA().getSelfInfo(), role)) {
                            chat.sendMessage("The bot doesn't have enough permissions to interact with that role!\n"
                                    + "To fix this issue, drag the bot's role to the top of the role list.");
                            return;
                        }
                        if (isPlus()) {
                            if (e.getGuild().getRolesForUser(user).contains(role)) {
                                chat.sendMessage("That user already has the role specified!");
                                return;
                            }
                            e.getGuild().getManager().addRoleToUser(user, role).update();
                        } else {
                            if (!e.getGuild().getRolesForUser(user).contains(role)) {
                                chat.sendMessage("That user doesn't have the role specified!");
                                return;
                            }
                            e.getGuild().getManager().removeRoleFromUser(user, role).update();
                        }
                        chat.sendMessage(MessageUtil.stripFormatting("Successfully " + getTask().perfect + " " + role.getName() + " to " + MessageUtil.userDiscrimSet(user) + "!"));
                        break;
                }
                break;

            default:
                chat.sendMessage(getTooManyArguments("user"));
                break;
        }
    }

    @Override
    public String getName() {
        return getTask().name().toLowerCase() + "role";
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        String firstCap = getTask().name().substring(0, 1) + getTask().name().toLowerCase().substring(1);
        return firstCap + "s a role " + getTask().preposition + " the specified user.";
    }

    @Override
    public List<String> getRequiredParams() {
        return Arrays.asList("user", "role");
    }

    @Override
    public int getArgMin() {
        return 2;
    }

    private String getNotEnoughArguments(String type) {
        return "No " + type + "s were found that meet the criteria!";
    }

    private String getTooManyArguments(String type) {
        return "Too many " + type + "s were found that meet the criteria!\nPlease narrow down your query.";
    }

    private boolean isPlus() {
        return getTask() == Task.ADD;
    }

    public enum Task {
        ADD("given", "to"),
        REMOVE("taken", "from");

        private final String perfect;
        private final String preposition;

        Task(String perfect, String preposition) {
            this.perfect = perfect;
            this.preposition = preposition;
        }
    }
}
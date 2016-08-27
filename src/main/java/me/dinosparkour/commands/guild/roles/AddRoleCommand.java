package me.dinosparkour.commands.guild.roles;

public class AddRoleCommand extends ModifyRoleCommandImpl {

    @Override
    protected Task getTask() {
        return Task.ADD;
    }
}
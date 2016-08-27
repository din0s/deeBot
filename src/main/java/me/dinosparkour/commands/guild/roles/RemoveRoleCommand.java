package me.dinosparkour.commands.guild.roles;

public class RemoveRoleCommand extends ModifyRoleCommandImpl {

    @Override
    protected Task getTask() {
        return Task.REMOVE;
    }
}
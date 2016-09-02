package me.dinosparkour.commands.guild.roles;

public class RemoveRoleCommand extends ModifyRoleCommandImpl {

    @Override
    protected Task getTask() {
        return Task.REMOVE;
    }

    @Override
    public String getExample() {
        return "dinos#0649 Admin";
    }
}
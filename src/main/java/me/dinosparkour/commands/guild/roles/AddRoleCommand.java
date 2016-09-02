package me.dinosparkour.commands.guild.roles;

public class AddRoleCommand extends ModifyRoleCommandImpl {

    @Override
    protected Task getTask() {
        return Task.ADD;
    }

    @Override
    public String getExample() {
        return "dinos#0649 Loser";
    }
}
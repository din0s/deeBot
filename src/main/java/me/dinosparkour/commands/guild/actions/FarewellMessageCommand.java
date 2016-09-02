package me.dinosparkour.commands.guild.actions;

public class FarewellMessageCommand extends ActionCommandImpl {

    @Override
    public Action getAction() {
        return Action.LEAVE;
    }

    @Override
    public String getExample() {
        return "Poof! Bye %user%..";
    }
}
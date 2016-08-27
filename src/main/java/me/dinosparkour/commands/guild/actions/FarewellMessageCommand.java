package me.dinosparkour.commands.guild.actions;

public class FarewellMessageCommand extends ActionCommandImpl {

    @Override
    public Action getAction() {
        return Action.LEAVE;
    }
}
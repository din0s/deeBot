package me.dinosparkour.commands.guild.actions;

public class WelcomeMessageCommand extends ActionCommandImpl {

    @Override
    public Action getAction() {
        return Action.JOIN;
    }

    @Override
    public String getExample() {
        return "Tada! Hello %user%..";
    }
}
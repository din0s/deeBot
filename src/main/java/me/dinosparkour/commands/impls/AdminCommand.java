package me.dinosparkour.commands.impls;

public abstract class AdminCommand extends Command {

    @Override
    public boolean authorExclusive() {
        return true;
    }
}
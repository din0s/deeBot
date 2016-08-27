package me.dinosparkour.commands.impls;

public abstract class GlobalCommand extends Command {

    @Override
    public boolean allowsPrivate() {
        return true;
    }
}
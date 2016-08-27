package me.dinosparkour.commands.impls;

public abstract class GuildCommand extends Command {

    @Override
    public boolean allowsPrivate() {
        return false;
    }

    @Override
    public boolean authorExclusive() {
        return false;
    }
}
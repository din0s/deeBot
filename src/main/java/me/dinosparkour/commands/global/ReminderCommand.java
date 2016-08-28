package me.dinosparkour.commands.global;

import me.dinosparkour.commands.impls.TimerCommandImpl;

import java.util.Collections;
import java.util.List;

public class ReminderCommand extends TimerCommandImpl {

    @Override
    protected Type getType() {
        return Type.REMINDER;
    }

    @Override
    public List<String> getAlias() {
        return Collections.singletonList(getName());
    }

    @Override
    public String getDescription() {
        return "Creates a countdown reminder that will notify you in private.";
    }

    @Override
    public boolean allowsPrivate() {
        return true;
    }
}
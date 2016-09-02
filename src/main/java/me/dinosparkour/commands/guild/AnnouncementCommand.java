package me.dinosparkour.commands.guild;

import me.dinosparkour.commands.impls.TimerCommandImpl;

import java.util.Arrays;
import java.util.List;

public class AnnouncementCommand extends TimerCommandImpl {

    @Override
    protected Type getType() {
        return Type.ANNOUNCEMENT;
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "announce");
    }

    @Override
    public String getDescription() {
        return "Creates a timer to announce a message in the current channel.";
    }

    @Override
    public boolean allowsPrivate() {
        return false;
    }

    @Override
    public String getExample() {
        return "10 days Rematch between Team Blue and Team Red.";
    }
}
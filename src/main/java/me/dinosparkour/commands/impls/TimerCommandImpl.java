package me.dinosparkour.commands.impls;

import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.IOUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class TimerCommandImpl extends Command {

    private final List<TimerImpl> announcements = new ArrayList<>();
    private final List<TimerImpl> reminders = new ArrayList<>();
    private final Map<String, Timer> announcementTimers = new HashMap<>();
    private final Map<String, Timer> reminderTimers = new HashMap<>();
    private final String noTimers = "You have no " + getType().name().toLowerCase() + "s set!";
    protected MessageReceivedEvent e;
    private JDA jda;

    public TimerCommandImpl() {
        try {
            if (!getFile().exists() && !getFile().createNewFile()) {
                System.err.println("Failed to create the " + getFile().getName().substring(0, ".txt".length()) + " file!");
                return;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        IOUtil.readDataFileBlocking(getFile().getName(), () ->
                IOUtil.readLinesFromFile(getFile()).forEach(s -> {
                    String[] constructors = s.split("\\|");
                    String time = constructors[0];
                    OffsetDateTime odt = OffsetDateTime.parse(constructors[0]);
                    String authorId = constructors[1];
                    String targetId = constructors[2];
                    String message = s.substring(time.length() + 1 + authorId.length() + 1 + targetId.length() + 1);
                    TimerImpl impl = new TimerImpl(odt, authorId, targetId, message);
                    if (impl.getTimeLeft() < 0)
                        IOUtil.removeTextFromFile(getFile(), s); // Delete reminder if it's been skipped
                    else addEntry(impl); // Add a new entry if the reminder is valid
                })
        );
    }

    protected abstract Type getType();

    protected abstract String getTargetId();

    private void init(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(ReadyEvent e) {
        init(e.getJDA());
    }

    @Override
    public void onReconnect(ReconnectedEvent e) {
        init(e.getJDA());
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e) {
        this.e = e;
        String allArgs = String.join(" ", Arrays.asList(args));
        String typeName = getType().name().toLowerCase();
        String typeSet = getType().pronoun + typeName;

        switch (args.length) {
            case 0:
                if (hasSetTimer()) { // The user has a timer set
                    TimerImpl timer = getSetTimer();
                    sendMessage("You have " + typeSet + " set for `" + timer.getTimeLeftFormatted() + "`"
                            + (timer.getMessage() == null ? "." : (" with the following message:\n" + timer.getMessage())));
                } else sendMessage(noTimers); // No timer has been set
                break;

            case 1:
                if (args[0].equalsIgnoreCase("reset")) {
                    if (hasSetTimer()) {
                        e.getChannel().sendTyping();
                        removeEntry(getSetTimer());
                        sendMessage("Successfully deleted your " + typeName + "!");
                    } else
                        sendMessage(noTimers);
                } else sendUsageMessage();
                break;

            default:
                if (!hasSetTimer()) {
                /*
                 * timer [duration] [time unit] (timer text)
                 *        args[0]    args[1]     args[...]
                 */
                    int duration;
                    try {
                        duration = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        sendMessage("That's not a valid duration amount!");
                        return;
                    }

                    Unit unit = Unit.get(args[1]);
                    ChronoUnit chronoUnit;
                    if (unit == null) {
                        sendMessage("**That's not a valid time unit!** (seconds, minutes, hours, days)");
                        return;
                    } else chronoUnit = unit.chronoUnit;

                    OffsetDateTime odt = OffsetDateTime.now().plus(duration, chronoUnit);
                    String authorId = e.getAuthor().getId();
                    String message = allArgs.substring(args[0].length() + 1 + args[1].length()).trim();
                    TimerImpl impl = new TimerImpl(odt, authorId, getTargetId(), message);

                    // Add the entry
                    e.getChannel().sendTyping();
                    addEntry(impl);
                    IOUtil.writeTextToFile(getFile(), getEntryLine(impl), true);
                    sendMessage("Your " + typeName + " has been set!");

                } else
                    sendMessage("You already have " + typeSet + " set! Use " + getPrefix() + getName() + " to review it.");
                break;
        }
    }

    @Override
    public String getName() {
        return getType().name().toLowerCase();
    }

    @Override
    public List<String> getOptionalParams() {
        return Arrays.asList("duration / reset", "time unit", "text");
    }

    private List<TimerImpl> getList() {
        return isReminder() ? reminders : announcements;
    }

    private Map<String, Timer> getMap() {
        return isReminder() ? reminderTimers : announcementTimers;
    }

    private String getEntryLine(TimerImpl impl) {
        return impl.getTimeStamp() + "|" + impl.getAuthorId() + "|" + impl.getTargetId() + "|" + impl.getMessage();
    }

    private void addEntry(TimerImpl impl) {
        Timer timer = new Timer();
        timer.schedule(timerTask(impl), impl.getTimeLeft());
        getList().add(impl);
        getMap().put(impl.getAuthorId(), timer);
    }

    private void removeEntry(TimerImpl impl) {
        getMap().get(impl.getAuthorId()).cancel(); // Cancel the timer thread
        getMap().remove(impl.getAuthorId());
        getList().remove(impl); // Remove the timer from the set
        IOUtil.removeTextFromFile(getFile(), getEntryLine(impl));
    }

    private boolean timerAuthorMatchesEvent(TimerImpl t) {
        return t.getAuthorId().equals(e.getAuthor().getId());
    }

    private boolean hasSetTimer() {
        return getList().stream().anyMatch(this::timerAuthorMatchesEvent);
    }

    private TimerImpl getSetTimer() {
        return getList().stream().filter(this::timerAuthorMatchesEvent).findFirst().orElse(null);
    }

    private File getFile() {
        return new File(ServerManager.getDataDir() + getType().name().toLowerCase() + "s.txt");
    }

    private TimerTask timerTask(TimerImpl impl) {
        return new TimerTask() {
            @Override
            public void run() {
                String msg = impl.getMessage().equals("") ? "⏰  Time's up!" : impl.getMessage();
                MessageChannel channel = isReminder()
                        ? jda.getPrivateChannelById(impl.getTargetId())
                        : jda.getTextChannelById(impl.getTargetId());
                if (channel != null) sendMessage(msg, channel);
                removeEntry(impl);
            }
        };
    }

    private boolean isReminder() {
        return getType() == Type.REMINDER;
    }

    protected enum Type {
        ANNOUNCEMENT("an "), REMINDER("a ");

        private final String pronoun;

        Type(String pronoun) {
            this.pronoun = pronoun;
        }
    }

    private enum Unit {
        SECONDS(ChronoUnit.SECONDS, 1000, "s", "second", "seconds"),
        MINUTES(ChronoUnit.MINUTES, Unit.SECONDS.multiplier * 60, "m", "min", "minute", "minutes"),
        HOURS(ChronoUnit.HOURS, Unit.MINUTES.multiplier * 60, "h", "hour", "hours"),
        DAYS(ChronoUnit.DAYS, Unit.HOURS.multiplier * 24, "d", "day", "days");

        private final ChronoUnit chronoUnit;
        private final int multiplier;
        private final String[] measurements;

        Unit(ChronoUnit chronoUnit, int multiplier, String... measurements) {
            this.chronoUnit = chronoUnit;
            this.multiplier = multiplier;
            this.measurements = measurements;
        }

        static Unit get(String measurement) {
            for (Unit u : Unit.values())
                if (Arrays.asList(u.measurements).contains(measurement))
                    return u;
            return null;
        }
    }

    public class TimerImpl {
        private final OffsetDateTime odt;
        private final String authorId;
        private final String targetId;
        private final String message;

        TimerImpl(OffsetDateTime odt, String authorId, String targetId, String message) {
            this.odt = odt;
            this.authorId = authorId;
            this.targetId = targetId;
            this.message = message;
        }

        OffsetDateTime getTimeStamp() {
            return odt;
        }

        long getTimeLeft() {
            return Duration.between(OffsetDateTime.now(), odt).getSeconds() * 1000;
        }

        String getTimeLeftFormatted() {
            return MessageUtil.formatTime(getTimeLeft());
        }

        public String getAuthorId() {
            return authorId;
        }

        public String getTargetId() {
            return targetId;
        }

        String getMessage() {
            return message;
        }
    }
}
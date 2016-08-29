package me.dinosparkour.commands.impls;

import me.dinosparkour.managers.ServerManager;
import me.dinosparkour.utils.IOUtil;
import me.dinosparkour.utils.MessageUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.PermissionUtil;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class TimerCommandImpl extends Command {

    private final ScheduledExecutorService timerScheduler = Executors.newScheduledThreadPool(1);
    private final List<TimerImpl> announcementList = new ArrayList<>();
    private final List<TimerImpl> reminderList = new ArrayList<>();
    private final Map<String, ScheduledFuture> scheduledAnnouncements = new HashMap<>(); // Map<AuthorId, Runnable>
    private final Map<String, ScheduledFuture> scheduledReminders = new HashMap<>();     // Map<AuthorId, Runnable>
    private final String noTimers = "You have no " + getType().name().toLowerCase() + "s set!";
    private JDA jda;

    public TimerCommandImpl() {
        if (IOUtil.createFile(getFile()))
            IOUtil.readDataFileBlocking(getFile().getName(), () ->
                    IOUtil.readLinesFromFile(getFile()).forEach(s -> {
                        String[] constructors = s.split("\\|");
                        String time = constructors[0];
                        OffsetDateTime odt = OffsetDateTime.parse(constructors[0]);
                        String authorId = constructors[1];
                        String targetId = constructors[2];
                        String message = s.substring(time.length() + 1 + authorId.length() + 1 + targetId.length() + 1);
                        TimerImpl impl = new TimerImpl(odt, authorId, targetId, message);
                        if (impl.getSecondsLeft() < 0)
                            IOUtil.removeTextFromFile(getFile(), s); // Delete reminder if it's been skipped
                        else addEntry(impl); // Add a new entry if the reminder is valid
                    })
            );
    }

    protected abstract Type getType();

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
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String targetId = isReminder() ? e.getAuthor().getPrivateChannel().getId() : e.getTextChannel().getId();
        String typeName = getType().name().toLowerCase();
        String typeSet = getType().pronoun + typeName;

        switch (args.length) {
            case 0:
                if (hasSetTimer(e.getAuthor())) { // The user has a timer set
                    TimerImpl timer = getSetTimer(e.getAuthor());
                    chat.sendMessage("You have " + typeSet + " set for `" + timer.getTimeLeftFormatted() + "`"
                            + (timer.getMessage() == null ? "." : (" with the following message:\n" + timer.getMessage())));
                } else chat.sendMessage(noTimers); // No timer has been set
                break;

            case 1:
                if (args[0].equalsIgnoreCase("reset")) {
                    if (hasSetTimer(e.getAuthor())) {
                        chat.sendMessage("Successfully deleted your " + typeName + "!");
                        removeEntry(getSetTimer(e.getAuthor()));
                    } else
                        chat.sendMessage(noTimers);
                } else chat.sendUsageMessage();
                break;

            default:
                if (!isReminder() && !PermissionUtil.checkPermission(e.getTextChannel(), e.getAuthor(), Permission.MESSAGE_MANAGE))
                    chat.sendMessage("You need `[MESSAGE_MANAGE]` in order to create announcements for this channel!");
                else if (!hasSetTimer(e.getAuthor())) {
                /*
                 * timer [duration] [time unit] (timer text)
                 *        args[0]    args[1]     args[...]
                 */
                    int duration;
                    try {
                        duration = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        chat.sendMessage("That's not a valid duration amount!");
                        return;
                    }

                    Unit unit = Unit.get(args[1]);
                    ChronoUnit chronoUnit;
                    if (unit == null) {
                        chat.sendMessage("**That's not a valid time unit!** (seconds, minutes, hours, days)");
                        return;
                    } else chronoUnit = unit.chronoUnit;

                    OffsetDateTime odt = OffsetDateTime.now().plus(duration, chronoUnit);
                    String authorId = e.getAuthor().getId();
                    String message = e.getMessage().getRawContent()
                            .substring(e.getMessage().getRawContent().indexOf(" ") + 1 + args[0].length() + 1 + args[1].length())
                            .trim().replace("\n", "\\n");
                    TimerImpl impl = new TimerImpl(odt, authorId, targetId, message);

                    // Add the entry
                    chat.sendMessage("Your " + typeName + " has been set!", e.getChannel());
                    addEntry(impl);
                    IOUtil.writeTextToFile(getFile(), getEntryLine(impl), true);
                } else
                    chat.sendMessage("You already have " + typeSet + " set! Use " + getPrefix(e.getGuild()) + getName() + " to review it.");
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
        return isReminder() ? reminderList : announcementList;
    }

    private Map<String, ScheduledFuture> getMap() {
        return isReminder() ? scheduledReminders : scheduledAnnouncements;
    }

    private String getEntryLine(TimerImpl impl) {
        return impl.getTimeStamp() + "|" + impl.getAuthorId() + "|" + impl.getTargetId() + "|" + impl.getMessage();
    }

    private void addEntry(TimerImpl impl) {
        ScheduledFuture future = timerScheduler.schedule(timerRunnable(impl), impl.getSecondsLeft(), TimeUnit.SECONDS);
        getList().add(impl);
        getMap().put(impl.getAuthorId(), future);
    }

    private void removeEntry(TimerImpl impl) {
        getList().remove(impl); // Remove the timer from the set
        IOUtil.removeTextFromFile(getFile(), getEntryLine(impl));
        getMap().get(impl.getAuthorId()).cancel(true); // Cancel the scheduled future
        getMap().remove(impl.getAuthorId());
    }

    private boolean userMatch(TimerImpl impl, User u) {
        return impl.getAuthorId().equals(u.getId());
    }

    private boolean hasSetTimer(User u) {
        return getList().stream().anyMatch(impl -> userMatch(impl, u));
    }

    private TimerImpl getSetTimer(User u) {
        return getList().stream().filter(impl -> userMatch(impl, u)).findFirst().orElse(null);
    }

    private File getFile() {
        return new File(ServerManager.getDataDir() + getType().name().toLowerCase() + "s.txt");
    }

    private Runnable timerRunnable(TimerImpl impl) {
        return () -> {
            String msg = impl.getMessage().isEmpty() ? "⏰  Time's up!" : impl.getMessage().replace("\\n", "\n");
            MessageChannel channel = isReminder()
                    ? jda.getPrivateChannelById(impl.getTargetId())
                    : jda.getTextChannelById(impl.getTargetId());
            if (channel != null) MessageUtil.sendMessage(msg, channel);
            removeEntry(impl);
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
        SECONDS(ChronoUnit.SECONDS, 1000, "s", "sec", "second", "seconds"),
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

        long getSecondsLeft() {
            return Duration.between(OffsetDateTime.now(), odt).getSeconds();
        }

        String getTimeLeftFormatted() {
            return MessageUtil.formatTime(getSecondsLeft() * 1000);
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
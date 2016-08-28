package me.dinosparkour.commands.admin;

import me.dinosparkour.commands.impls.AdminCommand;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class EvalCommand extends AdminCommand {

    private final ScheduledExecutorService eval = Executors.newScheduledThreadPool(1);
    private final ScriptEngine engine;

    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util);");
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
        String allArgs = String.join(" ", Arrays.asList(args));
        engine.put("e", e);
        engine.put("e", e);
        engine.put("API", e.getJDA()); // .. because my phone autocorrects to all caps, :P
        engine.put("api", e.getJDA());
        engine.put("jda", e.getJDA());
        engine.put("channel", e.isPrivate() ? e.getPrivateChannel() : e.getTextChannel());
        engine.put("author", e.getAuthor());
        engine.put("message", e.getMessage());
        engine.put("guild", e.getGuild());
        engine.put("input", allArgs);
        engine.put("mentionedUsers", e.getMessage().getMentionedUsers());
        engine.put("mentionedRoles", e.getMessage().getMentionedRoles());
        engine.put("mentionedChannels", e.getMessage().getMentionedChannels());

        ScheduledFuture<?> future = eval.schedule(() -> {
            Object out = null;
            try {
                out = engine.eval("(function() { with (imports) {\n" + allArgs + "\n} })();");
            } catch (Exception ex) {
                chat.sendMessage("**Exception**: ```\n" + ex.getLocalizedMessage() + "```");
                return;
            }

            String outputS;
            if (out == null)
                outputS = "`Task executed without errors.`";
            else
                outputS = "Output: ```\n" + out.toString().replace("`", "\\`") + "\n```";

            if (outputS.length() > 2000)
                outputS = "The output is longer than 2000 chars!";

            chat.sendMessage(outputS);
        }, 0, TimeUnit.MILLISECONDS);

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            chat.sendMessage("Your task exceeds the time limit!");
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList(getName(), "evaluate", "exec", "execute");
    }

    @Override
    public String getDescription() {
        return "Evaluates a given snippet of code.";
    }

    @Override
    public boolean allowsPrivate() {
        return true;
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList("code");
    }

    @Override
    public int getArgMin() {
        return 1;
    }

}
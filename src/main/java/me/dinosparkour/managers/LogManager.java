package me.dinosparkour.managers;

import me.dinosparkour.utils.IOUtil;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private static final File LOG_FOLDER = new File("logs");

    public static void init() {
        String logFileName = new SimpleDateFormat("dd.MM.yyyy").format(new Date()) + ".err";
        File logFile = new File(LOG_FOLDER + File.separator + logFileName);
        if (IOUtil.createFolder(LOG_FOLDER) && IOUtil.createFile(logFile)) {
            try {
                SimpleLog.addFileLogs(null, logFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
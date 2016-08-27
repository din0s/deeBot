package me.dinosparkour.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOUtil {

    private static final ExecutorService THREADPOOL = Executors.newFixedThreadPool(1);

    /*
     * INPUT READERS
     */

    // Resource Reader
    public static List<String> readLinesFromResource(String fileName) {
        List<String> result = null;
        InputStream resource = IOUtil.class.getClassLoader().getResourceAsStream(fileName);
        try {
            System.out.print("Reading " + fileName + " ... ");
            result = IOUtils.readLines(resource, "UTF-8");
            System.out.print("Done!\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // File Reader
    public static List<String> readLinesFromFile(File file) {
        List<String> result = null;
        try {
            result = Files.readAllLines(Paths.get(file.getPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // JSON Reader
    public static JSONObject readJsonFromFile(File file) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(new String(Files.readAllBytes(Paths.get(file.getPath())), "UTF-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    // Data Reader
    public static void readDataFileBlocking(String dataName, Runnable r) {
        System.out.print("Reading " + dataName + " ... ");
        Thread t = new Thread(r, dataName.toUpperCase() + " LOADER");
        try {
            t.start(); // Run the thread
            t.join();  // Wait for the task to complete
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.print("Done!\n");
    }


    /*
     * OUTPUT WRITERS
     */

    // File Writer
    public static void writeLinesToFile(File file, List<String> lines, boolean append) {
        try {
            FileUtils.writeLines(file, "UTF-8", lines, append);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeTextToFile(File file, String addedLine, boolean append) {
        writeLinesToFile(file, Collections.singletonList(addedLine), append);
    }

    public static void removeLinesFromFile(File file, List<String> deletedLines) {
        List<String> lines = readLinesFromFile(file);
        if (lines.removeAll(deletedLines)) writeLinesToFile(file, lines, false);
    }

    public static void removeTextFromFile(File file, String removedText) {
        removeLinesFromFile(file, Collections.singletonList(removedText));
    }

    // JSON Writer
    public static void writeJsonToFile(File file, JSONObject jsonObject, boolean lock) {
        Runnable task = () -> {
            try {
                Files.write(Paths.get(file.getPath()), jsonObject.toString(4).getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };

        if (lock)
            THREADPOOL.submit(task);
        else
            task.run();
    }

    // File Deleter
    public static void deleteFile(File file) {
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeJsonToFile(File file, JSONObject jsonObject) {
        writeJsonToFile(file, jsonObject, false);
    }
}
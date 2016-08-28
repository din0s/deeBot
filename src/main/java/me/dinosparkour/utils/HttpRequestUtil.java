package me.dinosparkour.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class HttpRequestUtil {

    public static InputStream getInputStream(String url) {
        InputStream is = null;
        try {
            is = Unirest.get(url).asBinary().getBody();
        } catch (UnirestException ignored) {
        }
        return is;
    }

    public static JSONObject getData(String url) {
        return getData(url, Collections.emptyMap());
    }

    public static JSONObject getData(String url, Map<String, String> params) {
        JSONObject obj = null;
        GetRequest req = Unirest.get(url); // Create a GET request
        params.entrySet().forEach(set -> req.routeParam(set.getKey(), set.getValue())); // Add params for each set
        try {
            obj = req.asJson().getBody().getObject(); // Send the request
        } catch (UnirestException ignored) {
        }
        return obj;
    }

    public static void postData(String url, JSONObject data) {
        postData(url, Collections.emptyMap(), data);
    }

    public static void postData(String url, Map<String, String> headers, JSONObject data) {
        HttpRequestWithBody request = Unirest.post(url); // Create a POST request

        if (!headers.isEmpty()) // If we have a map of headers, iterate and include in the request
            headers.entrySet().forEach(set -> request.header(set.getKey(), set.getValue()));

        request.header("Content-Type", "application/json"); // Body is encoded in JSON
        request.body(data.toString());

        try {
            HttpResponse response = request.asString(); // Send the request
            if (!success(response)) // If we do not receive 2XX, print the response
                System.out.printf("Request to %s returned %s %s:\n%s\n", url, response.getStatus(), response.getStatusText(), response.getBody());
            else if (SimpleLog.LEVEL.getPriority() <= SimpleLog.Level.DEBUG.getPriority()) // If we are in debug mode, print the status
                System.out.printf("Request to %s returned %s %s\n", url, response.getStatus(), response.getStatusText());
        } catch (UnirestException ignored) {
        }
    }

    private static boolean success(HttpResponse response) {
        return String.valueOf(response.getStatus()).charAt(0) == '2';
    }
}
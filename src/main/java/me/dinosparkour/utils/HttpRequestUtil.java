package me.dinosparkour.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtil {

    private static final Map<String, String> JSON_MAP = new HashMap<>(1);

    static {
        JSON_MAP.put("Content-Type", "application/json");
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        HttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
        Unirest.setHttpClient(httpclient);
    }

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
        return getData(url, params, "");
    }

//    public static JSONObject getData(String url, String auth) {
//        return getData(url, Collections.emptyMap(), auth);
//    }

    public static JSONObject getData(String url, Map<String, String> params, String auth) {
        return getData(url, params, "", auth);
    }

//    public static JSONObject getData(String url, String username, String password) {
//        return getData(url, Collections.emptyMap(), username, password);
//    }

    public static JSONObject getData(String url, Map<String, String> params, String username, String password) {
        try {
            for (Map.Entry<String, String> set : params.entrySet()) { // Add params for each set
                url = url.replace("{" + set.getKey() + "}", URLEncoder.encode(set.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException ignored) {
        }

        GetRequest req = Unirest.get(url); // Create a GET request
        if (!password.isEmpty()) {
            if (username.isEmpty()) { // No username - Set authorization headers
                req.header("Authorization", password);
            } else { // Use Basic HTTP Authentication instead
                req.basicAuth(username, password);
            }
        }

        JSONObject obj = null;
        try {
            obj = req.asJson().getBody().getObject(); // Send the request
        } catch (UnirestException ignored) {
        }
        return obj;
    }

    public static HttpResponse postData(String url, Map<String, String> headers, String body) {
        HttpRequestWithBody request = Unirest.post(url); // Create a POST request

        if (!headers.isEmpty()) { // If we have a map of headers, iterate and include in the request
            headers.forEach(request::header);
        }

        request.body(body);
        HttpResponse response = null;
        try {
            response = request.asString(); // Send the request
            if (!success(response)) {// If we do not receive 2XX, print the response
                System.out.printf("Request to %s returned %s %s:\n%s\n", url, response.getStatus(), response.getStatusText(), response.getBody());
            } else if (SimpleLog.LEVEL.getPriority() <= SimpleLog.Level.DEBUG.getPriority()) { // If we are in debug mode, print the status
                System.out.printf("Request to %s returned %s %s\n", url, response.getStatus(), response.getStatusText());
            }
        } catch (UnirestException ignored) {
        }
        return response;
    }

    public static HttpResponse postData(String url, Map<String, String> headers, JSONObject data) {
        headers.putAll(JSON_MAP);
        return postData(url, headers, data.toString());
    }

    public static HttpResponse postData(String url, JSONObject data) {
        return postData(url, JSON_MAP, data.toString());
    }

    public static HttpResponse postData(String url, String body) {
        return postData(url, Collections.emptyMap(), body);
    }

    private static boolean success(HttpResponse response) {
        return String.valueOf(response.getStatus()).charAt(0) == '2';
    }
}
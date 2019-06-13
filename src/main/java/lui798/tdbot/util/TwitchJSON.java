package lui798.tdbot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwitchJSON {

    private JsonElement json;
    private String urlString;

    public TwitchJSON(String url) {
        this.urlString = url;
        updateJson();
    }

    public void rootFromUrl() throws Exception {

        URL url = new URL(urlString);
        HttpURLConnection request = (HttpURLConnection)url.openConnection();
        request.connect();

        json = new JsonParser().parse(new InputStreamReader((InputStream)request.getContent()));
    }

    public void updateJson() {
        try {
            rootFromUrl();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonElement getElement(JsonElement parent, String name) {
        return parent.getAsJsonObject().get(name);
    }

    public static String getString(JsonElement parent, String name) {
        return getElement(parent, name).getAsString();
    }

    public JsonElement getStream() {
        return getElement(getRoot(), "stream");
    }

    public JsonElement getRoot() {
        return json;
    }
}

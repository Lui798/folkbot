package lui798.folkbot.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class JsonThing {
    private JsonElement json;
    private URL jsonUrl;
    private Logger log = LoggerFactory.getLogger(JsonThing.class);

    public JsonThing(String url) throws MalformedURLException {
        this.jsonUrl = new URL(url);
        //getJsonFromUrl();
    }

    public void getJsonFromUrl() {
        try {
            URLConnection request = jsonUrl.openConnection();
            request.connect();

            json = new JsonParser().parse(new InputStreamReader(request.getInputStream()));
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public JsonElement element() {
        return json;
    }
}

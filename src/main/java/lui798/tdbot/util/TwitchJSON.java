package lui798.tdbot.util;

import com.google.gson.JsonElement;

public class TwitchJSON extends CustomJSON {

    public TwitchJSON(String url) {
        super(url);
    }

    public JsonElement getStream() {
        return getElement(getRoot(), "stream");
    }

}

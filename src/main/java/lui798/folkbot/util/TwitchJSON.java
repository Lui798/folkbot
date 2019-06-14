package lui798.folkbot.util;

import com.google.gson.JsonElement;

public class TwitchJSON extends CustomJSON {

    public TwitchJSON(String url) {
        super(url);
    }

    public JsonElement getStream() {
        return getElement(getRoot(), "stream");
    }

}

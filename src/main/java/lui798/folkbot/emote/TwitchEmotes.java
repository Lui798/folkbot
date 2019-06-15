package lui798.folkbot.emote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lui798.folkbot.util.CustomJSON;
import lui798.folkbot.util.TwitchJSON;

import java.util.ArrayList;
import java.util.List;

public class TwitchEmotes {
    private String user;
    private String clientID;
    private final String SIZE = "2";

    private CustomJSON frankerFaceZ;
    private CustomJSON betterTTV;
    private CustomJSON twitchGlobal;

    private List<CustomEmote> ffzList = new ArrayList<>();
    private List<CustomEmote> bttvList = new ArrayList<>();
    private List<CustomEmote> twitchList = new ArrayList<>();

    public TwitchEmotes(String user) {
        this.user = user;
        this.clientID = clientID;
        this.frankerFaceZ = new CustomJSON("https://api.frankerfacez.com/v1/room/" + user);
        this.betterTTV = new CustomJSON("https://api.betterttv.net/2/channels/" + user);
        this.twitchGlobal = new CustomJSON("https://api.twitch.tv/kraken/chat/emoticon_images?emotesets=0");

        makeEmoteLists();
    }

    public List<CustomEmote> getList() {
        List<CustomEmote> temp = new ArrayList<>();
        temp.addAll(ffzList);
        temp.addAll(bttvList);
        temp.addAll(twitchList);

        return temp;
    }

    private void makeEmoteLists() {
        String ffzSet = CustomJSON.getString(CustomJSON.getElement(frankerFaceZ.getRoot(), "room"), "set");
        JsonArray ffzArray = CustomJSON.getElement(CustomJSON.getElement(CustomJSON.getElement(frankerFaceZ.getRoot(), "sets"), ffzSet), "emoticons").getAsJsonArray();

        for (JsonElement ffzEmote : ffzArray) {
            String url = "https:" + TwitchJSON.getElement(TwitchJSON.getElement(ffzEmote, "urls"), SIZE);
            String name = TwitchJSON.getString(ffzEmote, "name");

            ffzList.add(new CustomEmote(name, url));
        }

        String bttvUrl = "https:" + TwitchJSON.getString(betterTTV.getRoot(), "urlTemplate");
        JsonArray bttvArray = CustomJSON.getElement(betterTTV.getRoot(), "emotes").getAsJsonArray();

        for (JsonElement bttvEmote : bttvArray) {
            String url = bttvUrl.replace("{{id}}", TwitchJSON.getString(bttvEmote, "id")).replace("{{image}}", SIZE + "x");
            String name = TwitchJSON.getString(bttvEmote, "code");

            bttvList.add(new CustomEmote(name, url));
        }

        JsonArray twitchArray = CustomJSON.getElement(CustomJSON.getElement(twitchGlobal.getRoot(), "emoticon_sets"), "0").getAsJsonArray();

        for (JsonElement twitchEmote : twitchArray) {
            String name = CustomJSON.getString(twitchEmote, "code");

            twitchList.add(new CustomEmote(name, null));
        }
    }
}

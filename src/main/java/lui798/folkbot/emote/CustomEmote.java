package lui798.folkbot.emote;

public class CustomEmote {
    private final String url;
    private final String name;

    public CustomEmote(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}

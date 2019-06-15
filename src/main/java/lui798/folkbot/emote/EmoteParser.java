package lui798.folkbot.emote;

public class EmoteParser {

    TwitchEmotes emotes;

    public EmoteParser(String user) {
        emotes = new TwitchEmotes(user);
    }

    public String twitchToDiscord(String message) {
        for (CustomEmote emote : emotes.getList()) {
            message = message.replaceAll("\\b" + emote.getName() + "\\b", ":" + emote.getName() + ":");
        }

        return message;
    }

    public String discordToTwitch(String message) {
        for (CustomEmote emote : emotes.getList()) {
            message = message.replace(":" + emote.getName() + ":", emote.getName());
        }

        return message;
    }
}

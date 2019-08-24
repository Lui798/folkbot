package lui798.folkbot.twitch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lui798.folkbot.util.Config;
import lui798.folkbot.util.EncodingUtil;
import lui798.folkbot.util.JsonThing;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;

public class TwitchController {
    private final String API_URL = "https://api.twitch.tv/kraken/";
    private final Logger LOG = LoggerFactory.getLogger(TwitchController.class);
    private final Config config;
    private final TextChannel liveChannel;

    private Timer liveTimer = new Timer();
    private boolean timerStarted = false;
    private Message currentMessage = null;
    private JsonThing stream;
    private JsonThing channel;

    private String USER_ID;
    private String CLIENT_ID;

    public TwitchController(Config config, Guild guild) {
        this.config = config;
        this.liveChannel = guild.getTextChannelById(config.getProp("liveChannel"));
        setJson();
    }

    private JsonObject getStream() {
        return stream.element().getAsJsonObject().get("stream").getAsJsonObject();
    }

    public Timer getLiveTimer() {
        return liveTimer;
    }

    public void setTimerStarted(boolean timerStarted) {
        this.timerStarted = timerStarted;
    }

    public boolean isTimerStarted() {
        return this.timerStarted;
    }

    public Config getConfig() {
        return config;
    }

    private boolean isLive() {
        stream.getJsonFromUrl();
        return !getStream().isJsonNull();
    }

    public void updateJson() {
        channel.getJsonFromUrl();
        stream.getJsonFromUrl();
    }

    public void setJson() {
        USER_ID = config.getProp("twitchUser");
        CLIENT_ID = config.getProp("twitchClientID");
        try {
            this.stream = new JsonThing(API_URL + "streams/" + USER_ID + "?client_id=" + CLIENT_ID);
            this.channel = new JsonThing(API_URL + "channels/" + USER_ID + "?client_id=" + CLIENT_ID);

        }
        catch (MalformedURLException e) {
            LOG.error(e.toString());
        }
        updateJson();
    }

    public void liveMain() {
        updateJson();
        if (isLive() && currentMessage == null) {
            currentMessage = liveChannel.sendMessage(liveEmbed()).complete();
            LOG.info("Sent live message");
        }
        else if (isLive() && currentMessage != null) {
            currentMessage = currentMessage.editMessage(liveEmbed()).complete();
            LOG.info("Updated live message");
        }
        else if (!isLive()) {
            if (currentMessage != null) {
                currentMessage = currentMessage.editMessage(vodEmbed()).complete();
                LOG.info("Updated live message to vod");
            }
            currentMessage = null;
        }
    }

    private MessageEmbed liveEmbed() {
        JsonObject channel = getStream().get("channel").getAsJsonObject();
        JsonElement preview = getStream().get("preview").getAsJsonObject().get("template");

        NumberFormat numberFormat = NumberFormat.getInstance();
        Duration date = Duration.between(Instant.from(OffsetDateTime.parse(getStream().get("created_at").getAsString(), DateTimeFormatter.ISO_DATE_TIME)), Instant.now());

        LOG.info(getStream().get("created_at").getAsString());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(6570404);
        embed.setAuthor(channel.get("display_name").getAsString(), null, channel.get("logo").getAsString());
        embed.setImage(preview.getAsString().replace("{width}", "1152")
                .replace("{height}", "648"));
        embed.setThumbnail(channel.get("logo").getAsString());
        embed.addField("Stream", "[" + channel.get("status").getAsString() + "]("
                + channel.get("url").getAsString() + ")", false);
        if (!channel.get("game").getAsString().equals("")) {
            String gameUrl = "https://www.twitch.tv/directory/game/"
                    + EncodingUtil.encodeURIComponent(channel.get("game").getAsString());

            embed.addField("Game", "[" + channel.get("game").getAsString() + "]("
                    + gameUrl + ")", false);
        }
        embed.setTimestamp(Instant.now());
        Long l = date.toMillis()/1000;
        embed.setFooter(numberFormat.format(Integer.parseInt(getStream().get("viewers").getAsString()))
                + " Viewers | " + EncodingUtil.decodeTimeInSeconds(l.intValue()) + " Uptime", null);


        return embed.build();
    }

    private MessageEmbed vodEmbed() {
        JsonObject channelObject = channel.element().getAsJsonObject();
        JsonThing vodJSON = null;
        try {
            vodJSON = new JsonThing(API_URL + "channels/" + USER_ID
                    + "/videos?client_id=" + CLIENT_ID + "&broadcast_type=archive");
            vodJSON.getJsonFromUrl();
        }
        catch (MalformedURLException e) {
            LOG.error(e.toString());
        }

        JsonObject video = vodJSON.element().getAsJsonObject().get("videos").getAsJsonArray().get(0).getAsJsonObject();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(16070455);
        embed.setAuthor(channelObject.get("display_name").getAsString(), null, channelObject.get("logo").getAsString());
        if (video.get("preview").getAsString().contains("_404")) {
            embed.setImage(video.get("preview").getAsString());
        } else {
            embed.setImage(video.get("preview").getAsString()
                    .substring(0, video.get("preview").getAsString().lastIndexOf("-")+1) + "1152x648.jpg");
        }
        embed.setThumbnail(channelObject.get("logo").getAsString());
        embed.addField("Stream VOD", "[" + video.get("title").getAsString()
                + "](" + video.get("url").getAsString() + ")", false);

        if (!video.get("game").getAsString().equals("")) {
            String gameUrl = "https://www.twitch.tv/directory/game/"
                    + EncodingUtil.encodeURIComponent(video.get("game").getAsString());

            embed.addField("Game", "[" + video.get("game").getAsString() + "]("
                    + gameUrl + ")", false);
        }
        embed.setTimestamp(Instant.now());
        embed.setFooter(EncodingUtil.decodeTimeInSeconds(video.get("length").getAsInt()) + " Length", null);

        return embed.build();
    }
}

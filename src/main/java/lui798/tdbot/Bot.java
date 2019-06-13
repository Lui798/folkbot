package lui798.tdbot;

import com.google.gson.JsonElement;
import lui798.tdbot.util.EncodingUtil;
import lui798.tdbot.util.TwitchJSON;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Bot extends ListenerAdapter {
    private static Config config;
    private static String prefix;
    private final JDA jda;
    private static TwitchJSON json;
    private static TwitchJSON channel;

    private static final int EMBED_COLOR = 6570404;
    private static final int VOD_COLOR = 16070455;
    private static final String API_URL = "https://api.twitch.tv/kraken/";
    private static String USER_ID;
    private static String CLIENT_ID;

    private NumberFormat numberFormat = NumberFormat.getInstance();
    private Message currentMessage = null;
    private int maxViewers = 0;

    public static void main(String[] args) {
        config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getToken());

        if (args.length > 0) {
            new Bot(builder, args[0]);
        }
        else {
            new Bot(builder);
        }
    }

    public static void setJson() {
        CLIENT_ID = config.getClient();
        USER_ID = config.getUser();
        json = new TwitchJSON(API_URL + "streams/" + USER_ID + "?client_id=" + CLIENT_ID);
        channel = new TwitchJSON(API_URL + "channels/" + USER_ID + "?client_id=" + CLIENT_ID);
    }

    public static void setJson(String user) {
        CLIENT_ID = config.getClient();
        USER_ID = user;
        json = new TwitchJSON(API_URL + "streams/" + USER_ID + "?client_id=" + CLIENT_ID);
        channel = new TwitchJSON(API_URL + "channels/" + USER_ID + "?client_id=" + CLIENT_ID);
    }

    public Bot(JDABuilder builder) {
        this.jda = build(builder);
        jda.addEventListener(this);

        setJson();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                liveMain();
            }
        }, 5000, 20000);
    }

    public Bot(JDABuilder builder, String user) {
        this.jda = build(builder);
        jda.addEventListener(this);

        setJson(user);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                liveMain();
            }
        }, 5000, 20000);
    }

    public String getStatus() {
        if (!json.getStream().isJsonNull()) {
            JsonElement channel = json.getElement(json.getStream(), "channel");
            JsonElement status = json.getElement(channel, "status");

            return status.getAsString();
        }
        else return null;
    }

    public boolean checkIfLive() {
        json.updateJson();

        if (getStatus() != null) {
            return true;
        }
        return false;
    }

    public MessageEmbed liveEmbed() {
        JsonElement channel = TwitchJSON.getElement(json.getStream(), "channel");
        JsonElement preview = TwitchJSON.getElement(TwitchJSON.getElement(json.getStream(), "preview"), "template");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(EMBED_COLOR);
        embed.setAuthor(TwitchJSON.getString(channel, "display_name"), null,
                TwitchJSON.getString(channel, "logo"));
        embed.setImage(preview.getAsString().replace("{width}", "1152")
                .replace("{height}", "648"));
        embed.setThumbnail(TwitchJSON.getString(channel, "logo"));
        embed.addField("Stream", "[" + TwitchJSON.getString(channel, "status") + "]("
                + TwitchJSON.getString(channel, "url") + ")", false);
        if (!TwitchJSON.getString(channel, "game").equals("")) {
            String gameUrl = "https://www.twitch.tv/directory/game/"
                    + EncodingUtil.encodeURIComponent(TwitchJSON.getString(channel, "game"));

            embed.addField("Game", "[" + TwitchJSON.getString(channel, "game") + "]("
                    + gameUrl + ")", false);
        }
        embed.setTimestamp(Instant.now());
        embed.setFooter(numberFormat.format(Integer.parseInt(TwitchJSON.getString(json.getStream(), "viewers"))) + " Viewers", null);

        return embed.build();
    }

    public MessageEmbed vodEmbed() {
        TwitchJSON vodJSON = new TwitchJSON(API_URL + "channels/" + USER_ID
                + "/videos?client_id=" + CLIENT_ID + "&broadcast_type=archive");

        JsonElement video = TwitchJSON.getElement(vodJSON.getRoot(), "videos").getAsJsonArray().get(0);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(VOD_COLOR);
        embed.setAuthor(TwitchJSON.getString(channel.getRoot(), "display_name"), null,
                TwitchJSON.getString(channel.getRoot(), "logo"));
        if (TwitchJSON.getString(video, "preview").contains("_404")) {
            embed.setImage(TwitchJSON.getString(video, "preview"));
        } else {
            embed.setImage(TwitchJSON.getString(video, "preview")
                    .substring(0, TwitchJSON.getString(video, "preview").lastIndexOf("-")+1)
                    + "1152x648.jpg");
        }
        embed.setThumbnail(TwitchJSON.getString(channel.getRoot(), "logo"));
        embed.addField("Stream VOD", "[" + TwitchJSON.getString(video, "title")
                + "](" + TwitchJSON.getString(video, "url") + ")", false);
        if (!TwitchJSON.getString(video, "game").equals("")) {
            String gameUrl = "https://www.twitch.tv/directory/game/"
                    + EncodingUtil.encodeURIComponent(TwitchJSON.getString(video, "game"));

            embed.addField("Game", "[" + TwitchJSON.getString(video, "game") + "]("
                    + gameUrl + ")", false);
        }
        embed.setTimestamp(Instant.now());
        embed.setFooter(numberFormat.format(maxViewers) + " Peak Viewers", null);

        return embed.build();
    }

    public void updateLiveMessage(boolean isVod) {
        if (isVod) {
            currentMessage = currentMessage.editMessage(vodEmbed()).complete();
        }
        else {
            currentMessage = currentMessage.editMessage(liveEmbed()).complete();
        }
    }

    public void sendLiveMessage() {
        TextChannel textChannel = jda.getTextChannelById(config.getLiveChannel());
        currentMessage = textChannel.sendMessage(liveEmbed()).complete();
    }

    public void liveMain() {
        if (checkIfLive() && currentMessage == null) {
            sendLiveMessage();
            System.out.println("Sent live message");
            maxViewers = 0;
        }
        else if (checkIfLive() && currentMessage != null) {
            if (Integer.parseInt(TwitchJSON.getString(json.getStream(), "viewers")) > maxViewers) {
                maxViewers = Integer.parseInt(TwitchJSON.getString(json.getStream(), "viewers"));
            }
            updateLiveMessage(false);
            System.out.println("Updated live message");
        }
        else if (!checkIfLive()) {
            if (currentMessage != null) {
                updateLiveMessage(true);
                System.out.println("Updated live message to vod");
            }
            currentMessage = null;
        }
    }



    public void clear(TextChannel channel, String message) {
        int n;

        try {
            n = Integer.valueOf(message.substring(message.indexOf(" ")+1));
        }
        catch (NumberFormatException e) {
            channel.sendMessage(responseEmbed("Wrong input!",
                    "Please type in a valid integer. **" + prefix + "clear 0**")).queue();
            return;
        }

        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

        new Thread(() -> {
            List<Message> messages = channel.getHistory().retrievePast(n+1).complete();
            messages.removeIf(m -> m.getCreationTime().isBefore(twoWeeksAgo));
            messages.removeIf(m -> m.equals(currentMessage));

            if (messages.isEmpty()) {
                System.out.println("Done deleting: " + channel);
                return;
            }

            messages.forEach(m -> System.out.println("Deleting: " + m));
            channel.deleteMessages(messages).complete();
        }).run();
    }

    public void user(TextChannel channel, String message) {
        String n = message.substring(message.indexOf(" ")+1);

        config.setUser(n.trim());
        setJson();
        channel.sendMessage(responseEmbed("Successfully set!",
                "User is now set to: " + n.trim())).queue();
    }

    public void live(TextChannel channel) {
        config.setLiveChannel(channel.getId());
        channel.sendMessage(responseEmbed("Successfully set!",
                "Live notifications will be sent to this channel: " + channel.getName())).queue();
        System.out.println("Set live channel");
    }

    public JDA build(JDABuilder builder) {
        try {
            return builder.build();
        }
        catch (LoginException e) {
            System.out.println("Failed to login, check your token\nPress enter to exit");
            try {
                System.in.read();
            } catch (IOException eIO) {
                eIO.printStackTrace();
            }
            System.exit(0);
        }
        return null;
    }

    public MessageEmbed responseEmbed(String name, String value) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(EMBED_COLOR);
        embed.addField(name, value, false);

        return embed.build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            TextChannel channel = event.getTextChannel();
            Message message = event.getMessage();

            if (message.getContentRaw().equals(prefix + "live")
                    && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR)) {
                live(channel);
            } else if (message.getContentRaw().length() >= 6
                    ? message.getContentRaw().substring(0, 6).equals(prefix + "clear")
                    : message.getContentRaw().equals(prefix + "clear")
                    && message.getMember().getPermissions(channel).contains(Permission.MESSAGE_MANAGE)) {
                clear(channel, message.getContentRaw());
            } else if (message.getContentRaw().length() >= 5
                    ? message.getContentRaw().substring(0, 5).equals(prefix + "user")
                    : message.getContentRaw().equals(prefix + "user")
                    && message.getMember().getPermissions(channel).contains(Permission.MESSAGE_MANAGE)) {
                user(channel, message.getContentRaw());
            }
        }
    }
}

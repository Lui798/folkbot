package lui798.folkbot;

import com.google.gson.JsonElement;
import lui798.folkbot.command.Command;
import lui798.folkbot.command.RunnableC;
import lui798.folkbot.emote.EmoteParser;
import lui798.folkbot.util.CustomJSON;
import lui798.folkbot.util.EncodingUtil;
import lui798.folkbot.util.TwitchJSON;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.time.Duration;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Bot extends ListenerAdapter {
    private static Config config;
    private static String prefix;
    private static JDA jda;
    private static WebhookClient webhook;
    private static PircBotX irc;
    private static IRCBot ircBot;
    private static TwitchJSON json;
    private static TwitchJSON channel;

    private NumberFormat numberFormat = NumberFormat.getInstance();
    private Message currentMessage = null;
    private int maxViewers = 0;

    //Live notification settings
    private static final int EMBED_COLOR = 6570404;
    private static final int VOD_COLOR = 16070455;
    private static final String API_URL = "https://api.twitch.tv/kraken/";
    private static String USER_ID;
    private static String CLIENT_ID;

    public static void main(String[] args) {
        config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getToken());
        new Bot(builder);
    }

    public void setJson() {
        CLIENT_ID = config.getClient();
        USER_ID = config.getUser();
        json = new TwitchJSON(API_URL + "streams/" + USER_ID + "?client_id=" + CLIENT_ID);
        channel = new TwitchJSON(API_URL + "channels/" + USER_ID + "?client_id=" + CLIENT_ID);
    }

    public Bot(JDABuilder builder) {
        jda = build(builder);
        jda.addEventListener(this);

        setJson();
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

    @Override
    public void onReady(ReadyEvent event) {
        TextChannel chatChannel = jda.getTextChannelById(config.getProp("chatChannel"));

        boolean existingWebhook = false;
        for (Webhook hook : chatChannel.getWebhooks().complete()) {
            if (hook.getName().equals(jda.getSelfUser().getName())) {
                webhook = hook.newClient().build();
                existingWebhook = true;
            }
        }
        if (!existingWebhook) {
            webhook = chatChannel
                    .createWebhook(jda.getSelfUser().getName())
                    .complete()
                    .newClient()
                    .build();
        }

        ircBot = new IRCBot();
        Configuration ircConfig = new Configuration.Builder()
                .setName(config.getProp("ircUser"))
                .addServer("irc.chat.twitch.tv", 6667)
                .setServerPassword(config.getProp("ircOAuth"))
                .addListener(ircBot)
                .addAutoJoinChannel("#" + config.getUser())
                .buildConfiguration();

        irc = new PircBotX(ircConfig);
    }


    /*-----------------------------------
    Live notifications
    -----------------------------------*/

    public boolean checkIfLive() {
        json.updateJson();
        return !json.getStream().isJsonNull();
    }

    public MessageEmbed liveEmbed() {
        JsonElement channel = TwitchJSON.getElement(json.getStream(), "channel");
        JsonElement preview = TwitchJSON.getElement(TwitchJSON.getElement(json.getStream(), "preview"), "template");

        Duration date = Duration.between(Instant.from(OffsetDateTime.parse("2019-06-13T20:41:28Z", DateTimeFormatter.ISO_DATE_TIME)), Instant.now());

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
        embed.setFooter(numberFormat.format(Integer.parseInt(TwitchJSON.getString(json.getStream(), "viewers")))
                + " Viewers | " + date.toHours() + "h" + date.toMinutes()%60 + "m Uptime", null);

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


    /*-----------------------------------
    Discord messages and commands
    -----------------------------------*/

    public static MessageEmbed responseEmbed(String name, String value) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(EMBED_COLOR);
        embed.addField(name, value, false);

        return embed.build();
    }

    private Timer timer = new Timer();
    private boolean timerStarted = false;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        Message message = event.getMessage();

        Command live = new Command("live");
        Command chat = new Command("chat");
        Command user = new Command("user");
        Command clear = new Command("clear");

        //------Live command------//
        live.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                if (argument.equals("start")) {
                    message.delete().queue();
                    if (!timerStarted) {
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                liveMain();
                                timerStarted = true;
                            }
                        }, 0, 30000);
                    }
                } else {
                    config.setProp("liveChannel", argument);
                    channel.sendMessage(responseEmbed("Successfully set!",
                            "Live notifications will be sent to: **" + argument + "**")).queue();
                    System.out.println("Set live channel");
                }
            }
            @Override
            public void run() {
                run(channel.getId());
            }
        });
        //------Chat command------//
        chat.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                if (argument.equals("start")) {
                    message.delete().queue();
                    if (!irc.isConnected()) {
                        onReady(null);
                        new Thread(() -> {
                            try {
                                irc.startBot();
                            } catch (IrcException | IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        System.out.println("Chatbot started");
                    }
                } else if (argument.equals("stop")) {
                    irc.sendIRC().quitServer();
                    System.out.println("Chatbot stopped");
                } else {
                    config.setProp("chatChannel", argument);
                    channel.sendMessage(responseEmbed("Successfully set!",
                            "Twitch messages be sent to: **" + argument + "**")).queue();
                    System.out.println("Set chat channel");
                }
            }
            @Override
            public void run() {
                run(channel.getId());
            }
        });
        //------User command------//
        user.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                config.setProp("twitchUser", argument);
                setJson();
                channel.sendMessage(responseEmbed("Successfully set!",
                        "User is now set to: **" + argument + "**")).queue();
            }
            @Override
            public void run() {
                channel.sendMessage(responseEmbed("Wrong input!",
                        "Please type a username. **" + prefix + user.getName() + "** ***name***")).queue();
            }
        });
        //------Clear command------//
        clear.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                int n;

                try { n = Integer.valueOf(argument); }
                catch (NumberFormatException e) {
                    channel.sendMessage(responseEmbed("Wrong input!",
                            "Please type a valid integer. **" + prefix + clear.getName() + "** ***0***")).queue();
                    return;
                }

                if (n < 2) {
                    channel.sendMessage(responseEmbed("Wrong input!",
                            "Please type a integer greater than 1. **" + prefix + clear.getName() + "** ***2***")).queue();
                    return;
                }

                OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

                List<Message> messages = channel.getHistory().retrievePast(n+1).complete();
                messages.removeIf(m -> m.getCreationTime().isBefore(twoWeeksAgo));
                messages.removeIf(m -> m.equals(currentMessage));

                if (messages.isEmpty()) return;

                channel.deleteMessages(messages).complete();
                messages.forEach(m -> System.out.println("Deleted: " + m));
            }

            @Override
            public void run() {
                channel.sendMessage(responseEmbed("Wrong input!",
                        "Please type a valid integer. **" + prefix + clear.getName() + "** ***0***")).queue();
            }
        });

        EmoteParser parser = new EmoteParser(config.getUser());

        if (!event.getAuthor().isBot()) {
            String m = message.getContentDisplay();

            if (live.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                live.run(m);
            else if (chat.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                chat.run(m);
            else if (user.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                user.run(m);
            else if (clear.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                clear.run(m);
//            else if (irc.isConnected())
//                ircBot.sendMessage(parser.discordToTwitch(m), message.getAuthor().getName());
        }
    }


    /*-----------------------------------
    Twitch irc chatbot
    -----------------------------------*/

    public static class IRCBot extends org.pircbotx.hooks.ListenerAdapter {

        @Override
        public void onGenericMessage(GenericMessageEvent event) {
            String message = event.getMessage();
            String name = event.getUser().getNick();

            EmoteParser parser = new EmoteParser(config.getUser());
            message = parser.twitchToDiscord(message);

            CustomJSON twitchUser = new CustomJSON(API_URL + "channels/" + name + "?client_id=" + CLIENT_ID);
            String avatarUrl = CustomJSON.getString(twitchUser.getRoot(), "logo");

            WebhookMessageBuilder builder = new WebhookMessageBuilder()
                    .setUsername(name)
                    .setAvatarUrl(avatarUrl)
                    .setContent(message);

            webhook.send(builder.build());
        }

        @Override
        public void onPing(PingEvent event) {
            irc.sendRaw().rawLineNow(String.format("PONG %s\r\n", event.getPingValue()));
        }

        private void sendMessage(String message, String nick) {
            irc.sendIRC().message("#" + config.getUser(), "[" + nick + "]: " + message);
            System.out.println("Sent message to IRC #" + config.getUser());
        }
    }
}

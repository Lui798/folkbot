package lui798.tdbot;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import lui798.tdbot.util.TwitchJSON;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.List;

public class Bot extends ListenerAdapter {
    private static Config config;
    private static String prefix;
    private final JDABuilder jda;
    private static TwitchJSON json;

    private static final int EMBED_COLOR = 6570404;
    private static final String API_URL = "https://api.twitch.tv/kraken/";
    private static String USER_ID;
    private static String CLIENT_ID;

    public static void main(String[] args) {
        config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        CLIENT_ID = config.getClient();
        USER_ID = config.getUser();
        json = new TwitchJSON(API_URL + "streams/" + USER_ID + "?client_id=" + CLIENT_ID);

        builder.setToken(config.getToken());
        new Bot(builder);
    }

    public Bot(JDABuilder builder) {
        this.jda = builder;
        jda.addEventListener(this);
        build();
        System.out.println(checkIfLive());
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

        new Thread(() -> {
            List<Message> messages = channel.getHistory().retrievePast(n+1).complete();

            if (messages.isEmpty()) {
                System.out.println("Done deleting: " + channel);
                return;
            }

            messages.forEach(m -> System.out.println("Deleting: " + m));
            channel.deleteMessages(messages).complete();
        }).run();
    }

    public void live(TextChannel channel) {
        config.setLiveChannel(channel.getId());
        channel.sendMessage(responseEmbed("Successfully set!",
                "Live notifications will be sent to this channel: " + channel.getName())).queue();
        System.out.println("Set live channel");
    }

    public void build() {
        try {
            jda.build();
        }
        catch (LoginException e) {
            System.out.println("Failed to login, check your token\nPress enter to exit");
            try {
                System.in.read();
            } catch (IOException eIO) {
                System.out.println(eIO);
            }
            System.exit(0);
        }
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
            }
        }
    }
}

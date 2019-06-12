package lui798.tdbot;

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

    private final int EMBED_COLOR = 6570404;

    public static void main(String[] args) {
        config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getToken());
        new Bot(builder);
    }

    public Bot(JDABuilder builder) {
        this.jda = builder;
        jda.addEventListener(this);
        build();
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

    public MessageEmbed messageEmbed(String name, String value) {
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
            } else if (message.getContentRaw().substring(0, 6).equals(prefix + "clear")
                    && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR)) {
                clear(channel, message.getContentRaw());
            }
        }
    }

    public void live(TextChannel channel) {
        config.setLiveChannel(channel.getId());
        channel.sendMessage(messageEmbed("Successfully set!",
                "Live notifications will be sent to this channel: " + channel.getName())).queue();
        System.out.println("Set live channel");
    }

    public void clear(TextChannel channel, String message) {
        int n;

        try {
            n = Integer.valueOf(message.substring(message.indexOf(" ")+1));
        }
        catch (NumberFormatException e) {
            channel.sendMessage(messageEmbed("Wrong input!",
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
}

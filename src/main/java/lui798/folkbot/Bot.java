package lui798.folkbot;

import lui798.folkbot.util.Config;
import lui798.folkbot.util.DependencyFile;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Bot {
    public static String prefix;
    public static Config config;
    private static JDA jda;

    //Live notification settings
    public static final int ERROR_COLOR = 14696512;
    public static final int EMBED_COLOR = 7506394;

    public static void main(String[] args) throws Exception {
        new DependencyFile(new URL("https://yt-dl.org/downloads/latest/youtube-dl.exe"),
                System.getProperty("user.dir") + File.separator + "bin", "youtube-dl.exe");

        config = new Config();
        prefix = config.getProp("prefix");
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getProp("discordToken"));
        new Bot(builder);
    }

    private Bot(JDABuilder builder) throws InterruptedException {
        jda = build(builder);

        jda.awaitReady();
        for (Guild g : jda.getGuilds()) {
            jda.addEventListener(new BotListener(g));
        }
    }

    private JDA build(JDABuilder builder) {
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

    public static MessageEmbed responseEmbed(String name, String value, int color) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        embed.addField(name, value, false);

        return embed.build();
    }
}

package lui798.folkbot;

import lui798.folkbot.util.Config;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Bot {
    public static String prefix;
    public static Config config;
    private static JDA jda;

    public static void main(String[] args) throws Exception {
        config = new Config("halobot.conf");
        prefix = config.getProp("prefix");
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getProp("discordToken"));
        new Bot(builder);
    }

    private Bot(JDABuilder builder) throws InterruptedException {
        jda = build(builder);

        jda.awaitReady();
        for (Guild g : jda.getGuilds()) {
            jda.addEventListener(new BotListener(g, config));
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

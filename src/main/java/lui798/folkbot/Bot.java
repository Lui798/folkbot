package lui798.folkbot;

import lui798.folkbot.command.Command;
import lui798.folkbot.command.RunnableC;
import lui798.folkbot.player.AudioPlayerMain;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Bot extends ListenerAdapter {
    private static Config config;
    private static String prefix;
    private static JDA jda;

    private Message currentMessage = null;

    //Live notification settings
    public static final int ERROR_COLOR = 14696512;
    public static final int EMBED_COLOR = 7506394;

    public static void main(String[] args) {
        config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getToken());
        new Bot(builder);
    }

    public Bot(JDABuilder builder) {
        jda = build(builder);
        jda.addEventListener(this);
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



    /*-----------------------------------
    Discord messages and commands
    -----------------------------------*/

    public static MessageEmbed responseEmbed(String name, String value, int color) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        embed.addField(name, value, false);

        return embed.build();
    }

    private Timer timer = new Timer();
    private AudioManager manager = null;
    private AudioPlayerMain playerMain = null;

    private Message queueMessage = null;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getUser().isBot() && event.getMessageId().equals(queueMessage.getId())) {
            int index = Integer.parseInt(event.getReactionEmote().getName().substring(0, 1)) - 1;
            playerMain.getScheduler().play(index);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        Message message = event.getMessage();
        VoiceChannel voice = message.getMember().getVoiceState().getChannel();

        Command clear = new Command("clear");
        Command screen = new Command("screen");

        Command play = new Command("play");
        Command stop = new Command("stop");
        Command skip = new Command("skip");
        Command volume = new Command("volume");
        Command queue = new Command("queue");

        //------Clear command------//
        clear.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                int n;

                try { n = Integer.valueOf(argument); }
                catch (NumberFormatException e) {
                    channel.sendMessage(responseEmbed("Wrong input!",
                            "Please type a valid integer. **" + prefix + clear.getName() + "** ***0***", ERROR_COLOR)).queue();
                    return;
                }

                if (n < 2) {
                    channel.sendMessage(responseEmbed("Wrong input!",
                            "Please type a integer greater than 1. **" + prefix + clear.getName() + "** ***2***", ERROR_COLOR)).queue();
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
                        "Please type a valid integer. **" + prefix + clear.getName() + "** ***0***", ERROR_COLOR)).queue();
            }
        });
        //------Screenshare command------//
        screen.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                run();
            }

            @Override
            public void run() {
                if (message.getMember().getVoiceState().inVoiceChannel()) {
                    String guildID = channel.getGuild().getId();
                    String channelID = message.getMember().getVoiceState().getChannel().getId();
                    channel.sendMessage(responseEmbed("Screenshare "
                                    + message.getMember().getVoiceState().getChannel().getName(),
                            "Click [here](https://discordapp.com/channels/" + guildID + "/"
                                    + channelID + ")", EMBED_COLOR)).queue();
                }
                else {
                    channel.sendMessage(responseEmbed("Error", "You are not in a voice channel", ERROR_COLOR)).queue();
                }
            }
        });
        //------Player command------//
        play.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                if (manager == null && playerMain == null) {
                    playerMain = new AudioPlayerMain();
                    manager = message.getGuild().getAudioManager();
                    manager.setSendingHandler(playerMain.getHandler());
                    manager.openAudioConnection(voice);
                }

                MessageEmbed response = playerMain.loadItem(argument.trim());
                if (response != null) {
                    channel.sendMessage(response).queue();
                }
            }

            @Override
            public void run() {

            }
        });
        stop.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                run();
            }

            @Override
            public void run() {
                if (playerMain != null) {
                    playerMain.stopPlaying();
                    channel.sendMessage(responseEmbed("Player Queue", "Cleared queue and left the voice channel.", EMBED_COLOR)).queue();
                }
                if (manager != null) {
                    manager.closeAudioConnection();
                    manager = null;
                    playerMain = null;
                }
                timer.cancel();
            }
        });
        skip.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                run();
            }

            @Override
            public void run() {
                if (playerMain != null) {
                    playerMain.skipPlaying();
                    channel.sendMessage(responseEmbed("Player Queue", "Skipped current song.", EMBED_COLOR)).queue();
                }
                else {
                    channel.sendMessage(responseEmbed("Skip", "There is no song playing.", ERROR_COLOR)).queue();
                }
            }
        });
        volume.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                if (playerMain != null) {
                    MessageEmbed vol = playerMain.setVolume(argument.trim());
                    if (vol != null)
                        channel.sendMessage(vol).queue();
                }
                else {
                    channel.sendMessage(responseEmbed("Volume Adjustment", "No songs are playing.", ERROR_COLOR)).queue();
                }
            }

            @Override
            public void run() {
                channel.sendMessage(Bot.responseEmbed("Volume Level", "Volume is set to " + playerMain.getVolume() + "%", EMBED_COLOR)).queue();
            }
        });
        queue.setCom(new RunnableC() {
            @Override
            public void run(String argument) {
                run();
            }

            @Override
            public void run() {
                queueMessage = null;
                if (playerMain != null) {
                    String[] numbers = new String[] {"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3"};

                    queueMessage = channel.sendMessage(responseEmbed("Player Queue", playerMain.getQueue(), EMBED_COLOR)).complete();
                    for (int i = 1; i < playerMain.getScheduler().getQueue().size(); i++) {
                        queueMessage.addReaction(numbers[i+1]).queue();
                    }
                }
                else {
                    channel.sendMessage(responseEmbed("Player Queue", "No songs are in the queue.", ERROR_COLOR)).queue();
                }
            }
        });

        String m = message.getContentDisplay();

        if (!event.getAuthor().isBot() && message.getAttachments().isEmpty()) {
            if (clear.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                clear.run(m);
            else if (screen.equalsInput(m))
                screen.run(m);
            else if (play.equalsInput(m))
                play.run(m);
            else if (stop.equalsInput(m))
                stop.run(m);
            else if (skip.equalsInput(m))
                skip.run(m);
            else if (volume.equalsInput(m))
                volume.run(m);
            else if (queue.equalsInput(m))
                queue.run(m);
        }
        else if (!event.getAuthor().isBot() && !message.getAttachments().isEmpty()) {
            if (play.equalsInput(m))
                play.run(m + " " + message.getAttachments().get(0).getUrl());
        }
    }
}

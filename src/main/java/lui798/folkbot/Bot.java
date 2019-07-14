package lui798.folkbot;

import lui798.folkbot.command.Command;
import lui798.folkbot.command.RunnableC;
import lui798.folkbot.player.AudioPlayerMain;
import lui798.folkbot.util.Config;
import lui798.folkbot.util.DependencyFile;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Bot {
    public static String prefix;
    private static JDA jda;

    //Live notification settings
    public static final int ERROR_COLOR = 14696512;
    public static final int EMBED_COLOR = 7506394;

    public static void main(String[] args) throws Exception {
        new DependencyFile(new URL("https://yt-dl.org/downloads/2019.06.27/youtube-dl.exe"),
                System.getProperty("user.dir") + File.separator + "bin", "youtube-dl.exe");

        Config config = new Config();
        prefix = config.getPrefix();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(config.getToken());
        new Bot(builder);
    }

    private Bot(JDABuilder builder) throws InterruptedException {
        jda = build(builder);

        jda.awaitReady();
        for (Guild g : jda.getGuilds()) {
            jda.addEventListener(new Listener(g));
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

    private class Listener extends ListenerAdapter {
        private Guild guild;

        private Listener(Guild guild) {
            this.guild = guild;
        }

        private AudioManager manager = null;
        private AudioPlayerMain playerMain = null;
        private Message queueMessage = null;
        private TextChannel sleeping = null;

        private final String[] numbers = new String[]{"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3"};

        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event) {
            if (!event.getGuild().getId().equals(guild.getId())) return;

            if (!event.getUser().isBot() && queueMessage != null && event.getMessageId().equals(queueMessage.getId())) {
                int index = Integer.parseInt(event.getReactionEmote().getName().substring(0, 1)) - 1;
                playerMain.getScheduler().play(index);

                queueMessage.clearReactions().queue();
                queueMessage.editMessage(responseEmbed("Player Queue", playerMain.getQueue(), EMBED_COLOR)).queue();
                for (int i = 1; i < playerMain.getScheduler().getQueue().size() && i < 5; i++) {
                    queueMessage.addReaction(numbers[i + 1]).queue();
                }
            }
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (!event.getGuild().getId().equals(guild.getId()) || event.getAuthor().isBot()) return;

            TextChannel channel = event.getTextChannel();
            Message message = event.getMessage();
            VoiceChannel voice = message.getMember().getVoiceState().getChannel();

            Command clear = new Command("clear");
            Command screen = new Command("screen");
            Command sleep = new Command("sleep");

            Command play = new Command("play");
            Command stop = new Command("stop");
            Command skip = new Command("skip");
            Command volume = new Command("volume");
            Command queue = new Command("queue");

            Command teams = new Command("teams");
            Command game = new Command("game");
            Command register = new Command("register");

            //------Clear command------//
            clear.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    int n;

                    try {
                        n = Integer.valueOf(argument);
                    } catch (NumberFormatException e) {
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

                    message.delete().queue();
                    List<Message> messages = channel.getHistory().retrievePast(n).complete();
                    messages.removeIf(m -> m.getCreationTime().isBefore(twoWeeksAgo));

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
                    } else {
                        channel.sendMessage(responseEmbed("Error", "You are not in a voice channel", ERROR_COLOR)).queue();
                    }
                }
            });
            sleep.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    run();
                }

                @Override
                public void run() {
                    if (sleeping == null) {
                        sleeping = message.getTextChannel();
                        message.delete().queue();
                    }
                    else
                        sleeping = null;
                        message.delete().queue();
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
                    if (queueMessage != null) {
                        queueMessage.clearReactions().queue();
                        queueMessage = null;
                    }

                    if (playerMain != null) {
                        playerMain.stopPlaying();
                        channel.sendMessage(responseEmbed("Player Queue", "Cleared queue and left the voice channel.", EMBED_COLOR)).queue();
                    }
                    if (manager != null) {
                        manager.closeAudioConnection();
                        manager = null;
                        playerMain = null;
                    }
                }
            });
            skip.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    run();
                }

                @Override
                public void run() {
                    if (queueMessage != null) {
                        queueMessage.clearReactions().queue();
                        queueMessage = null;
                    }

                    if (playerMain != null) {
                        playerMain.skipPlaying();
                        channel.sendMessage(responseEmbed("Player Queue", "Skipped current song.", EMBED_COLOR)).queue();
                    } else {
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
                    } else {
                        channel.sendMessage(responseEmbed("Volume Adjustment", "No songs are playing.", ERROR_COLOR)).queue();
                    }
                }

                @Override
                public void run() {
                    if (playerMain != null)
                        channel.sendMessage(Bot.responseEmbed("Volume Level", "Volume is set to " + playerMain.getVolume() + "%", EMBED_COLOR)).queue();
                    else {
                        channel.sendMessage(Bot.responseEmbed("Error", "Bot is not in a voice channel.", ERROR_COLOR)).queue();
                    }
                }
            });
            queue.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    run();
                }

                @Override
                public void run() {
                    if (queueMessage != null) {
                        queueMessage.clearReactions().queue();
                        queueMessage = null;
                    }

                    if (playerMain != null) {
                        queueMessage = channel.sendMessage(responseEmbed("Player Queue", playerMain.getQueue(), EMBED_COLOR)).complete();
                        for (int i = 1; i < playerMain.getScheduler().getQueue().size() && i < 5; i++) {
                            queueMessage.addReaction(numbers[i + 1]).queue();
                        }
                    } else {
                        channel.sendMessage(responseEmbed("Player Queue", "No songs are in the queue.", ERROR_COLOR)).queue();
                    }
                }
            });

            teams.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    Role redTeamMember = guild.getRolesByName("Red Team Member", true).get(0);
                    Role blueTeamMember = guild.getRolesByName("Blue Team Member", true).get(0);
                    Role haloMember = guild.getRolesByName("Halo Member", true).get(0);

                    List<Member> guildMembers = new ArrayList<>(message.getGuild().getMembers());
                    guildMembers.removeIf(m -> m.getUser().isBot());

                    if (argument.equals("assign")) {
                        guildMembers.removeIf(m -> !m.getRoles().contains(haloMember));

                        for (Member member : guildMembers) {
                            if (member.getRoles().contains(redTeamMember) || member.getRoles().contains(blueTeamMember)) {
                                channel.sendMessage(responseEmbed("Error", "Teams are already assigned.\nPlease run **"
                                        + prefix + "teams clear** first.", ERROR_COLOR)).queue();
                                return;
                            }
                        }

                        Random r = new Random();
                        List<Member> assigned = new ArrayList<>();

                        int numRed = guildMembers.size() / 2;
                        int n;

                        while (!guildMembers.isEmpty() && assigned.size() < numRed) {
                            n = r.nextInt(guildMembers.size());
                            guild.getController().addRolesToMember(guildMembers.get(n), redTeamMember).complete();
                            System.out.println("Added " + guildMembers.get(n).getEffectiveName() + " to red team.");
                            assigned.add(guildMembers.remove(n));
                        }
                        while (!guildMembers.isEmpty()) {
                            n = r.nextInt(guildMembers.size());
                            guild.getController().addRolesToMember(guildMembers.get(n), blueTeamMember).complete();
                            System.out.println("Added " + guildMembers.get(n).getEffectiveName() + " to blue team.");
                            assigned.add(guildMembers.remove(n));
                        }

                        String redString = "";
                        String blueString = "";
                        for (Member member : assigned) {
                            if (member.getRoles().contains(redTeamMember))
                                redString += member.getEffectiveName() + "\n";
                            else if (member.getRoles().contains(blueTeamMember))
                                blueString += member.getEffectiveName() + "\n";
                        }

                        channel.sendMessage(responseEmbed("Red Team Members", redString, 15158332)).queue();
                        channel.sendMessage(responseEmbed("Blue Team Members", blueString, 3447003)).queue();
                    }
                    else if (argument.equals("clear")) {
                        List<Role> roles = new ArrayList<>();
                        roles.add(redTeamMember);
                        roles.add(blueTeamMember);

                        for (Member member : guildMembers) {
                            guild.getController().removeRolesFromMember(member, roles).queue();
                        }
                    }
                }

                @Override
                public void run() {

                }
            });
            game.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    final String G_PREFIX = "Game-";
                    Role everyone = guild.getPublicRole();

                    if (argument.startsWith("new")) {
                        List<Permission> membrPerms = new ArrayList<>();
                        List<Permission> adminPerms = new ArrayList<>();

                        int id = -1;
                        for (int i = 0; i < 10; i++) {
                            if (guild.getRolesByName(G_PREFIX + i, true).isEmpty()) {
                                id = i;
                                break;
                            }
                        }
                        if (id < 0)
                            channel.sendMessage(responseEmbed("Error", "No empty game slots available", ERROR_COLOR)).queue();

                        //Create chats and roles for new game
                        guild.getController().createCategory(G_PREFIX + id).complete();
                        Category category = guild.getCategoriesByName(G_PREFIX + id, true).get(0);
                        Role admin = guild.getController().createRole().setName(G_PREFIX + id + "-admin").complete();
                        Role member = guild.getController().createRole().setName(G_PREFIX + id).complete();
                        Channel gen = category.createVoiceChannel(G_PREFIX + id).setUserlimit(16).complete();
                        Channel red = category.createVoiceChannel("Red-" + id).setUserlimit(8).complete();
                        Channel blue = category.createVoiceChannel("Blue-" + id).setUserlimit(8).complete();

                        //Add permissions
                        //Member perms
                        membrPerms.add(Permission.VIEW_CHANNEL);
                        membrPerms.add(Permission.VOICE_CONNECT);
                        //Admin perms
                        adminPerms.add(Permission.VOICE_MOVE_OTHERS);
                        adminPerms.add(Permission.VOICE_DEAF_OTHERS);
                        adminPerms.add(Permission.VOICE_MUTE_OTHERS);

                        //Combine perms
                        List<Permission> combined = new ArrayList<>();
                        combined.addAll(membrPerms);
                        combined.addAll(adminPerms);

                        //Set perms on channels
                        category.getManager()
                                .putPermissionOverride(admin, combined, null)
                                .putPermissionOverride(member, membrPerms, null)
                                .putPermissionOverride(everyone, null, membrPerms).complete();
                        red.getManager().sync().complete();
                        blue.getManager().sync().complete();
                        gen.getManager().sync().complete();

                        //Set roles on game members
                        guild.getController().addSingleRoleToMember(message.getMember(), admin).queue();
                        for (Member m : message.getMentionedMembers()) {
                            guild.getController().addSingleRoleToMember(m, member).queue();
                        }

                        channel.sendMessage(responseEmbed("Success", "Your new game has been created with\nid `"
                                + id + "`, please find your new channel.", EMBED_COLOR)).queue();
                    }
                    else if (argument.startsWith("end")) {
                        Role admin = null;
                        Role member = null;

                        int id = -1;
                        for (int i = 0; i < 10; i++) {
                            if (!guild.getRolesByName(G_PREFIX + i + "-admin", true).isEmpty())
                            if (message.getMember().getRoles().contains(guild.getRolesByName(G_PREFIX + i + "-admin", true).get(0))) {
                                id = i;
                                admin = guild.getRolesByName(G_PREFIX + id + "-admin", true).get(0);
                                member = guild.getRolesByName(G_PREFIX + id, true).get(0);
                                break;
                            }
                        }
                        if (id < 0) {
                            channel.sendMessage(responseEmbed("Error", "You are not an admin of a game.", ERROR_COLOR)).queue();
                            return;
                        }

                        admin.delete().queue();
                        member.delete().queue();

                        guild.getVoiceChannelsByName(G_PREFIX + id, true).get(0).delete().complete();
                        guild.getVoiceChannelsByName("Red-" + id, true).get(0).delete().complete();
                        guild.getVoiceChannelsByName("Blue-" + id, true).get(0).delete().complete();
                        guild.getCategoriesByName(G_PREFIX + id, true).get(0).delete().complete();
                    }
                }

                @Override
                public void run() {
                    channel.sendMessage(responseEmbed("Error", "Please specify an argument.\n"
                            + prefix + "game <arg>", ERROR_COLOR)).queue();
                }
            });
            register.setCom(new RunnableC() {
                @Override
                public void run(String argument) {
                    Role banned = guild.getRolesByName("Banned", true).get(0);
                    Role haloMember = guild.getRolesByName("Halo Member", true).get(0);

                    if (argument.equals("halo")) {
                        if (message.getMember().getRoles().contains(banned)) {
                            channel.sendMessage(responseEmbed("Error", "Cannot give role, you are banned.", ERROR_COLOR)).queue();
                            return;
                        }
                        else if (message.getMember().getRoles().contains(haloMember)) {
                            channel.sendMessage(responseEmbed("Error", "Cannot give role, you are already \na halo member.", ERROR_COLOR)).queue();
                            return;
                        }
                        List<Role> roles = new ArrayList<>();
                        roles.add(haloMember);

                        guild.getController().addRolesToMember(message.getMember(), roles).complete();
                        channel.sendMessage(responseEmbed("Success", message.getMember().getEffectiveName() + " is now a Halo Member.", EMBED_COLOR)).queue();
                    }
                }

                @Override
                public void run() {
                    channel.sendMessage(responseEmbed("Error", "Please specify an argument.\n"
                            + prefix + "register <role>", ERROR_COLOR)).queue();
                }
            });

            String m = message.getContentDisplay();

            if (message.getAttachments().isEmpty()) {
                if (clear.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                    clear.run(m);
                else if (screen.equalsInput(m))
                    screen.run(m);
                else if (sleep.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                    sleep.run(m);
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
                //else if (teams.equalsInput(m) && message.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR))
                //    teams.run(m);
                else if (game.equalsInput(m))
                    game.run(m);
                else if (register.equalsInput(m))
                    register.run(m);
                else if (sleeping != null && sleeping == event.getTextChannel())
                    event.getMessage().delete().queue();
                else return;
                System.out.println(message.getAuthor().getName() + " > " + m);
            } else if (!message.getAttachments().isEmpty() && !message.getAttachments().get(0).isImage()) {
                if (play.equalsInput(m))
                    play.run(m + " " + message.getAttachments().get(0).getUrl());
            }
        }
    }
}

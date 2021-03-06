package lui798.folkbot;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.command.util.CommandRunner2;
import lui798.folkbot.player.AudioPlayerMain;
import lui798.folkbot.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static lui798.folkbot.command.player.PlayerCommand.numbers;

public class BotListener extends ListenerAdapter {
    private Guild guild;
    private Config config;
    private CommandRunner2 runner;

    private final Logger LOG = LoggerFactory.getLogger(BotListener.class);

    public BotListener(Guild guild, Config config) {
        this.config = config;
        this.guild = guild;
        this.runner = new CommandRunner2(guild);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getUser().isBot()) return;
        if (runner.getPlayerController().getQueueMessage() == null) return;

        Message queueMessage = runner.getPlayerController().getQueueMessage();
        AudioPlayerMain playerMain = runner.getPlayerController().getPlayerMain();

        if (event.getMessageId().equals(queueMessage.getId())) {
            int index = Integer.parseInt(event.getReactionEmote().getName().substring(0, 1)) - 1;
            playerMain.getScheduler().play(index);

            queueMessage.clearReactions().queue();
            queueMessage.editMessage(responseEmbed("Player Queue", playerMain.getQueue(), CommandResult.DEFAULT_COLOR)).queue();
            for (int i = 1; i < playerMain.getScheduler().getQueue().size() && i < 9; i++) {
                queueMessage.addReaction(numbers[i + 1]).queue();
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        CommandResult result = null;

        final List<String> MEDIA_LINKS = new ArrayList<>();
        MEDIA_LINKS.add("facebook.com");
        MEDIA_LINKS.add("youtube.com");
        MEDIA_LINKS.add("youtu.be");
        MEDIA_LINKS.add("instagram.com");
        MEDIA_LINKS.add("twitter.com");
        MEDIA_LINKS.add("imgur.com");
        MEDIA_LINKS.add("reddit.com");
        MEDIA_LINKS.add("cdn.discordapp.com");
        MEDIA_LINKS.add("pornhub.com");


        if (runner.isCommand(message.getContentDisplay(), config.getProp("prefix"))
                && config.getList("noCommands").contains(message.getTextChannel().getId()) && !message.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            new Thread(() -> {
                try {
                    Message response = message.getTextChannel().sendMessage(responseEmbed("Not Allowed",
                            "Please send commands in the <#" + config.getProp("commands") + "> chat.", CommandResult.ERROR_COLOR)).complete();
                    Thread.sleep(1000);
                    message.delete().complete();
                    Thread.sleep(5000);
                    response.delete().complete();
                }
                catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }).start();
        }
        else if (runner.isCommand(message.getContentDisplay(), config.getProp("prefix"))) {
            result = runner.runCommand(message);

            String m = message.getContentDisplay();
            if (message.getAttachments().isEmpty())
                LOG.info(message.getAuthor().getName() + " > " + m);
        }
        else if (config.getList("noMedia").contains(message.getTextChannel().getId()) && !message.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            if (!message.getAttachments().isEmpty()) {
                new Thread(() -> {
                    try {
                        Message response = message.getTextChannel().sendMessage(responseEmbed("Not Allowed",
                                "Please send media in the <#" + config.getProp("media") + "> chat.", CommandResult.ERROR_COLOR)).complete();
                        Thread.sleep(1000);
                        message.delete().complete();
                        Thread.sleep(5000);
                        response.delete().complete();
                    }
                    catch (Exception e) {
                        LOG.error(e.getMessage());
                    }
                }).start();
            }
            else {
                for (String s : MEDIA_LINKS) {
                    if (message.getContentDisplay().contains(s)) {
                        new Thread(() -> {
                            try {
                                Message response = message.getTextChannel().sendMessage(responseEmbed("Not Allowed",
                                        "Please send media in the <#" + config.getProp("media") + "> chat.", CommandResult.ERROR_COLOR)).complete();
                                Thread.sleep(1000);
                                message.delete().complete();
                                Thread.sleep(5000);
                                response.delete().complete();
                            }
                            catch (Exception e) {
                                LOG.error(e.getMessage());
                            }
                        }).start();
                        break;
                    }
                }
            }

        }

        try {
            message.getTextChannel().sendMessage(responseEmbed(result.getResult(), result.getDesc(), result.getColor())).queue();
        }
        catch (NullPointerException e) { }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getUser().isBot()) return;

        if (!config.getProp("joinRoles").equals("default") && !config.getProp("joinGuilds").equals("default")) {

            ArrayList<Guild> guilds = new ArrayList<>();
            ArrayList<Role> roles = new ArrayList<>();

            for (String s : config.getList("joinGuilds")) {
                guilds.add(event.getJDA().getGuildById(s));
            }

            if (guilds.contains(guild)) {
                for (String s : config.getList("joinRoles")) {
                    roles.add(guild.getRolesByName(s, false).get(0));
                }

                for (Role r : roles) {
                    guild.addRoleToMember(event.getMember(), r).queue();
                }
                System.out.println("Assigned roles to new member " + event.getMember().getEffectiveName() + " in Guild " + event.getGuild().getName());
            }
        }
    }

    public MessageEmbed responseEmbed(String name, String value, int color) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        embed.addField(name, value, false);

        return embed.build();
    }
}

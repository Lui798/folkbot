package lui798.folkbot;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.command.util.CommandRunner2;
import lui798.folkbot.player.AudioPlayerMain;
import lui798.folkbot.util.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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

        if (runner.isCommand(message.getContentDisplay(), config.getProp("prefix"))) {
            result = runner.runCommand(message);

            String m = message.getContentDisplay();
            if (message.getAttachments().isEmpty())
                LOG.info(message.getAuthor().getName() + " > " + m);
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

                guild.getController().addRolesToMember(event.getMember(), roles).queue();
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

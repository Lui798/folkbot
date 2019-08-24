package lui798.folkbot;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.command.util.CommandRunner2;
import lui798.folkbot.util.Config;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotListener extends ListenerAdapter {
    private Guild guild;
    private Config config;
    private CommandRunner2 runner;

    private final Logger LOG = LoggerFactory.getLogger(BotListener.class);

    public BotListener(Guild guild, Config config) {
        this.guild = guild;
        this.config = config;
        this.runner = new CommandRunner2(config, guild);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getUser().isBot()) return;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        CommandResult result = null;

        if (runner.isCommand(message.getContentDisplay(), Bot.prefix)) {
            result = runner.runCommand(message);

            String m = message.getContentDisplay();
            if (message.getAttachments().isEmpty())
                LOG.info(message.getAuthor().getName() + " > " + m);
        }

        try {
            message.getTextChannel().sendMessage(Bot.responseEmbed(result.getResult(), result.getDesc(), result.getColor())).queue();
        }
        catch (NullPointerException e) { }

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getUser().isBot()) return;
    }
}

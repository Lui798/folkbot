package lui798.folkbot;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.command.util.CommandRunner2;
import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.util.Config;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BotListener extends ListenerAdapter {
    private Guild guild;
    private Config config;
    private CommandRunner2 runner;
    private ServerConnection server;

    public BotListener(Guild guild, Config config) {
        this.guild = guild;
        this.config = config;
        this.runner = new CommandRunner2();

        if (guild.getTextChannels().contains(guild.getJDA().getTextChannelById(config.getProp("rconChat")))) {
            List<String> admins = config.getList("serverAdmins");
            server = new ServerConnection(this.config.getProp("serverIP"), this.config.getProp("rconPort"), this.config.getProp("gamePort"),
                    this.config.getProp("rconPass"), guild.getTextChannelById(this.config.getProp("rconChat")), admins);
        }
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
                System.out.println(message.getAuthor().getName() + " > " + m);
        }
        else if (message.getTextChannel().getId().equals(config.getProp("rconChat"))) {
            if (!message.getMember().getUser().getId().equals("463122243300360192"))
                server.send(message.getContentDisplay());
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

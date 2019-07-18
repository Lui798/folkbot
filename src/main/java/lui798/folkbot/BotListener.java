package lui798.folkbot;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.command.util.CommandRunner2;
import lui798.folkbot.player.AudioPlayerMain;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;

import static lui798.folkbot.command.player.PlayerCommand.numbers;

public class BotListener extends ListenerAdapter {
    private Guild guild;
    private CommandRunner2 runner;

    public BotListener(Guild guild) {
        this.guild = guild;
        this.runner = new CommandRunner2(guild);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals(guild.getId())) return;

        Message queueMessage = runner.getPlayerController().getQueueMessage();
        AudioPlayerMain playerMain = runner.getPlayerController().getPlayerMain();

        if (!event.getUser().isBot() && event.getMessageId().equals(queueMessage.getId())) {
            int index = Integer.parseInt(event.getReactionEmote().getName().substring(0, 1)) - 1;
            playerMain.getScheduler().play(index);

            queueMessage.clearReactions().queue();
            queueMessage.editMessage(Bot.responseEmbed("Player Queue", playerMain.getQueue(), Bot.EMBED_COLOR)).queue();
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

        if (runner.isCommand(message.getContentDisplay(), Bot.prefix)) {
            result = runner.runCommand(message);

            String m = message.getContentDisplay();
            if (message.getAttachments().isEmpty())
                System.out.println(message.getAuthor().getName() + " > " + m);
        }

        try {
            message.getTextChannel().sendMessage(Bot.responseEmbed(result.getResult(), result.getDesc(), result.getColor())).queue();
        }
        catch (NullPointerException e) { }

//        } else if (!message.getAttachments().isEmpty() && !message.getAttachments().get(0).isImage()) {
//            if (play.equalsInput(m))
//                play.run(m + " " + message.getAttachments().get(0).getUrl());
//        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(guild.getId()) || event.getUser().isBot()) return;

        if (!Bot.config.getProp("joinRoles").equals("unchanged") && !Bot.config.getProp("joinGuilds").equals("unchanged")) {

            ArrayList<Guild> guilds = new ArrayList<>();
            ArrayList<Role> roles = new ArrayList<>();

            for (String s : Bot.config.getList("joinGuilds")) {
                guilds.add(event.getJDA().getGuildById(s));
            }

            if (guilds.contains(guild)) {
                for (String s : Bot.config.getList("joinRoles")) {
                    roles.add(guild.getRolesByName(s, false).get(0));
                }

                guild.getController().addRolesToMember(event.getMember(), roles).queue();
                System.out.println("Assigned roles to new member " + event.getMember().getEffectiveName() + " in Guild " + event.getGuild().getName());
            }
        }
    }
}

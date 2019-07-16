package lui798.folkbot.command.player;

import lui798.folkbot.Bot;
import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class QueueCommand extends PlayerCommand {

    public QueueCommand() {
        setName("queue");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        TextChannel channel = message.getTextChannel();

        if (queueMessage != null) {
            queueMessage.clearReactions().queue();
            queueMessage = null;
        }

        if (playerMain != null) {
            queueMessage = channel.sendMessage(Bot.responseEmbed("Player Queue", playerMain.getQueue(), Bot.EMBED_COLOR)).complete();
            for (int i = 1; i < playerMain.getScheduler().getQueue().size() && i < 5; i++) {
                queueMessage.addReaction(numbers[i + 1]).queue();
            }
        } else {
            return new CommandResult(CommandResult.ERROR, "No songs are in the queue.", CommandResult.ERROR_COLOR);
        }

        return null;
    }
}

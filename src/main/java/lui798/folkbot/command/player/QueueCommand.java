package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class QueueCommand extends PlayerCommand {

    public QueueCommand(PlayerController controller) {
        super(controller);
        setName("queue");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        TextChannel channel = message.getTextChannel();

        clearQueueMessage();

        getController().setQueueMessage(channel.sendMessage(queueEmbed(getController().getPlayerMain().getQueue())).complete());
        for (int i = 1; i < getController().getPlayerMain().getScheduler().getQueue().size() && i < 9; i++) {
            getController().getQueueMessage().addReaction(numbers[i + 1]).queue();
        }

        return null;
    }

    private MessageEmbed queueEmbed(String queue) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.addField("Player Queue", queue, false);
        builder.setColor(CommandResult.DEFAULT_COLOR);

        return builder.build();
    }
}

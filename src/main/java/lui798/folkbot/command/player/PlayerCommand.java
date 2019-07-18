package lui798.folkbot.command.player;

import lui798.folkbot.command.Command;
import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public abstract class PlayerCommand extends Command {
    private PlayerController controller;

    public PlayerCommand(PlayerController controller) {
        this.controller = controller;
    }

    public static final String[] numbers = new String[]{"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3"};

    @Override
    public abstract CommandResult run(Message message, List<String> arguments);

    public PlayerController getController() {
        return controller;
    }

    public void clearQueueMessage() {
        if (controller.getQueueMessage() != null) {
            controller.getQueueMessage().clearReactions().queue();
            controller.setQueueMessage(null);
        }
    }
}

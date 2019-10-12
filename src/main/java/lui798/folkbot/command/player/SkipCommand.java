package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class SkipCommand extends PlayerCommand {

    public SkipCommand(PlayerController controller) {
        super(controller);
        setName("skip");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        CommandResult result;

        clearQueueMessage();

        if (getController().getPlayerMain().isPlaying()) {
            getController().getPlayerMain().skipPlaying();
            result = new CommandResult("Player Queue", "Skipped current song.", CommandResult.DEFAULT_COLOR);
        }
        else {
            result = new CommandResult("Skip", "There is no song playing.", CommandResult.ERROR_COLOR);
        }

        return result;
    }
}

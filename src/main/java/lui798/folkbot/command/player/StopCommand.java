package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class StopCommand extends PlayerCommand {

    public StopCommand(PlayerController controller) {
        super(controller);
        setName("stop");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {

        clearQueueMessage();

        if (getController().getManager().isConnected()) {
            getController().getPlayerMain().stopPlaying();
            getController().getManager().closeAudioConnection();

            return new CommandResult("Player Queue", "Cleared queue and left the voice channel.", CommandResult.DEFAULT_COLOR);
        }

        return null;
    }
}

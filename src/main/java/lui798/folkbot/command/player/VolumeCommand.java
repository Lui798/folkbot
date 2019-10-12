package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class VolumeCommand extends PlayerCommand {

    public VolumeCommand(PlayerController controller) {
        super(controller);
        setName("volume");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        CommandResult result;

        if (arguments.isEmpty()) {
            if (getController().getManager().isConnected())
                result = new CommandResult("Volume Level", "Volume is set to " + getController().getPlayerMain().getVolume() + "%", CommandResult.DEFAULT_COLOR);
            else {
                result = new CommandResult(CommandResult.ERROR, "Bot is not in a voice channel.", CommandResult.ERROR_COLOR);
            }
        }
        else {
            if (getController().getManager().isConnected())
                result = getController().getPlayerMain().setVolume(arguments.get(0).trim());
            else
                result = new CommandResult(CommandResult.ERROR, "No songs are playing.", CommandResult.ERROR_COLOR);
        }

        return result;
    }
}

package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class VolumeCommand extends PlayerCommand {

    public VolumeCommand() {
        setName("volume");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        CommandResult result = null;

        if (arguments.isEmpty()) {
            if (playerMain != null)
                result = new CommandResult("Volume Level", "Volume is set to " + playerMain.getVolume() + "%", CommandResult.DEFAULT_COLOR);
            else {
                result = new CommandResult(CommandResult.ERROR, "Bot is not in a voice channel.", CommandResult.ERROR_COLOR);
            }
        }
        else {
            if (playerMain != null)
                result = playerMain.setVolume(arguments.get(0).trim());
            else
                result = new CommandResult(CommandResult.ERROR, "No songs are playing.", CommandResult.ERROR_COLOR);
        }

        return result;
    }
}

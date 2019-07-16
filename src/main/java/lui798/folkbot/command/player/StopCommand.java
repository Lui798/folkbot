package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class StopCommand extends PlayerCommand {

    public StopCommand() {
        setName("stop");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        CommandResult result = null;

        if (queueMessage != null) {
            queueMessage.clearReactions().queue();
            queueMessage = null;
        }

        if (playerMain != null) {
            playerMain.stopPlaying();
            result = new CommandResult("Player Queue", "Cleared queue and left the voice channel.", CommandResult.DEFAULT_COLOR);
        }
        if (manager != null) {
            manager.closeAudioConnection();
            manager = null;
            playerMain = null;
        }

        return result;
    }
}

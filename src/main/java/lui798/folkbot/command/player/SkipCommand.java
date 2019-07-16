package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class SkipCommand extends PlayerCommand {

    public SkipCommand() {
        setName("skip");
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
            playerMain.skipPlaying();
            result = new CommandResult("Player Queue", "Skipped current song.", CommandResult.DEFAULT_COLOR);
        } else {
            result = new CommandResult("Skip", "There is no song playing.", CommandResult.ERROR_COLOR);
        }

        return result;
    }
}

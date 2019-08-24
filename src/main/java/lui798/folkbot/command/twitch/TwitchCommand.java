package lui798.folkbot.command.twitch;

import lui798.folkbot.command.Command;
import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.twitch.TwitchController;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public abstract class TwitchCommand extends Command {
    private TwitchController controller;

    public TwitchCommand(TwitchController controller) {
        this.controller = controller;
    }

    @Override
    public abstract CommandResult run(Message message, List<String> arguments);

    public TwitchController getController() {
        return controller;
    }
}

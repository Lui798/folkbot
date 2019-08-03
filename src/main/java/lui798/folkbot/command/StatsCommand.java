package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand() {
        setName("stats");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        return null;
    }
}

package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public abstract class Command {
    private String name;

    public abstract CommandResult run(Message message, List<String> arguments);

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

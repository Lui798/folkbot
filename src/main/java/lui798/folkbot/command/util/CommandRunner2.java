package lui798.folkbot.command.util;

import lui798.folkbot.command.*;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRunner2 {
    private final List<Command> COMMANDS = new ArrayList<>();

    public CommandRunner2() {
        COMMANDS.add(new CommandClear());
    }

    private Command getCommand(String command) {
        for (Command c : COMMANDS) {
            if (command.equals(c.getName()))
                return c;
        }
        return null;
    }

    private List<String> parseInput(String input) {
        return new ArrayList<>(Arrays.asList(input.split(" ")));
    }

    public CommandResult runCommand(Message message) {
        List<String> parts = parseInput(message.getContentDisplay().substring(1));
        String command = parts.remove(0);

        CommandResult result;

        try {
            result = getCommand(command).run(message, parts);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            result = new CommandResult(CommandResult.ERROR, "That didn't work.");
        }

        return result;
    }

    public boolean isCommand(String message, String prefix) {
        boolean result = false;

        if (message.startsWith(prefix))
            for (Command c : COMMANDS) {
                if (message.substring(1).startsWith(c.getName()))
                    result = true;
            }

        return result;
    }
}

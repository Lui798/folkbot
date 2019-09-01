package lui798.folkbot.halo.command.util;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.command.*;
import lui798.folkbot.halo.command.admin.*;
import lui798.folkbot.halo.object.ChatMessage;
import net.dv8tion.jda.core.entities.SelfUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HaloCommandRunner {
    private final List<HaloCommand> COMMANDS = new ArrayList<>();
    private ServerConnection server;

    public HaloCommandRunner(ServerConnection server) {
        this.server = server;

        //HALO COMMANDS
        COMMANDS.add(new DiscordCommand());
        COMMANDS.add(new HelpCommand());
        COMMANDS.add(new kdComand());
        COMMANDS.add(new ReportCommand());

        //ADMIN COMMANDS
        COMMANDS.add(new aEndGameCommand());
        COMMANDS.add(new aKickComand());
        COMMANDS.add(new aKickIndexComand());
        COMMANDS.add(new aKickRandomComand());
        COMMANDS.add(new aShuffleTeamsCommand());
    }

    private HaloCommand getCommand(String command) {
        for (HaloCommand c : COMMANDS) {
            if (command.equals(c.getName()))
                return c;
        }
        return null;
    }

    private String parseInput(String input) {
        try {
            return input.substring(input.indexOf(' ') + 1);
        }
        catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    private String parseCommand(String input) {
        try {
            return input.substring(0, input.indexOf(' '));
        }
        catch (StringIndexOutOfBoundsException e) {
            return input;
        }
    }

    public String runCommand(ChatMessage c, boolean permission) {
        String argument = parseInput(c.message.substring(1));
        String command = parseCommand(c.message.substring(1)).toLowerCase();

        String result;

        try {
            if (getCommand(command).hasPermission(permission))
                result = getCommand(command).run(server, c, argument, permission);
            else
                result = "You don't have permission to run that command.";
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            result = "That didn't work.";
        }

        return result;
    }

    public boolean isCommand(String message, String prefix) {
        boolean result = false;

        if (message.startsWith(prefix))
            for (HaloCommand c : COMMANDS) {
                if (message.toLowerCase().substring(1).startsWith(c.getName()))
                    result = true;
            }

        return result;
    }
}

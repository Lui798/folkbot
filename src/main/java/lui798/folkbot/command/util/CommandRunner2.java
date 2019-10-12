package lui798.folkbot.command.util;

import lui798.folkbot.command.*;
import lui798.folkbot.command.player.*;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRunner2 {
    private final List<Command> COMMANDS = new ArrayList<>();
    private PlayerController playerController;

    public CommandRunner2(Guild guild) {
        playerController = new PlayerController(guild);

        //NORMAL COMMANDS
        COMMANDS.add(new ClearCommand());
        COMMANDS.add(new ScreenCommand());
        COMMANDS.add(new EmbedCommand());
        COMMANDS.add(new StatsCommand());

        //PLAYER COMMANDS
        COMMANDS.add(new PlayCommand(playerController));
        COMMANDS.add(new StopCommand(playerController));
        COMMANDS.add(new SkipCommand(playerController));
        COMMANDS.add(new VolumeCommand(playerController));
        COMMANDS.add(new QueueCommand(playerController));
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    private Command getCommand(String command) {
        for (Command c : COMMANDS) {
            if (command.startsWith(c.getName()))
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
            if (getCommand(command).hasPermission(message))
                result = getCommand(command).run(message, parts);
            else
                result = new CommandResult(CommandResult.ERROR, "You don't have permission to\nrun that command.");
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            result = new CommandResult(CommandResult.ERROR, "That didn't work.", CommandResult.ERROR_COLOR);
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

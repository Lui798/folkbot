package lui798.folkbot.halo.command.admin;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.command.HaloCommand;
import lui798.folkbot.halo.object.ChatMessage;

public class aEndGameCommand extends HaloCommand {

    public aEndGameCommand() {
        setName("a.endgame");
        setAdmin(true);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        server.send("Game.End");
        return "Ended the game successfully.";
    }
}

package lui798.folkbot.halo.command.admin;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.command.HaloCommand;
import lui798.folkbot.halo.object.ChatMessage;

public class aShuffleTeamsCommand extends HaloCommand {

    public aShuffleTeamsCommand() {
        setName("a.shuffleteams");
        setAdmin(true);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        server.send("Server.ShuffleTeams");
        return "Teams have been shuffled successfully.";
    }
}

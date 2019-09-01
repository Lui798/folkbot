package lui798.folkbot.halo.command.admin;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.command.HaloCommand;
import lui798.folkbot.halo.object.ChatMessage;

public class aKickIndexComand extends HaloCommand {

    public aKickIndexComand() {
        setName("a.kickindex");
        setAdmin(true);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        if (!argument.trim().equals("")) {
            server.send("Server.KickIndex " + argument);
            return "Kicked player [" + argument + "] successfully.";
        }
        else {
            return "Please type the command like this: !a.kickIndex index";
        }
    }
}

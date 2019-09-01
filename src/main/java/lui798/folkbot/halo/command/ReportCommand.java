package lui798.folkbot.halo.command;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.object.ChatMessage;
import lui798.folkbot.halo.object.Player;

import java.util.List;

public class ReportCommand extends HaloCommand {

    public ReportCommand() {
        setName("report");
        setAdmin(false);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        String r = null;

        if (argument.trim().equals(""))
            r = "Please type the command like this: !report name";
        else {
            Player p = server.getPlayers().find(argument);
            server.sendToDiscord(server.embedMessage("Player Reported", "Name - " + p.name +
                    "\nIP - " + p.ip + "\nUID - " + p.uid, 14696512));
            r = "Player successfully reported.";
        }

        return r;
    }
}

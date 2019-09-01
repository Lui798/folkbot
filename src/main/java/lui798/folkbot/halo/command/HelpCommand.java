package lui798.folkbot.halo.command;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.object.ChatMessage;

public class HelpCommand extends HaloCommand {

    public HelpCommand() {
        setName("help");
        setAdmin(false);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        if (admin) {
            server.sendPM(c.name, "!a.endGame - Ends the current game.");
            server.sendPM(c.name, "!a.shuffleTeams - Shuffles the teams.");
            server.sendPM(c.name, "!a.kick - Kicks the player.");
            server.sendPM(c.name, "!a.kickIndex - Kicks the player by index.");
            server.sendPM(c.name, "!a.kickRandom - Kicks a random player in the session.");
        }

        server.sendPM(c.name, "!report - Reports specified player to the admins.");
        server.sendPM(c.name, "!discord - Sends you a link to our discord.");
        server.sendPM(c.name, "!kd - Tells you your current K/D.");

        return null;
    }
}

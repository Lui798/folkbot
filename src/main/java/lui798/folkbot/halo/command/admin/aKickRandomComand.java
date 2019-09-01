package lui798.folkbot.halo.command.admin;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.command.HaloCommand;
import lui798.folkbot.halo.object.ChatMessage;
import lui798.folkbot.halo.object.PlayerList;

import java.util.Random;

public class aKickRandomComand extends HaloCommand {

    public aKickRandomComand() {
        setName("a.kickrandom");
        setAdmin(true);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {

        PlayerList players = new PlayerList();
        players.addAll(server.getPlayers());
        players.removeIf(player -> server.getAdmins().contains(player.uid));

        if (players.size() > 0) {
            Random random = new Random();
            argument = players.get(random.nextInt(players.size())).name;

            server.send("Server.KickPlayer " + argument);
            return "Kicked random player [" + argument + "] successfully.";
        }
        else {
            return "There are no players to kick.";
        }


    }
}

package lui798.folkbot.halo.util;

import lui798.folkbot.halo.object.Player;

import java.util.ArrayList;

public class PlayerList extends ArrayList<Player> {

    public Player find(String name) {
        for (Player p : this) {
            if (name.equals(p.name))
                return p;
        }
        return null;
    }
}

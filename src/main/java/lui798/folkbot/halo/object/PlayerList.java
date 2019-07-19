package lui798.folkbot.halo.object;

import java.util.ArrayList;

public class PlayerList extends ArrayList<Player> {

    public Player find(String name) {
        for (Player p : this) {
            if (name.equals(p.name))
                return p;
        }
        return null;
    }

    public Player find(String name, String uid) {
        for (Player p : this) {
            if (name.equals(p.name) && uid.equals(p.uid))
                return p;
        }
        return null;
    }
}

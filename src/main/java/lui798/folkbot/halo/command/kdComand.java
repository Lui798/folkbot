package lui798.folkbot.halo.command;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.object.ChatMessage;
import lui798.folkbot.halo.object.Player;

import java.math.BigDecimal;

public class kdComand extends HaloCommand {

    public kdComand() {
        setName("kd");
        setAdmin(false);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        Player p = server.getPlayers().find(c.name, c.uid);
        return "You have a K/D of " + round(p.kills / (float)p.deaths, 2);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd;
        try {
            bd = new BigDecimal(Float.toString(d));
        }
        catch (NumberFormatException e) {
            bd = new BigDecimal("0.00");
        }
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}

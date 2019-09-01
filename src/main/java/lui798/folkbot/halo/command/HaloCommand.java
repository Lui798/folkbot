package lui798.folkbot.halo.command;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.object.ChatMessage;

public abstract class HaloCommand {
    private String name;
    private boolean admin;

    public abstract String run(ServerConnection server, ChatMessage c, String argument, boolean admin);

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }

    public boolean hasPermission(boolean isAdmin) {
        if (admin)
            return isAdmin;
        else return true;
    }
}

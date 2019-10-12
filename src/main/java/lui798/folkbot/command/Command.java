package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public abstract class Command {
    private String name;
    private List<Permission> perms;

    public abstract CommandResult run(Message message, List<String> arguments);

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPerms(List<Permission> perms) {
        this.perms = perms;
    }

    public boolean hasPermission(Message message) {
        if (perms == null)
            return true;

        int n = perms.size();
        for (int i = 0; i < perms.size(); i++) {
            if (message.getMember().getPermissions(message.getTextChannel()).contains(perms.get(i))) n--;
        }
        if (n == 0)
            return true;

        return false;
    }
}

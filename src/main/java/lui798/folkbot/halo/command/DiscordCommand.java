package lui798.folkbot.halo.command;

import lui798.folkbot.halo.ServerConnection;
import lui798.folkbot.halo.object.ChatMessage;

import java.util.List;

public class DiscordCommand extends HaloCommand {

    public DiscordCommand() {
        setName("discord");
        setAdmin(false);
    }

    @Override
    public String run(ServerConnection server, ChatMessage c, String argument, boolean admin) {
        return "@" + c.name + ", here's your invite: https://discord.gg/HpNBESJ";
    }
}

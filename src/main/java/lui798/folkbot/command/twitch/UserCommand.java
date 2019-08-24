package lui798.folkbot.command.twitch;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.twitch.TwitchController;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;

public class UserCommand extends TwitchCommand {

    public UserCommand(TwitchController controller) {
        super(controller);
        setName("user");
        setPerms(Arrays.asList(Permission.ADMINISTRATOR));
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        if (arguments.size() == 0) {
            return new CommandResult("User", "User is currently set to **"
                    + getController().getConfig().getProp("twitchUser") + "**.");
        }
        else {
            getController().getConfig().setProp("twitchUser", arguments.get(0));
            getController().setJson();
            return new CommandResult(CommandResult.SUCCESS, "User is now set to **" + arguments.get(0) + "**.");
        }
    }
}

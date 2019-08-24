package lui798.folkbot.command.twitch;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.twitch.TwitchController;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

public class LiveCommand extends TwitchCommand {

    Logger LOG = LoggerFactory.getLogger(LiveCommand.class);

    public LiveCommand(TwitchController controller) {
        super(controller);
        setName("live");
        setPerms(Arrays.asList(Permission.ADMINISTRATOR));
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        if (arguments.get(0).equals("start")) {
            message.delete().queue();
            if (!getController().isTimerStarted()) {
                getController().getLiveTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getController().liveMain();
                        getController().setTimerStarted(true);
                    }
                }, 0, 30000);
            }
        }
        else if (arguments.get(0).equals("stop") && getController().isTimerStarted()) {
            getController().getLiveTimer().cancel();
            getController().setTimerStarted(false);
        }
        else {
            getController().getConfig().setProp("liveChannel", arguments.get(0));
            LOG.info("Set live channel");
            return new CommandResult(CommandResult.SUCCESS, "Live notifications will be sent to <#" + arguments.get(0) + ">");
        }
        return null;
    }
}

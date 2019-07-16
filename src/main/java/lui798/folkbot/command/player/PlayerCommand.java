package lui798.folkbot.command.player;

import lui798.folkbot.command.Command;
import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.AudioPlayerMain;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.List;

public abstract class PlayerCommand extends Command {
    public static AudioManager manager = null;
    public static AudioPlayerMain playerMain = null;
    public static Message queueMessage = null;

    public static final String[] numbers = new String[]{"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3"};

    @Override
    public abstract CommandResult run(Message message, List<String> arguments);
}

package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.PlayerController;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

public class PlayCommand extends PlayerCommand {

    public PlayCommand(PlayerController controller) {
        super(controller);
        setName("play");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        VoiceChannel voice;

        if (message.getMember().getVoiceState().inVoiceChannel())
            voice = message.getMember().getVoiceState().getChannel();
        else
            return new CommandResult(CommandResult.ERROR, "You are not in a voice channel.", CommandResult.ERROR_COLOR);

        if (!getController().getManager().isConnected()) {
            getController().getManager().setSendingHandler(getController().getPlayerMain().getHandler());
            getController().getManager().openAudioConnection(voice);
        }

        return getController().getPlayerMain().loadItem(arguments.get(0).trim());
    }
}

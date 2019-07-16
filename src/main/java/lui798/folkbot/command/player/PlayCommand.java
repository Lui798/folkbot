package lui798.folkbot.command.player;

import lui798.folkbot.command.util.CommandResult;
import lui798.folkbot.player.AudioPlayerMain;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

public class PlayCommand extends PlayerCommand {

    public PlayCommand() {
        setName("play");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        VoiceChannel voice = null;

        if (message.getMember().getVoiceState().inVoiceChannel())
            voice = message.getMember().getVoiceState().getChannel();

        if (voice == null)
            return new CommandResult(CommandResult.ERROR, "You are not in a voice channel.", CommandResult.ERROR_COLOR);
        else if (manager == null && playerMain == null) {
            playerMain = new AudioPlayerMain();
            manager = message.getGuild().getAudioManager();
            manager.setSendingHandler(playerMain.getHandler());
            manager.openAudioConnection(voice);
        }

        return playerMain.loadItem(arguments.get(0).trim());
    }
}

package lui798.folkbot.command;

import lui798.folkbot.command.util.CommandResult;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class ScreenCommand extends Command {
    public ScreenCommand() {
        setName("screen");
        setPerms(null);
    }

    @Override
    public CommandResult run(Message message, List<String> arguments) {
        TextChannel channel = message.getTextChannel();

        if (message.getMember().getVoiceState().inVoiceChannel()) {
            String guildID = channel.getGuild().getId();
            String channelID = message.getMember().getVoiceState().getChannel().getId();
            return new CommandResult("Screenshare "
                    + message.getMember().getVoiceState().getChannel().getName(),
                    "Click [here](https://discordapp.com/channels/" + guildID + "/" + channelID + ")");
        } else {
            return new CommandResult(CommandResult.ERROR, "You are not in a voice channel", CommandResult.ERROR_COLOR);
        }
    }
}

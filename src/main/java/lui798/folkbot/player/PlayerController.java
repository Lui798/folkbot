package lui798.folkbot.player;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.managers.AudioManager;

public class PlayerController {
    private AudioManager manager;
    private AudioPlayerMain playerMain;
    private Message queueMessage;

    public PlayerController(Guild guild) {
        manager = guild.getAudioManager();
        playerMain = new AudioPlayerMain();
    }

    public AudioManager getManager() {
        return manager;
    }

    public AudioPlayerMain getPlayerMain() {
        return playerMain;
    }

    public Message getQueueMessage() {
        return queueMessage;
    }

    public void setQueueMessage(Message queueMessage) {
        this.queueMessage = queueMessage;
    }
}

package lui798.folkbot.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lui798.folkbot.Bot;
import net.dv8tion.jda.core.entities.TextChannel;

public class AudioPlayerMain {

    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private AudioPlayerSendHandler handler;
    private TrackScheduler scheduler;

    private TextChannel channel;

    public AudioPlayerMain(TextChannel channel) {
        this.channel = channel;
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);

        this.player = this.playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        player.addListener(this.scheduler);

        this.handler = new AudioPlayerSendHandler(this.player);

    }

    public AudioPlayerSendHandler getHandler() {
        return this.handler;
    }

    public String getQueue() {
        String output = "";
        AudioTrack currentTrack = player.getPlayingTrack();

        if (currentTrack != null) {
            output += "1). [" + currentTrack.getInfo().author + " - " + currentTrack.getInfo().title + "](" + currentTrack.getInfo().uri + ")\n";
        }

        if (!scheduler.getQueue().isEmpty()) {
            int count = 2;
            for (AudioTrack track : scheduler.getQueue()) {
                output += count + "). [" + track.getInfo().author + " - " + track.getInfo().title + "](" + track.getInfo().uri + ")\n";
                count++;
            }
        }

        if (output.isEmpty()) {
            return "No songs are in the queue.";
        }

        return output;
    }

    public void loadItem(String id) {
        playerManager.loadItem(id, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                scheduler.queue(track);
                scheduler.play();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    scheduler.queue(track);
                }
                scheduler.play();
            }

            @Override
            public void noMatches() {
                channel.sendMessage(Bot.responseEmbed("No Match Found", "No song was found for that link")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (exception.severity == FriendlyException.Severity.COMMON) {
                    channel.sendMessage(Bot.responseEmbed("Loading Error", exception.getMessage())).queue();
                }
            }
        });
    }

    public void stopPlaying() {
        scheduler.stop();
    }

    public void skipPlaying() {
        scheduler.skip();
    }
}

package lui798.folkbot.player;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lui798.folkbot.Bot;
import lui798.folkbot.util.YouTubeHelper;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class AudioPlayerMain {

    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private AudioPlayerSendHandler handler;
    private TrackScheduler scheduler;

    public AudioPlayerMain() {
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.MEDIUM);
        playerManager.getConfiguration().setOpusEncodingQuality(10);

        AudioSourceManagers.registerRemoteSources(this.playerManager);

        this.player = this.playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        player.addListener(this.scheduler);

        this.handler = new AudioPlayerSendHandler(this.player);

    }

    public AudioPlayerSendHandler getHandler() {
        return this.handler;
    }

    public TrackScheduler getScheduler() {
        return this.scheduler;
    }

    public String getQueue() {
        String output = "";

        if (!scheduler.getQueue().isEmpty()) {
            int count = 1;
            for (AudioTrack track : scheduler.getQueue()) {
                AudioTrackInfo info = (AudioTrackInfo) track.getUserData();
                output += count + "). " + info.author + " - " + info.title + "\n";
                count++;
            }
        }

        if (output.isEmpty()) {
            return "No songs are in the queue.";
        }

        return output;
    }

    private AudioTrack yt;
    private MessageEmbed response;

    public MessageEmbed loadItem(String id) {
        String url;
        response = null;

        try {
            url = YouTubeHelper.videoUrlProcess(id);
            yt = (AudioTrack) new YoutubeAudioSourceManager().loadTrackWithVideoId(YouTubeHelper.extractVideoIdFromUrl(id), false);
        }
        catch (FriendlyException e) {
            url = id;
            yt = null;
        }

        if (yt == null && !YouTubeHelper.isValidUrl(url)) {
            response = Bot.responseEmbed("No Match Found", "No song was found for that link", Bot.ERROR_COLOR);
            return response;
        }

        for (AudioTrack track : scheduler.getQueue()) {
            AudioTrackInfo info = (AudioTrackInfo) track.getUserData();
            if (info.title.equals(yt.getInfo().title)) {
                response = Bot.responseEmbed("Loading Error", "That song is already in the queue.", Bot.ERROR_COLOR);
                return response;
            }
        }

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (yt == null) {
                    yt = track;
                }
                scheduler.queue(track, yt);
                scheduler.play();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
//                for (AudioTrack track : playlist.getTracks()) {
//                    scheduler.queue(track);
//                }
//                scheduler.play();
                if (yt == null) {
                    yt = playlist.getTracks().get(0);
                }
                scheduler.queue(playlist.getTracks().get(0), yt);
            }

            @Override
            public void noMatches() {
                response = Bot.responseEmbed("No Match Found", "No song was found for that link", Bot.ERROR_COLOR);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (exception.severity == FriendlyException.Severity.COMMON) {
                    Bot.responseEmbed("Loading Error", exception.getMessage(), Bot.ERROR_COLOR);
                }
            }
        });

        return response;
    }

    public void stopPlaying() {
        scheduler.stop();
    }

    public void skipPlaying() {
        scheduler.skip();
    }

    public String getVolume() {
        return Integer.toString(player.getVolume());
    }

    public MessageEmbed setVolume(String level) {
        int level2;
        try {
            if (level.equals("default") || level.equals("normal")) {
                level2 = 100;
            }
            else {
                level2 = Integer.parseInt(level);
                if (level2 > 5000) {
                    level2 = 5000;
                }
                else if (level2 < 0) {
                    level2 = 0;
                }
            }
        }
        catch (NumberFormatException e) {
            response = Bot.responseEmbed("Volume Error", "Please type a valid number 0-100.", Bot.ERROR_COLOR);
            return response;
        }
        player.setVolume(level2);
        response = Bot.responseEmbed("Volume Adjustment", "Volume set to " + level2 + "%", Bot.EMBED_COLOR);

        return response;
    }
}

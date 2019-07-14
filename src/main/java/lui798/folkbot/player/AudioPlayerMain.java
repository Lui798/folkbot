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
            for (int i = 0; i < scheduler.getQueue().size() && i < 5; i++) {
                AudioTrack track = scheduler.getQueue().get(i);
                AudioTrackInfo info = (AudioTrackInfo) track.getUserData();
                output += i+1 + "). [" + info.author + " - " + info.title + "](" + info.uri + ")\n";
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

        if (!id.contains("list=")) {
            try {
                url = YouTubeHelper.videoUrlProcess(id);
                yt = (AudioTrack) new YoutubeAudioSourceManager().loadTrackWithVideoId(YouTubeHelper.extractVideoIdFromUrl(id), false);
            } catch (FriendlyException e) {
                url = id;
                yt = null;
            }
        }
        else {
            url = id;
        }

        if (yt == null && !YouTubeHelper.isValidUrl(url)) {
            response = Bot.responseEmbed("No Match Found", "No song was found for that link", Bot.ERROR_COLOR);
            return response;
        }

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (yt != null)
                    System.out.println("Loaded: " + yt.getIdentifier());
                else
                    System.out.println("Loaded: " + track.getIdentifier());

                scheduler.queue(track, yt);
                scheduler.play();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                loadItem(playlist.getTracks().get(0).getIdentifier());
                for (int i = 1; i < playlist.getTracks().size(); i++) {
                    loadItem(playlist.getTracks().get(i).getIdentifier());
                }
            }

            @Override
            public void noMatches() {
                response = Bot.responseEmbed("No Match Found", "No song was found for that link", Bot.ERROR_COLOR);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (exception.severity == FriendlyException.Severity.COMMON) {
                    response = Bot.responseEmbed("Loading Error", exception.getMessage(), Bot.ERROR_COLOR);
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
            else if (level.equals("max")) {
                level2 = 5000;
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
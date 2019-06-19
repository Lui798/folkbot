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
//        AudioTrack currentTrack = player.getPlayingTrack();
//        AudioTrackInfo currentInfo = (AudioTrackInfo) currentTrack.getUserData();
//
//        if (currentTrack != null) {
//            output += "1). " + currentInfo.author + " - " + currentInfo.title + "\n";
//        }

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
        System.out.println(output);
        return output;
    }

    private AudioTrack yt;

    public void loadItem(String id) {
        String url;

        try {
            url = YouTubeHelper.videoUrlProcess(id);
            yt = (AudioTrack) new YoutubeAudioSourceManager().loadTrackWithVideoId(YouTubeHelper.extractVideoIdFromUrl(id), false);
        }
        catch (FriendlyException e) {
            url = id;
            yt = null;
        }

        if (yt == null && !YouTubeHelper.isValidUrl(url)) {
            channel.sendMessage(Bot.responseEmbed("No Match Found", "No song was found for that link")).queue();
            return;
        }

        for (AudioTrack track : scheduler.getQueue()) {
            AudioTrackInfo info = (AudioTrackInfo) track.getUserData();
            if (info.title.equals(yt.getInfo().title)) {
                channel.sendMessage(Bot.responseEmbed("Loading Error", "That song is already in the queue.")).queue();
                return;
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

    public String getVolume() {
        return Integer.toString(player.getVolume());
    }

    public String setVolume(String level) {
        int level2 = 100;
        try {
            if (level.equals("default") || level.equals("normal")) {
                level2 = 100;
            }
            else {
                level2 = Integer.parseInt(level);
                if (level2 > 999999) {
                    level2 = 999999;
                }
                else if (level2 < 0) {
                    level2 = 0;
                }
            }
        }
        catch (NumberFormatException e) {
            channel.sendMessage(Bot.responseEmbed("Volume Error", "Please type a valid number 0-100.")).queue();
            return null;
        }
        player.setVolume(level2);
        return Integer.toString(level2);
    }
}

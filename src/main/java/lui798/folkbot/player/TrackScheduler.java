package lui798.folkbot.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;

public class TrackScheduler extends AudioEventAdapter {
    private AudioPlayer player;
    private List<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new ArrayList<>();
    }

    public void queue(AudioTrack track, AudioTrack yt) {
        if (yt == null)
            yt = track;

        track.setUserData(yt.getInfo());
        queue.add(track);
    }

    public List<AudioTrack> getQueue() {
        return this.queue;
    }

    public void play() {
        player.startTrack(queue.get(0), true);
    }

    public void play(int index) {
        player.stopTrack();
        queue.subList(0, index).clear();
        play();
    }

    public void stop() {
        queue.clear();
        player.stopTrack();
    }

    public void skip() {
        player.stopTrack();
        try {
            queue.remove(0);
            play();
        }
        catch (IndexOutOfBoundsException e) {
            return;
        }
    }

    public AudioTrack getPlaying() {
        return player.getPlayingTrack();
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            queue.remove(0);
            play();
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }
}

package com.oscar.discorddndbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackScheduler implements AudioLoadResultHandler {

    private final AudioPlayer player;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
    }

    public void stopPlaying() {
        this.player.stopTrack();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        player.playTrack(track);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // TODO Auto-generated method stub
    }

    @Override
    public void loadFailed(FriendlyException arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void noMatches() {
        // TODO Auto-generated method stub

    }

}

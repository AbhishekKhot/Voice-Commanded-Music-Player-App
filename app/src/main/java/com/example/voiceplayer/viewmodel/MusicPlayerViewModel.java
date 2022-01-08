package com.example.voiceplayer.viewmodel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlayer;

public class MusicPlayerViewModel extends ViewModel {
    private final MutableLiveData<ExoPlayer> player = new MutableLiveData<>();

    public MutableLiveData<ExoPlayer> getPlayer() {
        return player;
    }
}

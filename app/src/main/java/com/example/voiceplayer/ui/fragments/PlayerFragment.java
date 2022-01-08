package com.example.voiceplayer.ui.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voiceplayer.R;
import com.example.voiceplayer.viewmodel.MusicPlayerViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerFragment extends Fragment {

    View rootView;
    MusicPlayerViewModel musicPlayerViewModel;
    ImageView backBtn, voiceBtn;
    TextView txtCurrentSongName, startDuration, endDuration;
    CircleImageView circleSongImageView;
    ImageButton playPauseBtn, prevBtn, nextBtn, fastForwardBtn, fastRewindBtn;
    SeekBar seekBar;
    ExoPlayer exoPlayer;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String command = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_player, container, false);
        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicPlayerViewModel = new ViewModelProvider(requireActivity()).get(MusicPlayerViewModel.class);
        backBtn = rootView.findViewById(R.id.imageViewBackBtn);
        circleSongImageView = rootView.findViewById(R.id.imageViewCircle);
        txtCurrentSongName = rootView.findViewById(R.id.txtCurrentSongName);
        txtCurrentSongName.setSelected(true);
        seekBar = rootView.findViewById(R.id.seekBar);
        startDuration = rootView.findViewById(R.id.txtStartPosition);
        endDuration = rootView.findViewById(R.id.txtEndPosition);
        prevBtn = rootView.findViewById(R.id.previousBtn);
        nextBtn = rootView.findViewById(R.id.nextBtn);
        playPauseBtn = rootView.findViewById(R.id.playPauseSongBtn);
        fastForwardBtn = rootView.findViewById(R.id.fastForwardBtn);
        fastRewindBtn = rootView.findViewById(R.id.fastRewindBtn);
        voiceBtn = rootView.findViewById(R.id.imageButtonMicrophone);
        backBtn.setOnClickListener(view1 -> requireActivity().onBackPressed());
        gettingPlayer();


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        voiceBtn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    speechRecognizer.startListening(speechRecognizerIntent);
                    break;
                case MotionEvent.ACTION_UP:
                    speechRecognizer.stopListening();
                    break;
            }
            return false;
        });

        speechToCommandConversion();
    }

    private void gettingPlayer() {
        musicPlayerViewModel.getPlayer().observe(requireActivity(), livePlayer -> {
            if (livePlayer != null) {
                exoPlayer = livePlayer;
                playerControls(exoPlayer);
            }
        });
    }

    private void playerControls(ExoPlayer player) {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                assert mediaItem != null;
                txtCurrentSongName.setText(mediaItem.mediaMetadata.title);
                startDuration.setText(getFormattedTime((int) player.getCurrentPosition()));
                seekBar.setProgress((int) player.getCurrentPosition());
                endDuration.setText(getFormattedTime((int) player.getDuration()));
                seekBar.setMax((int) player.getDuration());
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                showCurrentArtwork();
                updatePlayerPositionProgress();
                circleSongImageView.setAnimation(showImageRotations());
                if (!player.isPlaying()) {
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    txtCurrentSongName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                    startDuration.setText(getFormattedTime((int) player.getCurrentPosition()));
                    seekBar.setProgress((int) player.getCurrentPosition());
                    endDuration.setText(getFormattedTime((int) player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                    showCurrentArtwork();
                    updatePlayerPositionProgress();
                    circleSongImageView.setAnimation(showImageRotations());

                } else {
                    playPauseBtn.setImageResource(R.drawable.ic_play);
                }
            }
        });

        if (player.isPlaying()) {
            txtCurrentSongName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
            startDuration.setText(getFormattedTime((int) player.getCurrentPosition()));
            seekBar.setProgress((int) player.getCurrentPosition());
            endDuration.setText(getFormattedTime((int) player.getDuration()));
            seekBar.setMax((int) player.getDuration());
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            showCurrentArtwork();
            updatePlayerPositionProgress();
            circleSongImageView.setAnimation(showImageRotations());
        }

        nextBtn.setOnClickListener(view -> {
            if (player.hasNextMediaItem()) {
                player.seekToNext();
                showCurrentArtwork();
                updatePlayerPositionProgress();
                circleSongImageView.setAnimation(showImageRotations());
            }
        });

        prevBtn.setOnClickListener(view -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious();
                showCurrentArtwork();
                updatePlayerPositionProgress();
                circleSongImageView.setAnimation(showImageRotations());
            }
        });

        playPauseBtn.setOnClickListener(view -> {
            if (player.isPlaying()) {
                player.pause();
                playPauseBtn.setImageResource(R.drawable.ic_play);
                circleSongImageView.clearAnimation();
            } else {
                player.play();
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                circleSongImageView.setAnimation(showImageRotations());
            }
        });

        fastForwardBtn.setOnClickListener(view -> {
            if (player.isPlaying()) {
                player.seekTo(player.getCurrentPosition() + 10000);
            }
        });

        fastRewindBtn.setOnClickListener(view -> {
            if (player.isPlaying()) {
                player.seekTo(player.getCurrentPosition() - 10000);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(progressValue);
                startDuration.setText(getFormattedTime(progressValue));
                player.seekTo(progressValue);
            }
        });

    }

    private Animation showImageRotations() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer.isPlaying()) {
                    startDuration.setText(getFormattedTime((int) exoPlayer.getCurrentPosition()));
                    seekBar.setProgress((int) exoPlayer.getCurrentPosition());
                }
                updatePlayerPositionProgress();
            }
        }, 1000);
    }

    private void showCurrentArtwork() {
        circleSongImageView.setImageURI(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.artworkUri);
        if (circleSongImageView.getDrawable() == null) {
            circleSongImageView.setImageResource(R.drawable.background_mixer);
        }
    }

    String getFormattedTime(int totalDuration) {
        String time;
        int hrs = totalDuration / (1000 * 60 * 60);
        int min = (totalDuration % (1000 * 60 * 60)) / (1000 * 60);
        int secs = (((totalDuration % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        if (hrs < 1) {
            time = min + ":" + secs;
        } else {
            time = hrs + ":" + min + ":" + secs;
        }
        return time;
    }

    private void speechToCommandConversion() {

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matchesFound != null) {
                    command = matchesFound.get(0);

                    if (command.equals("pause")) {
                        if (exoPlayer.isPlaying()) {
                            exoPlayer.pause();
                            playPauseBtn.setImageResource(R.drawable.ic_play);
                            circleSongImageView.clearAnimation();
                            Toast.makeText(requireContext(), command, Toast.LENGTH_LONG).show();
                        }
                    } else if (command.equals("play")) {
                        if (!exoPlayer.isPlaying()) {
                            exoPlayer.play();
                            playPauseBtn.setImageResource(R.drawable.ic_pause);
                            circleSongImageView.setAnimation(showImageRotations());
                            Toast.makeText(requireContext(), command, Toast.LENGTH_LONG).show();
                        }
                    } else if (command.equals("play next song")) {
                        if (exoPlayer.hasNextMediaItem()) {
                            exoPlayer.seekToNext();
                            showCurrentArtwork();
                            updatePlayerPositionProgress();
                            circleSongImageView.setAnimation(showImageRotations());
                        }
                    } else if (command.equals("play previous song")) {
                        if (exoPlayer.hasPreviousMediaItem()) {
                            exoPlayer.seekToPrevious();
                            showCurrentArtwork();
                            updatePlayerPositionProgress();
                            circleSongImageView.setAnimation(showImageRotations());
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

}
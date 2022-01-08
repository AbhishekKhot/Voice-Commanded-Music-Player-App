package com.example.voiceplayer.ui;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voiceplayer.R;
import com.example.voiceplayer.adapter.MusicPlayerAdapter;
import com.example.voiceplayer.data.Song;
import com.example.voiceplayer.ui.fragments.PlayerFragment;
import com.example.voiceplayer.viewmodel.MusicPlayerViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String> storagePermissionLauncher;
    ActivityResultLauncher<String> audioPermissionLauncher;
    RecyclerView recyclerview;
    MusicPlayerAdapter musicPlayerAdapter;
    ConstraintLayout constraintLayout;
    TextView txtPlayingSongName,txtPlayPreviousSong,txtPlayNextSong;
    ImageButton PlayAndPauseSong;
    ExoPlayer exoPlayer;
    MusicPlayerViewModel musicPlayerViewModel;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerview = findViewById(R.id.recyclerview);
        txtPlayingSongName = findViewById(R.id.txtPlayingSongName);
        txtPlayingSongName.setSelected(true);
        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        txtPlayPreviousSong = findViewById(R.id.txtPlayPreviousSong);
        txtPlayNextSong = findViewById(R.id.txtPlayNextSong);
        PlayAndPauseSong = findViewById(R.id.playPauseBtn);
        constraintLayout = findViewById(R.id.ConstraintLayoutContainer);


        musicPlayerViewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);
        musicPlayerViewModel.getPlayer().setValue(exoPlayer);

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result){
                fetchSongs();
            }
            else {
                onUserActionsStoragePermission();
            }
        });
        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        audioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result){
                Toast.makeText(getApplicationContext(), "permission granted", Toast.LENGTH_SHORT).show();
            }
            else {
                onUserActionsAudioPermission();
            }
        });
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);

        playerControls();
    }

    private void playerControls() {
        constraintLayout.setOnClickListener(view -> {
            PlayerFragment playerViewFragment = new PlayerFragment();
            String fragmentTag = playerViewFragment.getClass().getName();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_container, playerViewFragment)
                    .addToBackStack(fragmentTag)
                    .commit();
        });

        txtPlayNextSong.setOnClickListener(view -> {
            if (exoPlayer.hasNextMediaItem()){
                exoPlayer.seekToNext();
            }
        });

        txtPlayPreviousSong.setOnClickListener(view -> {
            if (exoPlayer.hasPreviousMediaItem()){
                exoPlayer.seekToPrevious();
            }
        });

        PlayAndPauseSong.setOnClickListener(view -> {
            if (exoPlayer.isPlaying()){
                exoPlayer.pause();
                PlayAndPauseSong.setImageResource(R.drawable.ic_play);
            }else {
                if (exoPlayer.getMediaItemCount()>0){
                    exoPlayer.play();
                    PlayAndPauseSong.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        playerListener();
    }

    private void playerListener() {
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                assert mediaItem != null;
                txtPlayingSongName.setText(mediaItem.mediaMetadata.title);
            }
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY){
                    txtPlayingSongName.setText(Objects.requireNonNull(exoPlayer.getCurrentMediaItem()).mediaMetadata.title);
                    PlayAndPauseSong.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onUserActionsStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            fetchSongs();
        }
        else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Required Permissions")
                    .setMessage("Without this permission this app doesn't work")
                    .setPositiveButton("Allow ", (dialogInterface, i) ->
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .setNegativeButton("Deny", (dialogInterface, i) -> {
                        Toast.makeText(getApplicationContext(),"permission denied", Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                    }).show();
        }
        else{
            Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onUserActionsAudioPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "permission granted successfully", Toast.LENGTH_SHORT).show();
        }
        else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
            new AlertDialog.Builder(this)
                    .setTitle("Required Permissions")
                    .setMessage("Without this permission this some features might not work")
                    .setPositiveButton("Allow ", (dialogInterface, i) -> audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO))
                    .setNegativeButton("Deny", (dialogInterface, i) -> { dialogInterface.dismiss();
                    }).show();
        }
        else{
            Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        try(Cursor cursor = getContentResolver().query(uri, projection, null, null,sortOrder)) {
            int idColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()){
                long id =  cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri1 = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);
                Uri albumartUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);
                name = name.substring(0,name.lastIndexOf("."));
                Song song = new Song(id,uri1,name,duration,size,albumId,albumartUri);
                songs.add(song);

            }
            setUpRecyclerView(songs);
        }
    }

    private void setUpRecyclerView(List<Song> songs) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);
        musicPlayerAdapter = new MusicPlayerAdapter(songs,exoPlayer);
        recyclerview.setAdapter(musicPlayerAdapter);
    }
}
package com.example.voiceplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voiceplayer.R;
import com.example.voiceplayer.data.Song;
import com.example.voiceplayer.ui.MainActivity;
import com.example.voiceplayer.ui.fragments.PlayerFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Song> songs;
    ExoPlayer player;

    public MusicPlayerAdapter(List<Song> songs, ExoPlayer player) {
        this.songs = songs;
        this.player = player;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rv_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int pos = position;
        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;
        viewHolder.rowSongName.setText(song.getName());
        viewHolder.totalSongDuration.setText(getSongDuration(song.getDuration()));

        Uri songAlbumImageUri = song.getAlbumImageUri();
        if (songAlbumImageUri != null) {
            viewHolder.SongAlbumImage.setImageURI(songAlbumImageUri);

            if (viewHolder.SongAlbumImage.getDrawable() == null) {
                viewHolder.SongAlbumImage.setImageResource(R.drawable.background_mixer);
            }
        } else {
            viewHolder.SongAlbumImage.setImageResource(R.drawable.background_mixer);
        }

        viewHolder.songRow.setOnClickListener(view -> {
            MediaItem mediaItem = getMediaItem(song);
            if (!player.isPlaying()) {
                player.setMediaItems(getMediaItems(), pos, 0);
            } else {
                player.pause();
                player.seekTo(pos, 0);
            }
            player.prepare();
            player.play();
            goToPlayerViewFragment(view.getContext());
        });
    }

    private void goToPlayerViewFragment(Context context) {
        PlayerFragment playerViewFragment = new PlayerFragment();
        String fragmentTag = playerViewFragment.getClass().getName();
        ((MainActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_container, playerViewFragment)
                .addToBackStack(fragmentTag)
                .commit();
    }

    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();
            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }

    private MediaItem getMediaItem(Song song) {
        return new MediaItem.Builder()
                .setUri(song.getUri())
                .setMediaMetadata(getMetadata(song))
                .build();
    }

    private com.google.android.exoplayer2.MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getName())
                .setArtworkUri(song.getAlbumImageUri())
                .build();
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout songRow;
        ImageView SongAlbumImage;
        TextView rowSongName, totalSongDuration;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songRow = itemView.findViewById(R.id.RelativeLayoutItems);
            SongAlbumImage = itemView.findViewById(R.id.rvItemSongImage);
            rowSongName = itemView.findViewById(R.id.rvItemSongName);
            totalSongDuration = itemView.findViewById(R.id.rvItemSongDuration);
        }
    }

    @SuppressLint("DefaultLocale")
    private String getSongDuration(int totalDuration) {
        String totalDurationText;
        int hrs = totalDuration / (1000 * 60 * 60);
        int min = (totalDuration % (1000 * 60 * 60)) / (1000 * 60);
        int secs = (((totalDuration % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        if (hrs < 1) {
            totalDurationText = String.format("%02d:%02d", min, secs);
        } else {
            totalDurationText = String.format("%1d:%02d:%02d", hrs, min, secs);
        }
        return totalDurationText;
    }
}

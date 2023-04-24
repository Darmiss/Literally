package com.cjcj55.literallynot.db;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cjcj55.literallynot.R;

import java.io.IOException;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> {
    private final Activity activity;
    private final List<AudioClip> audioClips;
    private MediaPlayer mediaPlayer;

    public AudioListAdapter(Activity activity, List<AudioClip> audioClips) {
        this.activity = activity;
        this.audioClips = audioClips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the AudioClip object at the given position
        AudioClip audioClip = audioClips.get(position);

        // Set the title and time said on the TextViews
        holder.titleTextView.setText(audioClip.getTitle());
        holder.timeSaidTextView.setText(audioClip.getTimeSaid());
        holder.locationTextView.setText(audioClip.getLocation());
        holder.textSaidTextView.setText(audioClip.getTextSaid());
        // Set the click listener on the play button
        holder.playButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                // Stop and release the media player if it's currently playing
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            try {
                // Create a new media player and set the data source
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioClip.getFilePath());

                // Set the completion listener to release the media player
                mediaPlayer.setOnCompletionListener(mp -> {
                    mediaPlayer.release();
                    mediaPlayer = null;
                });

                // Prepare the media player and start playing
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioClips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView timeSaidTextView;
        public final Button playButton;

        public final TextView locationTextView;

        public final TextView textSaidTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            timeSaidTextView = itemView.findViewById(R.id.time_said_text_view);
            playButton = itemView.findViewById(R.id.play_button);
            locationTextView = itemView.findViewById(R.id.location_text_view);
            textSaidTextView = itemView.findViewById(R.id.text_said_text_view);

        }
    }
}


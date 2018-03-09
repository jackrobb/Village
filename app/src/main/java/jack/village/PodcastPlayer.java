package jack.village;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PodcastPlayer extends AppCompatActivity{

    private TextView title;
    private TextView description;
    private  TextView currentDuration;
    private TextView totalDuration;
    private ImageButton play;
    private ProgressBar podcastProgress;

    private SeekBar podcastSeek;

    private Handler handler;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_player);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        play = findViewById(R.id.playButton);

        title = findViewById(R.id.podcastTitle);
        description = findViewById(R.id.podcastDescription);

        currentDuration = findViewById(R.id.currentDuration);
        totalDuration = findViewById(R.id.totalDuration);

        handler = new Handler();

        podcastSeek = findViewById(R.id.podcastSeek);

        podcastSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if(input){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateProgressBar();
            }
        });

        podcastProgress = findViewById(R.id.podcastProgress);

        podcastProgress.setVisibility(View.VISIBLE);

        //Create new media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Get the content from the RSSFeedAdapter
        Intent intent = getIntent();
        final String podcastUrl = intent.getExtras().getString("url");
        final String podcastTitle = intent.getExtras().getString("title");
        final String podcastDescription = intent.getExtras().getString("description");

        //Set the title and description to the information passed from the adapter
        title.setText(podcastTitle);
        description.setText(podcastDescription);

        //Instantiate new player (Auto play on page load) and set url to linkUrl, set play button to show pause icon
        new Player().execute(podcastUrl);
        play.setImageResource(R.drawable.ic_action_pause);

        //Set on click listener to the play button
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //If media player is not playing start media player and set icon to pause
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                play.setImageResource(R.drawable.ic_action_pause);
            } else {
                    //If it is playing pause, set icon to play
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        play.setImageResource(R.drawable.ic_action_play);
                    }
                }
            }
        });

    }

    public void updateProgressBar() {
        handler.postDelayed(mUpdateTimeTask, 0);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            if(mediaPlayer != null) {

                String total = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) % 60,
                        TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) % 60);

                String current = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(mediaPlayer.getCurrentPosition()) % 60,
                        TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) % 60);

                totalDuration.setText(total);
                currentDuration.setText(current);

                //Set the position of the seek bar to the current position of the media player
                podcastSeek.setProgress(mediaPlayer.getCurrentPosition());

                handler.postDelayed(this, 0);
            }
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onPause();
        finish();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean ready;

            //Try to set the media player source to a string passed to it by the on click method
            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        //Stop and reset the media player
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });

                //prepare the media player and set ready to true
                mediaPlayer.prepare();
                ready = true;

            } catch (Exception e) {
                //if it failed the try set ready to false
                ready = false;
            }

            return ready;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            podcastProgress.setVisibility(View.GONE);
            podcastSeek.setProgress(0);
            podcastSeek.setMax(mediaPlayer.getDuration());
            updateProgressBar();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.ic_action_play);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }
}

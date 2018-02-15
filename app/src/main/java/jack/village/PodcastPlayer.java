package jack.village;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class PodcastPlayer extends AppCompatActivity {

    private TextView title;
    private TextView description;
    private ImageButton play;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_player);

        play = findViewById(R.id.playButton);

        title = findViewById(R.id.podcastTitle);
        description = findViewById(R.id.podcastDescription);

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

    @Override
    protected void onPause() {
        super.onPause();

        //If the user leaves the activity and the media player is not null, reset and release the media player and set it to null
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
            mediaPlayer.start();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}

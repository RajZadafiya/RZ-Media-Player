package com.example.rzmediaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button play, fastforward, fastbackward, next, previous;
    TextView txtsongname, txtsongstart, txtsongend;
    SeekBar seekBar;
    ImageView imageView;
    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mysongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("RJ Music Player");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        play = findViewById(R.id.play);
        fastbackward = findViewById(R.id.fastbackward);
        fastforward = findViewById(R.id.fastforward);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);

        txtsongend = findViewById(R.id.txtsongend);
        txtsongstart = findViewById(R.id.txtsongstart);
        txtsongname = findViewById(R.id.txtsongstart);

        seekBar = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageview);

        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mysongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songname = intent.getStringExtra("songname");
        position = bundle.getInt("position", 0);
        txtsongname.setSelected(true);
        Uri uri = Uri.parse(mysongs.get(position).toString());
        songName = mysongs.get(position).getName();
        txtsongname.setText(songname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalduration = mediaPlayer.getDuration();
                int currentposition = 0;

                while (currentposition < totalduration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endtime = createtime(mediaPlayer.getDuration());
        txtsongend.setText(endtime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime = createtime(mediaPlayer.getCurrentPosition());
                txtsongstart.setText(currenttime);
                handler.postDelayed(this,delay);

            }
        },delay);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    play.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    play.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

                    TranslateAnimation anim = new TranslateAnimation(-25, 25, -25, 25);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.setDuration(600);
                    anim.setFillEnabled(true);
                    anim.setFillAfter(true);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(1);
                    imageView.startAnimation(anim);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mysongs.size());
                Uri uri1 = Uri.parse(mysongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mysongs.get(position).getName();
                txtsongname.setText(songName);
                mediaPlayer.start();

                startanimation(imageView, 360f);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1)<0) ? (mysongs.size() - 1) : position - 1;
                Uri uri1 = Uri.parse(mysongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mysongs.get(position).getName();
                txtsongname.setText(songName);
                mediaPlayer.start();
                startanimation(imageView, -360f);
            }
        });

        fastforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        fastbackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

        public void startanimation (View view, Float degree){
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
            objectAnimator.setDuration(1000);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(objectAnimator);
            animatorSet.start();
        }

        public String createtime ( int duration){
           String time = "";
           int min = duration/1000/60;
           int sec = duration/1000%60;

           time = time+min+":";

           if (sec<10){
               time+="0";
           }
           time += sec;
           return time;
          }
}
package com.android.music;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {
    Button btnPlay,btnNext,btnPrevious,btnFastForward,btnFastBackward;
    TextView txtSongName,txtSongStart,txtSongEnd;
    SeekBar seekMusicBar;
  //  BarVisualizer barVisualizer;
    ImageView imageView;
    String songName;

    public static final String EXTRA_NAME="song_name";

    static MediaPlayer mediaPlayer;

    int position;

    ArrayList<File> mySongs;

Thread updateSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPrevious =findViewById(R.id.btnPrevious);
        btnNext=findViewById(R.id.btnNext);
        btnPlay=findViewById(R.id.btnPlay);
        btnFastForward=findViewById(R.id.btnfastForward);
        btnFastBackward=findViewById(R.id.btnfastPrevious);

        txtSongName=findViewById(R.id.txtSong);
        txtSongStart=findViewById(R.id.txtSongStart);
        txtSongEnd=findViewById(R.id.txtSongEnd);

        seekMusicBar=findViewById(R.id.seekbar);

        imageView=findViewById(R.id.imgView);

        if(mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        mySongs=(ArrayList)bundle.getParcelableArrayList("songs");
        String sName=intent.getStringExtra("songname");
        position=bundle.getInt("pos",0);
        txtSongName.setSelected(true);
        Uri uri=Uri.parse(mySongs.get(position).toString());
        songName=mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateSeekbar =new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentPosition=0;
                if(currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
               // super.run();
            }
        };
        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200),PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        String endTime=createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler=new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime=createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                    TranslateAnimation moveAnimate=new TranslateAnimation(-25,25,-25,25);
                    moveAnimate.setInterpolator(new AccelerateInterpolator());
                    moveAnimate.setDuration(600);
                    moveAnimate.setFillAfter(true);
                    moveAnimate.setFillEnabled(true);
                    moveAnimate.setRepeatCount(1);
                    moveAnimate.setRepeatMode(Animation.REVERSE);
                    imageView.setAnimation(moveAnimate);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)%mySongs.size());
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                songName=mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView,360f);

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)%mySongs.size());
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                songName=mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();

                startAnimation(imageView,-360f);
            }
        });
        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnFastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }
    public void startAnimation(View v,float degree){
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(imageView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet= new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }
    public String createTime(int duration){
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;

        time=time+min+":";
        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }
}
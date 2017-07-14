package com.huahua.helloaudio;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by zhang.kh on 2017/7/12.
 */

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, View.OnClickListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {
    private ImageView btnPlay, btnPrev, btnNext;
    private SeekBar seekBar;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private MediaPlayer mp;
    private AudioUtils audioUtils;
    private Handler mHandler = new Handler();
    private Config config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All player buttons
        btnPlay = (ImageView) findViewById(R.id.btn_play);
        btnPrev = (ImageView) findViewById(R.id.btn_prev);
        btnNext = (ImageView) findViewById(R.id.btn_next);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.tv_play_time);
        songTotalDurationLabel = (TextView) findViewById(R.id.tv_duration);

        //Mediaplayer
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnPreparedListener(this);


        audioUtils = new AudioUtils();
        config = new Config();
        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(this);// Important
        mp.setOnCompletionListener(this); // Important
    }


    boolean isPlaying = false;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                if (isPlaying) {
                    btnPlay.setImageResource(R.drawable.play_btn_play_selector);
                    mp.pause();
                    isPlaying = false;

                } else {
                    btnPlay.setImageResource(R.drawable.play_btn_pause_selector);

                    playSong(config.url);
                    Log.i("2222222","222222222");
                    isPlaying = true;
                }
        }
    }

    public void playSong(String url) {
        try {

            // Play song
            mp.reset();
            mp.setDataSource(url);
            mp.prepare();
            mp.start();
            // Displaying Song title歌曲标题
//            String songTitle = songsList.get(songIndex).get("songTitle");
//            songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
            //btnPlay.setImageResource(R.drawable.play_btn_play_selector);

            // set Progress bar values
            seekBar.setProgress(0);
            seekBar.setMax(100);

            // Updating progress bar更新进度条
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mp!=null){

                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time音频总时长
                songTotalDurationLabel.setText("" + audioUtils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing当前播放的时长
                songCurrentDurationLabel.setText("" + audioUtils.milliSecondsToTimer(currentDuration));

                // Updating progress bar更新进度条
                int progress = (int) (audioUtils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                seekBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            }
        }
    };

    // 当拖动条发生变化时调用该方法
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    //当用户滑动时调用调用方法
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    // 鼠标松开时调用该方法
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = audioUtils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();

    }

    //播放完后调用
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mp.stop();
        btnPlay.setImageResource(R.drawable.play_btn_play_selector);
        isPlaying=false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.pause();
        isPlaying=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPlay.setImageResource(R.drawable.play_btn_play_selector);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();
        mp=null;
    }

    //此回调方法允许应用程序追踪流播放的缓冲状态
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferingProgress) {
        seekBar.setSecondaryProgress(bufferingProgress);
        int currentProgress = seekBar.getMax() * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
        Log.e(currentProgress + "% play", bufferingProgress + "% buffer");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}

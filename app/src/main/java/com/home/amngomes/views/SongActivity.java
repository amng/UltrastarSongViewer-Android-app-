package com.home.amngomes.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.home.amngomes.controller.Constants;
import com.home.amngomes.ultrastarsongviewer.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongActivity extends AppCompatActivity {

    public static final int SEEKTIME = 5000;
    public static Bitmap songImageBitmap = null;
    public static Palette palette = null;
    private float fab_init_x = 0f, fab_init_y = 0f;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Thread updateProgressThread;
    private boolean updating = false;
    private boolean running = true;

    private int statusBarColor;
    private int iconStatusColor;

    private int layoutColor;
    private int artistTextColor;
    private int songTextColor;
    private int progressColor;


    @BindView(R.id.song_image)
    ImageView song_image;

    @BindView(R.id.song_artist)
    public TextView artist;

    @BindView(R.id.song_name)
    public TextView song;

    @BindView(R.id.song_info)
    FrameLayout song_info_layout;

    @BindView(R.id.fab_play_song)
    FloatingActionButton fab_play_song;

    @BindView(R.id.song_progress)
    SeekBar progressbar;

    @BindView(R.id.layout_music_controls)
    LinearLayout layout_music_controls;

    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @BindView(R.id.btn_forward)
    ImageButton btn_forward;

    @BindView(R.id.btn_pause)
    ImageButton btn_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        ButterKnife.bind(this);

        ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setDisplayShowTitleEnabled(false);

            ImageView up = (ImageView) findViewById(android.R.id.home);
            if(up!=null)
                up.getDrawable().setColorFilter (Color.CYAN, PorterDuff.Mode.SRC_ATOP);

        }
        initColors();
        progressbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Bundle b = getIntent().getExtras();
        artist.setText(b.getString(Constants.Bundle.SONG_ARTIST));
        song.setText(b.getString(Constants.Bundle.SONG_NAME));
        String song_path = b.getString(Constants.Bundle.SONG_PATH);
        //Uri myUri = Uri.fromFile(new File(song_path));

        song_image.setImageBitmap(songImageBitmap);
        fab_play_song.setEnabled(false);
        try {
            mediaPlayer.setDataSource(song_path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp){
                    fab_play_song.setEnabled(true);
                    progressbar.setMax(mp.getDuration());
                    progressbar.setProgress(0);
                }
            });
        } catch (IOException e) {
            fab_play_song.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusBarColor);
        }

        ScaleAnimation animation = new ScaleAnimation(
                0, 1f, 0, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);
        animation.setStartOffset(200);
        animation.setFillBefore(true);

        song_info_layout.setBackgroundColor(layoutColor);
        song.setTextColor(songTextColor);
        artist.setTextColor(artistTextColor);
        layout_music_controls.setBackgroundColor(statusBarColor);
        btn_forward.setColorFilter(iconStatusColor, PorterDuff.Mode.SRC_ATOP);
        btn_pause.setColorFilter(iconStatusColor, PorterDuff.Mode.SRC_ATOP);
        btn_back.setColorFilter(iconStatusColor, PorterDuff.Mode.SRC_ATOP);
        progressbar.getProgressDrawable().setColorFilter(iconStatusColor, PorterDuff.Mode.SRC_ATOP);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                statusBarColor,
                statusBarColor,
                statusBarColor,
                statusBarColor
        };

        ColorStateList myList = new ColorStateList(states, colors);
        fab_play_song.setBackgroundTintList(myList);


        initThread();
        updateProgressThread.start();

        fab_play_song.startAnimation(animation);
    }

    private void initThread() {
        updateProgressThread = new Thread(
                new Runnable(){
                    @Override
                    public void run() {
                        while(running) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(updating)
                                SongActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mediaPlayer != null)
                                            progressbar.setProgress(mediaPlayer.getCurrentPosition());
                                    }
                                });
                        }
                    }
                });
    }


    private void initColors() {
        statusBarColor = getResources().getColor(R.color.md_deep_orange_900);
        iconStatusColor = getResources().getColor(R.color.md_white);
        layoutColor = getResources().getColor(R.color.md_orange_a200);
        artistTextColor = getResources().getColor(R.color.md_grey_300);
        songTextColor = getResources().getColor(R.color.md_white);
        progressColor = getResources().getColor(R.color.md_white);
        Palette.Swatch vibrant = null;
        Palette.Swatch muted = null;
        if (palette != null) {
            if (palette.getDarkVibrantSwatch() != null) {
                vibrant = palette.getDarkVibrantSwatch();
            } else if (palette.getVibrantSwatch() != null) {
                vibrant = palette.getVibrantSwatch();
            } else if (palette.getLightVibrantSwatch() != null) {
                vibrant = palette.getLightVibrantSwatch();
            }
            if(vibrant != null) {
                statusBarColor = vibrant.getRgb();
                iconStatusColor = vibrant.getTitleTextColor();
                progressColor = vibrant.getBodyTextColor();
            }

            if(palette.getDarkMutedSwatch() != null){
                muted = palette.getDarkMutedSwatch();
            }else if(palette.getMutedSwatch() != null){
                muted = palette.getMutedSwatch();
            }else if(palette.getLightMutedSwatch() != null){
                muted = palette.getLightMutedSwatch();
            }
            if(muted != null) {
                layoutColor = muted.getRgb();
                artistTextColor = muted.getTitleTextColor();
                songTextColor = muted.getBodyTextColor();
            }
        }
    }


    @OnClick(R.id.fab_play_song)
    public void showMediaControls(){
        updating = true;
        mediaPlayer.start();
        showMusicControls();
    }

    @OnClick(R.id.btn_pause)
    public void hideMediaControls(){
        updating = false;
        mediaPlayer.pause();
        hideMusicControls();
    }

    @OnClick(R.id.btn_forward)
    public void fastFowardMusic(){
        if(mediaPlayer.getCurrentPosition() + SEEKTIME < mediaPlayer.getDuration())
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+SEEKTIME);
    }

    @OnClick(R.id.btn_back)
    public void rewingMusic(){
        if(mediaPlayer.getCurrentPosition() - SEEKTIME > 0)
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-SEEKTIME);
        else
            mediaPlayer.seekTo(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        switch (id) {
            case android.R.id.home:
                exitAnimation();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        running = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        exitAnimation();
    }

    private void exitAnimation(){
        ScaleAnimation animation = new ScaleAnimation(
                1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(150);
        animation.setFillBefore(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fab_play_song.setVisibility(View.INVISIBLE);
                SongActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab_play_song.startAnimation(animation);
    }


    public void showMusicControls(){
        // get the center for the clipping circle
        fab_init_x = fab_play_song.getX();
        fab_init_y = fab_play_song.getY();

        final int cx = (layout_music_controls.getLeft() + layout_music_controls.getRight()) / 2;
        final int cy = (layout_music_controls.getTop() + layout_music_controls.getBottom());

        // get the final radius for the clipping circle
        final int finalRadius = Math.max(layout_music_controls.getWidth(), layout_music_controls.getHeight());

        //ending x co-ordinates
        float x1 = fab_play_song.getX();
        float y1 = fab_play_song.getY();

        float x3 = song_info_layout.getX() + song_info_layout.getWidth()/2;
        float y3 = song_info_layout.getY() + song_info_layout.getHeight()/2;
        //ending x co-ordinates

        final Path path = new Path();
        path.moveTo(x1, y1);
        path.cubicTo(x1, y1, x1, y3, x3, y3);
        ObjectAnimator anim = ObjectAnimator.ofFloat(fab_play_song, View.X, View.Y, path);
        anim.start();
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // create the animator for this view (the start radius is zero)
                fab_play_song.setVisibility(View.GONE);
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(layout_music_controls, cx, cy, 0, finalRadius);
                // make the view visible and start the animation
                layout_music_controls.setVisibility(View.VISIBLE);
                anim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void hideMusicControls(){
        // get the center for the clipping circle
        int cx = (layout_music_controls.getLeft() + layout_music_controls.getRight()) / 2;
        int cy = (layout_music_controls.getTop() + layout_music_controls.getBottom()) / 2;

        // get the initial radius for the clipping circle
        int initialRadius = layout_music_controls.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(layout_music_controls, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layout_music_controls.setVisibility(View.INVISIBLE);

                fab_play_song.setVisibility(View.VISIBLE);
                //ending x co-ordinates
                float x1 = fab_init_x;
                float y1 = fab_init_y;

                float x3 = song_info_layout.getX() + song_info_layout.getWidth()/2;
                float y3 = song_info_layout.getY() + song_info_layout.getHeight()/2;
                //ending x co-ordinates

                final Path path = new Path();
                path.moveTo(x3, y3);
                final float x2 = (x1 + x3) / 2;
                final float y2 = y1;
                path.cubicTo(x3, y3, x1, y3, x1, y1);
                ObjectAnimator anim = ObjectAnimator.ofFloat(fab_play_song, View.X, View.Y, path);
                anim.start();
            }
        });
        // start the animation
        anim.start();
    }
}

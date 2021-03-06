package com.example.user.smartfitnesstrainer.Main.DetailVideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.user.smartfitnesstrainer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.tomer.fadingtextview.FadingTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ExerciseActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    private RecyclerView rv;
    private ExerciseListAdapter ela;
    private ArrayList <String> temp = new ArrayList<>();
    private VideoView vf;
    private boolean isVideoCreate = false;
    private Analyticzer analyticzer =new Analyticzer();
    private FadingTextView ftv;
    private ArrayList <VideoModel> vm = new ArrayList<>();
    private ImageButton pause;
    private DeviceAlert devicealert;
    private TextView mTextField;
    private int isTutorMode = 0;
    private ArrayList<ExerciseModel> exerciseModelArrayList = new ArrayList<>();
    int currentExercise = 0;
    private ProgressBar pb;
    private int stageScore =0;
    private TextView currentScore;
    private TextView baseScore;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView simpleExoPlayerView;
    private ImageButton play;
    private ArrayList<Integer> totalRound =new ArrayList<>();
    private RelativeLayout rl;
    private RelativeLayout rl0;
    private LinearLayout scoreBoard;
    private Button skipTutor;
    private boolean onFinishRepeat =false;
    private Bundle sis;
    private TimerTask doAsynchronousTask;
    private MediaPlayer mp;
    private TextView angle;
    private double currentreading=0.0;
    public class MyBroadcaseReceiver1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
             int sender = intent.getIntExtra("sender_name",0);
            Log.d("shb",String.valueOf(sender));

            //tommy change 78
            if(sender==78)sender=0;
            angle.setText(String.valueOf(sender));
            currentreading+=sender;



        }

    }
    private MyBroadcaseReceiver1 m_MyReceiver1;
    @Override
    protected void onStart() {
        super.onStart();
        if (!isVideoCreate) {
            isVideoCreate =true;
            Log.d("playernum","lc");
            createPlaylist();
            createExerciseModellist();
            firstClip();
            prepareExoPlayerFromFileUri(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.video));
        }

    }
    private MediaSource buildMediaSource(int uri){
        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(uri));
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(getApplicationContext());
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return rawResourceDataSource;
            }
        };
        MediaSource audioSource =
                new ExtractorMediaSource(rawResourceDataSource.getUri(),factory, new DefaultExtractorsFactory(), null, null);
        return audioSource;
    }
    private void roundFinished(){
        Log.d("stageScore",String.valueOf(stageScore));

            onFinishRepeat =true;
            totalRound.add(stageScore);
            stageScore = 0;
            currentScore.setText(String.valueOf(stageScore));

    }
    private void prepareExoPlayerFromFileUri(Uri uri) {
        if (player!=null)
        {
            Log.d("playerx","gr");
            return;
        }
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        MediaSource[] mediaSources = new MediaSource[vm.size()];
        for (int i=0;i<vm.size();i++) {
           mediaSources[i]= buildMediaSource(vm.get(i).videoUrl);
        }

        simpleExoPlayerView.setPlayer(player);
        ConcatenatingMediaSource cms = new ConcatenatingMediaSource(mediaSources);
        if(player!=null && mediaSources!=null){

            player.addListener(new SimpleExoPlayer.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if(playbackState == Player.STATE_ENDED)
                    {
                        if(!onFinishRepeat) {
                            roundFinished();
                            Log.d("plex", String.valueOf(totalRound.size()));

                            for (int x : totalRound) {
                                Log.d("roundscore", String.valueOf(x));
                            }
                            finish();
                        }
                    }

                    Log.d("playState",String.valueOf(player.getCurrentPosition()));
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    super.onPositionDiscontinuity(reason);
                    int currentPosition = 0;
                    currentPosition = player.getCurrentWindowIndex();
                    Log.d("playState",String.valueOf(currentPosition));
                    if(isTutorMode==1)
                    {
                        //Finish one round
                        roundFinished();
                        Log.d("try","sessionmode");
                    }
                }


                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    player.setPlayWhenReady(false);
                    animationAfterExercise();
                    super.onTracksChanged(trackGroups, trackSelections);


                }
            });
            player.setPlayWhenReady(false);
            //player.prepare(cms);
            simpleExoPlayerView.getPlayer().prepare(cms);
            simpleExoPlayerView.setUseController(false);
        }


    }
    private void unregDevice(){
        try {
            unregisterReceiver(m_MyReceiver1);
        }
        catch (Exception e )
        {

        }
    }
    private void noSkip()
    {
        if(skipTutor.getVisibility()==View.VISIBLE)
            skipTutor.setVisibility(View.GONE);
    }
    private void skipAvaliable()
    {

        if(skipTutor.getVisibility()==View.GONE)
        skipTutor.setVisibility(View.VISIBLE);
      //  else if(skipTutor.getVisibility()==View.VISIBLE)
        //skipTutor.setVisibility(View.GONE);
    }
    //choser
    private void animationAfterExercise(){
        Log.d("animationaftere",String.valueOf(isTutorMode));
        if (isTutorMode==0)
        {

            unregDevice();

            introMode();

        }
        else if (isTutorMode == 1)
        {

            pauseCounter();
            scoreBoardAppear();
            skipAvaliable();
            unregisterReceiver(m_MyReceiver1);
            tutorMode();
        }
        else
        {
            noSkip();
            unregDevice();
            deviceCheck();
            scoreBoardAppear();
        }
    }
    private void deviceCheck(){
        devicealert = DeviceAlert.newInstance(currentExerciseInstruction(currentExercise));

       // devicealert.setArguments();
        devicealert.show(getFragmentManager(),"Edit");
    }
    private void introSound(){

        mp = MediaPlayer.create(getApplicationContext(), R.raw.welcometocadiochallege);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                challengeSound(R.raw.challegeonevsit);
            }
        });
        mp.start();

    }
    private void challengeSound(int challenge){
        mp = MediaPlayer.create(getApplicationContext(),challenge);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        mp.start();
    }

    private int currentExerciseSound(int currentExercise){
        switch (currentExercise)
        {
            case 0:
                return R.raw.challegeonevsit;
            case 1:
                return R.raw.challegetwoplank;
                default:
                    return R.raw.chal3tstable;
        }
    }
    private int currentExerciseInstruction(int currentExercise){
        switch (currentExercise)
        {
            case 0:
                return R.raw.vsittut;
            case 1:
                return R.raw.tplank;
            default:
                return R.raw.tstable;
        }
    }
    private void congrazSound(){
        mp = MediaPlayer.create(getApplicationContext(), R.raw.welldone);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // Actions to do after 10 seconds
                        try {
                            challengeSound(currentExerciseSound(currentExercise));
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }, 1200);

            }
        });
        mp.start();
    }
    //intro mode
    private void introMode(){
        //play intro sound
        introSound();
        new CountDownTimer(5000,1000) {

            public void onTick(long millisUntilFinished) {

                //challengeSound();
                //here you can have your logic to set text to edittext
            }
            public void onFinish() {
                rl0.setVisibility(View.GONE);
                player.setPlayWhenReady(true);
                isTutorMode = 2;
                skipAvaliable();
            }
        }.start();
    }
    private void scoreBoardAppear()
    {
        if(scoreBoard.getVisibility()==View.GONE)
        scoreBoard.setVisibility(View.VISIBLE);
        else if(scoreBoard.getVisibility()==View.VISIBLE)
            scoreBoard.setVisibility(View.GONE);
    }
    //tutor mode
    private void tutorMode(){

        ExerciseModel currentem = exerciseModelArrayList.get(currentExercise);

        noSkip();
        congrazSound();
        String[] tmps = {"Well done!","Challenge "+(currentExercise+1)+"\n"+currentem.name};
        ftv.restart();
        ftv.setTexts(tmps);
        rl0.setVisibility(View.VISIBLE);
        new CountDownTimer(6000,1000) {

            public void onTick(long millisUntilFinished) {
               // congradulationAudio
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                rl0.setVisibility(View.GONE);
                skipAvaliable();

                player.setPlayWhenReady(true);
                isTutorMode = 2;
            }

        }.start();

    }
    //player mode
    private void exerciseStarts(){

        pb.setVisibility(View.VISIBLE);

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                    if (rl.getVisibility() == View.GONE)
                    rl.setVisibility(View.VISIBLE);
                    mTextField.setText(String.valueOf(millisUntilFinished / 1000+1));

                //here you can have your logic to set text to edittext
            }

            public void onFinish() {

                rl.setVisibility(View.GONE);
                pb.setVisibility(View.INVISIBLE);
                player.setPlayWhenReady(true);
                currentExercise++;
                callAsynchronousTask();
                isTutorMode = 1;
            }

        }.start();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        player.clearVideoSurface();
        player.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    protected void createPlaylist(){
        VideoModel vid = new VideoModel(R.raw.svsit);
        vm.add(vid);
        VideoModel vid2 = new VideoModel(R.raw.vvsit);
        vm.add(vid2);
        VideoModel vid3 = new VideoModel(R.raw.tutplunk);
        vm.add(vid3);
        VideoModel vid4 = new VideoModel(R.raw.traplunk);
        vm.add(vid4);
        VideoModel vid5 = new VideoModel(R.raw.tuttstable);
        vm.add(vid5);
        VideoModel vid6 = new VideoModel(R.raw.realtstable);
        vm.add(vid6);
    }
    protected void createExerciseModellist(){
        ExerciseModel em = new ExerciseModel("V-Sit",9,11,15,30,2);
        exerciseModelArrayList.add(em);
        ExerciseModel em2 = new ExerciseModel("Plank",10,12,20,30,2);
        exerciseModelArrayList.add(em2);
        ExerciseModel em3 = new ExerciseModel("T-Stabilization",9,11,15,30,2);
        exerciseModelArrayList.add(em3);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    protected void firstClip(){
        ExerciseModel currentem = exerciseModelArrayList.get(currentExercise);
        String[] tmps = {"Welcome to Cardio challenge!","Challenge "+(currentExercise+1)+"\n"+currentem.name};
        ftv.setTexts(tmps);
    }
    private void pausePlayer(){
        player.setPlayWhenReady(false);
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.showController();
        simpleExoPlayerView.setControllerHideOnTouch(false);
        pause.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState!=null)
        {
            sis=savedInstanceState;
        }
        setContentView(R.layout.activity_exercise);
        pause = findViewById(R.id.exo_pause);

        play = findViewById(R.id.exo_play);
        skipTutor = findViewById(R.id.skiptut);
        rl = findViewById(R.id.timerrl);
        simpleExoPlayerView = findViewById(R.id.audio_view);
        ftv = findViewById(R.id.fadingTransition);
        mTextField = findViewById(R.id.countdown);
        pb = findViewById(R.id.video_progressbar);
        rl0 = findViewById(R.id.timerrl0);
        angle = findViewById(R.id.angle);
        scoreBoard = findViewById(R.id.scoreboard);
        currentScore = findViewById(R.id.score);
    //    baseScore = findViewById(R.id.basescore);
        rl.setVisibility(View.GONE);
        Log.d("pkxt","owow");
        m_MyReceiver1 = new MyBroadcaseReceiver1();


        ela = new ExerciseListAdapter(this,temp);
        play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                player.setPlayWhenReady(true);
                simpleExoPlayerView.setUseController(false);
                simpleExoPlayerView.hideController();
                simpleExoPlayerView.setControllerHideOnTouch(false);
                pause.setVisibility(View.VISIBLE);
            }
        });

        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pausePlayer();
            }
        });

        // initializePlayer();
       /* MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(vf);
        vf.setMediaController(mediaController);
*/
      /*  rv.setAdapter(ela);
        LinearLayoutManager llmm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llmm);
        */
        /*Uri uri = Uri.parse("android.resource://"+getApplicationContext().getPackageName()+"/"+R.raw.video);
        vf.setVideoURI(uri);
        */
        /*vf.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!bVideoIsBeingTouched) {
                    bVideoIsBeingTouched = true;
                    if (videoview.isPlaying()) {
                        onPause(videoview);
                    } else {
                        Log.d("vid",String.valueOf(videoview.isPlaying()));
                        onResume(videoview);
                    }

                    bVideoIsBeingTouched = false;

                }
                return true;
            }
        });vf.start();*/

    }

    public void onClickSkip(View v)
    {
        player.seekTo(player.getDuration());
    }

    private void scoreAdder()
    {

        stageScore++;
        currentScore.setText(String.valueOf(stageScore));
        mp = MediaPlayer.create(getApplicationContext(), R.raw.ding);
        mp.start();
    }
    public void pauseCounter(){
        if(doAsynchronousTask!=null)
            doAsynchronousTask.cancel();
    }
    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.d("readffromace",String.valueOf(currentExercise-1)+" "+String.valueOf(currentreading/40));
                            if(analyticzer.analyze(currentExercise-1,currentreading/40)){
                               scoreAdder();
                            }
                         currentreading=0;

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 2000); //execute in every 50000 ms
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
            mp.release();
        if(doAsynchronousTask!=null)
        doAsynchronousTask.cancel();
        unregDevice();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

        exerciseStarts();
        IntentFilter itFilter = new IntentFilter("tw.android.MY_BROADCAST1");
        registerReceiver(m_MyReceiver1, itFilter);
    }
}

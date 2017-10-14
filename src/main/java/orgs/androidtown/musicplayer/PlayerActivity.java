package orgs.androidtown.musicplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import orgs.androidtown.musicplayer.model.Music;

public class PlayerActivity extends AppCompatActivity
        implements View.OnClickListener{

    Music music;
    MediaPlayer player = null;
    int current = -1;
    private ViewPager viewPager;
    private RelativeLayout controller;
    private SeekBar seekBar;
    private TextView textCurrentTime;
    private TextView textDuration;
    private ImageButton btnPlay;
    private ImageButton btnFf;
    private ImageButton btnRew;
    private ImageButton btnNext;
    private ImageButton btnPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        load();
        initView();
        initViewPager();
        initControl();
        start();
    }

    private void load() {
        music = Music.getInstance();
        Intent intent = getIntent(); // 메인액티비티에서 전달한 포지션값을 받아서
        current = intent.getIntExtra(Const.KEY_POSITION, -1); //current 변수에 할당
    }

    private void initControl() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // 미디어 볼륨을 셋팅해줌
        setPlayer();
    }

    SeekBarThread seekBarThread = null;
    private void setPlayer(){
        Music.Item item = music.data.get(current); // music data 클래스안에 있는 아이템 클래스의 포지션값을 받아서 할당한다.
        Uri musicUri = item.musicUri; //아이템의 musicUri를 할당한다.
        if(seekBarThread != null) //쓰레드가 없으면
            seekBarThread.setStop(); // 중지메소드를 설정
        if(player != null) { //마찬가지로 뮤직플레이어가 없으면
            player.release(); // 해제시켜줌, 서브쓰레드에서 계속 해제시켜주고 있고
            player = null; // 새로운 것을 null로 할당하여, 다음에 만들어지는 것들에 문제가 없게 한다.
        }

        player = MediaPlayer.create(this, musicUri); // 미디어 플레이어에 컨텍스트와 아이템에 할당된 Uri를 할당한다.
        player.setLooping(false);// 연속재생을 멈춘다. 세팅하는 것이기 때문에....

        // 화면세팅
        String duration = miliToSec(player.getDuration()); // 16754265 => 03:15,  getDuration은 현재시간을 받아오는 메소드
        textDuration.setText(duration);
        textCurrentTime.setText("00:00");

        seekBar.setMax(player.getDuration());

        seekBarThread = new SeekBarThread(handler);
        seekBarThread.start();
    }

    private String miliToSec(int mili){
        int sec = mili / 1000;
        int min = sec / 60;
        sec = sec % 60;

        return String.format("%02d", min) + ":" + String.format("%02d",sec); // %02d의 의미: % 명령의 시작, 0 채워질문자, 2 총자리수 d 십진정수
    }


    private void initView() {
        setContentView(R.layout.activity_player);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        controller = (RelativeLayout) findViewById(R.id.controller);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textCurrentTime = (TextView) findViewById(R.id.textCurrentTime);
        textDuration = (TextView) findViewById(R.id.textDuration);

        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnFf = (ImageButton) findViewById(R.id.btnFf);
        btnRew = (ImageButton) findViewById(R.id.btnRew);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);

        btnPlay.setOnClickListener(this);
        btnFf.setOnClickListener(this);
        btnRew.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
    }

    private void initViewPager() { // 뷰페이저에 어댑터 설정한다.
        PlayerPagerAdapter adapter = new PlayerPagerAdapter(this, music.data); // 어댑터 객체를 생성하면, PlayerPagerAdapter에 생성자로 만든 것에서 데이터가 세팅됨.
        viewPager.setAdapter(adapter); // 어댑터 세팅
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current = position;
                setPlayer();
                if(playButtonStat == Const.STAT_PLAY){
                    start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(current > -1) // current가 포지션값을 할당받아 변하면,
            viewPager.setCurrentItem(current); // viewPager는 현재 current에 맞는 아이템으로 세팅
    }

    private void start() {
        playButtonStat = Const.STAT_PLAY;
        player.start();
        btnPlay.setImageResource(android.R.drawable.ic_media_pause); // 스타트 메소드가 시작되면누르면 멈춤으로 바뀜
    }

    private void pause(){
        playButtonStat = Const.STAT_PAUSE;
        player.pause();
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    protected void onDestroy() {
        if(seekBarThread != null) //쓰레드처리!
            seekBarThread.setStop();// setStop메서드가 호출되면, 플래그값은 false로 바뀌어 쓰레드가 중지된다.

        if (player != null)
            player.release();

        super.onDestroy();
    }


    int playButtonStat = Const.STAT_PLAY;

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnPlay:
                if(playButtonStat == Const.STAT_PLAY)
                    pause();
                else
                    start();
                break;
            case R.id.btnFf:
                break;
            case R.id.btnRew:
                break;
            case R.id.btnNext:
                break;
            case R.id.btnPrev:
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case Const.WHAT_SET:
                    if(player != null) {
                        int cp = player.getCurrentPosition(); // 미디어플레이어 현재 포지션값
                        seekBar.setProgress(cp);// 미디어 플레이어 현재 포지션값을 seekbar.setProgress 메소드에 담는다.
                        textCurrentTime.setText(miliToSec(cp)); // 그런후에 텍스트뷰에 그 값들을 할당함.
                    }
                    break;
            }
        }
    };
}

class SeekBarThread extends Thread {
    private boolean runFlag = true;
    private Handler handler;
    public SeekBarThread(Handler handler){
        this.handler = handler;
    }
    public void setStop(){
        runFlag = false;
    }
    public void run(){
        while(runFlag) {
            handler.sendEmptyMessage(Const.WHAT_SET);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


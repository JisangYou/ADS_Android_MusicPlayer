package orgs.androidtown.musicplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import orgs.androidtown.musicplayer.model.Music;

public class MainActivity extends BaseActivity
        implements MusicFragment.OnListFragmentInteractionListener{
    Music music = null;
    private ViewPager viewPager;
    private TabLayout tablayout;

    @Override
    public void init(){
        // 볼륨 조절 버튼으로 미디어 음량만 조절하기 위한 설정
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);
        load();
        initView();
        initViewPager();
        initTabs();
        initListener();
    }

    private void load(){ // model에 있는 music데이터에서 객체를 불러옴.
        music = Music.getInstance();
        music.load(this); // 뮤직클래스에 있는 데이터를 가지고 오는 과정
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
    }
    private void initViewPager(){ // 멀티턴
        List<Fragment> list = new ArrayList<>();
        MusicFragment fragTitle = MusicFragment.newInstance(1);
        MusicFragment fragArtist = MusicFragment.newInstance(1);
        MusicFragment fragAlbum = MusicFragment.newInstance(1);
        MusicFragment fragGenre = MusicFragment.newInstance(1);
        list.add(fragTitle);
        list.add(fragArtist);
        list.add(fragAlbum);
        list.add(fragGenre);
        ListPagerAdapter adapter
                = new ListPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
    }
    private void initTabs(){
        tablayout.addTab(tablayout.newTab().setText(getString(R.string.tab_title)));
        tablayout.addTab(tablayout.newTab().setText(getString(R.string.tab_artist)));
        tablayout.addTab(tablayout.newTab().setText(getString(R.string.tab_album)));
        tablayout.addTab(tablayout.newTab().setText(getString(R.string.tab_genre)));
    }
    private void initListener(){
        // 탭레이아웃과 뷰페이저를 연결
        tablayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager)
        );
        //viewPager의 변경사항을 탭레이아웃에 전달
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tablayout)
        );
    }

    @Override
    public List<Music.Item> getList() {
        return music.data;
    }

    @Override
    public void openPlayer(int position) {
        Intent intent = new Intent(this, PlayerActivity.class); //플레이어 액티비티로 인텐트 전달
        intent.putExtra(Const.KEY_POSITION, position); //포지션값 전달
        startActivity(intent);
    }
}
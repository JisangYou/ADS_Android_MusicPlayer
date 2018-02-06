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
        implements MusicFragment.OnListFragmentInteractionListener{ // 퍼미션 체크를 위한 BaseActivity 상속 및 프래그먼트와 액티비티의 통신을 위한 리스너 세팅.
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

    private void load(){ // model에 있는 music데이터에서 싱글턴으로 선언된 객체를 가지고오는 메소드.
        music = Music.getInstance();
        music.load(this); // 안드로이드 기기에 있는 데이터를 가지고 오는 과정
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

    /**
     * 하기의 메소드들은 액티비티와 프래그먼트간에 통신을 위해서 구현한 것
     */

    @Override
    public List<Music.Item> getList() {
        return music.data;
    }

    /**
     *  해당 메소드 name을 클릭하면, musicFragment에 속해있는 어댑터 클래스로 이동한다. 해당 어댑터 클래스의 정의해놓은 아이템을 클릭했을 때 이벤트로 아래 메소드가 실행
     *  MainActivity에 ViewPager에 연결되어 있는 프래그먼트(프래그먼트 내에 세팅해놓은 recyclerView item)와 통신하는 로직
     *
     */


    @Override
    public void openPlayer(int position) {
        Intent intent = new Intent(this, PlayerActivity.class); //프래그먼트에 세팅해놓은 recyclerView의 아이템을 클릭했을 떄, 플레이어 액티비티로 이동
        intent.putExtra(Const.KEY_POSITION, position); //이동할 때 함께 포지션값 이동.
        startActivity(intent);
    }
}
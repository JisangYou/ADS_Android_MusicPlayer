# ADS04 Android

## 수업 내용

- 안드로이드 로컬에 있는 음원들을 활용한 뮤직플레이어를 만드는 학습

## Code Review

### MainActivity
```Java
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
```

### MusicFragment

```Java
public class MusicFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    // 메인액티비티와 통신하는 인터페이스
    private OnListFragmentInteractionListener mListener;

    public MusicFragment() {
    }

    public static MusicFragment newInstance(int columnCount) { // 메인에서 프래그먼트 생성시
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyMusicRecyclerViewAdapter(mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // 메인 액티비티와 통신하는 인터페이스
    // 액티비티에서 implement 하지 않으면 앱이 강제로 종료된다.
    public interface OnListFragmentInteractionListener {
        // 목록 데이터를 가져오는 함수
        List<Music.Item> getList();
        // 메인액티비티에서 새로운 페이지로 이동하는 함수
        void openPlayer(int position);
    }
}
```

### MyMusicRecyclerViewAdapter

```java
public class MyMusicRecyclerViewAdapter extends RecyclerView.Adapter<MyMusicRecyclerViewAdapter.ViewHolder> {

    private final List<Music.Item> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyMusicRecyclerViewAdapter(OnListFragmentInteractionListener listener) { //리스너로 선언한 곳에서 정의한 데이터를 인자로 세팅할 수 있다.
        mListener = listener;
        mValues = mListener.getList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).title);
        holder.position = position;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.openPlayer(holder.position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public int position;
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Music.Item mItem;

        public ViewHolder(View view) { // 온클릭 리스너를 뷰홀더에 넣는 것과 차이가 있는것인가?
            super(view);
            mView = view; //
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
```

### PlayerActivity

```java
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
    private void setPlayer(){ // 음원 세팅 메소드.
        Music.Item item = music.data.get(current); // music data 클래스안에 있는 아이템 클래스의 포지션값을 받아서 할당한다.
        Uri musicUri = item.musicUri; // 이곳에서 사용할 uri는 아이템에 정의해놓은 MusicUri
        if(seekBarThread != null) //쓰레드가 없으면
            seekBarThread.setStop(); // 중지(flag값으로, 쓰레드를 컨트롤)
        if(player != null) { //마찬가지로 뮤직플레이어가 없으면
            player.release();
            player = null;
        }

        player = MediaPlayer.create(this, musicUri); // 미디어 플레이어에 컨텍스트와 아이템에 할당된 Uri를 할당한다.
        player.setLooping(false);// 연속재생을 멈춘다. 세팅하는 것이기 때문에....

        // 화면세팅
        String duration = miliToSec(player.getDuration()); // 16754265 => 03:15,  getDuration은 현재시간을 받아오는 안드로이드 자체 메소드
        textDuration.setText(duration); // duration은 총시간
        textCurrentTime.setText("00:00");

        seekBar.setMax(player.getDuration()); //setMax는 seekBar 자체 메소드로써, 가지고 온 음원의 총시간을 세팅해준다.

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
        setContentView(R.layout.activity_player); // TODO 의미?
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
        PlayerPagerAdapter adapter = new PlayerPagerAdapter(this, music.data); // 어댑터 객체를 생성후, context자원과 데이터를 넣으면, 뷰페이저 세팅
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
            viewPager.setCurrentItem(current); // 인텐트로 넘어온 current(position)값을 이곳에 세팅, 이곳에 인텐트를 보낸 액티비티 아이템과 뷰페이저 화면을 개연성있게 세팅하기 위함.
    }

    private void start() { // 음원재생 시 메소드.
        playButtonStat = Const.STAT_PLAY; // 음원 재생, 일시정지, 빨리감기 등을 Const에 정의한 키,값으로 상태를 정의. // switch문으로 현재 상태를 구분해 메소드를 실행 시키기 위함.
        player.start();
        btnPlay.setImageResource(android.R.drawable.ic_media_pause); // 음원재생 시 버튼의 이미지 변화.
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

class SeekBarThread extends Thread { //쓰레드는 boolean 값으로 상태를 체크해서 처리한다. interrupt(), stop() 사용은 자제.
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
```

### Music

```java
public class Music { // 데이터 모델 클래스
    private static Music music = null;
    public List<Item> data = new ArrayList<>();

    private Music() {
    } // new 사용방지를 위해 생성자를 private으로 만든다

    public static Music getInstance() {
        if (music == null)
            music = new Music();
        return music;
    }

    // 음악 데이터를 불러오는 함수
    public void load(Context context) {
        ContentResolver resolver = context.getContentResolver();
        // 1. 테이블명 정의
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 2. 불러올 컬럼명 정의
        String proj[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };
        // 3. 쿼리
        Cursor cursor = resolver.query(uri, proj, null, null, proj[2] + " ASC");
        // 4. 쿼리결과가 담긴 커서를 통해 데이터 꺼내기
        if (cursor != null) {
            while (cursor.moveToNext()) { // 커서가 돌면서, 가지고 온 데이터를 모델에 세팅 시키는 과정.
                Item item = new Item();
                item.id = getValue(cursor, proj[0]); // 이는 곧, MediaStore.Audio.Media._ID의 String을 값을 인자로 넣겠다는 의미
                item.albumId = getValue(cursor, proj[1]);
                item.artist = getValue(cursor, proj[2]);
                item.title = getValue(cursor, proj[3]);

                item.musicUri = makeMusicUri(item.id); // 위에서 세팅한 모델 id를 uri로 바꾸는 메소드 for 주소
                item.albumUri = makeAlbumUri(item.albumId); // 마찬가지로 Uri로 바꾸는 메소드.
                data.add(item);
            }
            cursor.close();
        }
    }

    private String getValue(Cursor cursor, String name) {//  쿼리하는 것을 모듈화 시킴
        int index = cursor.getColumnIndex(name);
        return cursor.getString(index);
    }

    private Uri makeMusicUri(String musicId) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return Uri.withAppendedPath(contentUri, musicId);
    }

    private Uri makeAlbumUri(String albumId) {
        String contentUri = "content://media/external/audio/albumart/";
        return Uri.parse(contentUri + albumId);
    }

    // 실제 뮤직 데이터
    public class Item {
        public String id;      // 음악의 아이디
        public String albumId; // 앨범 아이디
        public String artist;
        public String title;

        public Uri musicUri; // 음악의 주소
        public Uri albumUri; // 앨범이미지의 주소
    }
}

```

### 기타
- baseActivity, Const,ListPagerAdapter은 생략


## 보충설명

- MediaPlayer: 이 클래스는 오디오와 비디오를 재생하는데 있어서 가장 기본적인 클래스
- AudioManager: 이 클래스는 오디오 소스와 출력 장비를 관리하는 클래스

### MediaPlayer 클래스

> 오디오와 비디오를 가져오고 디코딩하고 재생하는 것을 최소한의 설정으로 가능

- MediaPlayer 클래스는 안드로이드 미디어 프레임워크에서 가장 중요한 컴포넌트 중 하나
- 미디어 제공방식

```Java
- 로컬 resource
- ContentResolver등을 활용할 내부 URI
- 외부 URL(스트리밍)
```

![다이어그램](https://images.contentful.com/s72atsk5w5jo/4EiU48QgV2CwMKcCCkCywW/523ceea49c96543297c1c77224296ca5/android-mediaplayer-status.png)


[지원포맷정보](https://developer.android.com/guide/topics/media/media-formats.html)

### 멀티턴, 싱글턴

> 싱글턴 패턴(Singleton pattern)을 따르는 클래스는, 생성자가 여러 차례 호출되더라도 실제로 생성되는 객체는 하나이고 최초 생성 이후에 호출된 생성자는 최초의 생성자가 생성한 객체를 리턴한다. 이와 같은 디자인 유형을 싱글턴 패턴이라 한다.

> 멀티턴 패턴(Multiton pattern)은 싱글턴과 유사하지만 싱글턴은 1개의 인스턴스를, 멀티턴은 n개의 인스턴스를 갖는 다는 점이 다르다.

### instanceOf

> instanceof 연산자는 프로그램 실행시 참조 데이터형을 검사하기 위해 사용되는 연산자

- 왼쪽이 오른쪽에 오는 클래스의 객체이거나 하위 클래스의 객체일 경우 true를 반환하고, 그렇지 않을 경우 false를 반환


### 출처

- 출처: http://unikys.tistory.com/350 [All-round programmer]
- 출처: http://www.jynote.net/107 [하늘과 나의 IT 이야기]

## TODO

- 액티비티 및 프래그먼트 통신 등 다루지 못한 부분은  MusicPlayer2에 추가.
- musicPlayer1은 코드흐름 분석 및 간단한 사용방법을 알아보았고, musicPlayer2에서는 이러한 것을 토대로 로직 상에서의 이슈 및 보충설명 추가 필요

## Retrospect

- 첫번째로 그동안 배웠던 프래그먼트, 뷰페이저, 탭레이아웃 등의 UI가 가지고 있는 기본 메소드들이 익숙해지는 데 시간이 걸렸다.
- 두번째로는 안드로이드 자체적으로 제공해주는 mediaPlayer 클래스를 접했을때, 이를 로직 적재적소에 넣어 진짜 음악어플처럼 만드는 것이 꽤 어려웠다.
- 세번째로는 content resolver+datasetting, theread control+ seekBar, 프래그먼트와 액티비티간 통신 등 고려할 것이 참 많았던 것 같다. 


## Output

- 생략
package orgs.androidtown.musicplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import orgs.androidtown.musicplayer.MusicFragment.OnListFragmentInteractionListener;
import orgs.androidtown.musicplayer.model.Music;

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
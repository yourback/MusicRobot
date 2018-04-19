package gjjzx.com.robotclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import gjjzx.com.robotclient.bean.SongBean;
import gjjzx.com.robotclient.diy.CardRvAdapter;
import gjjzx.com.robotclient.diy.MyRecyclerView;

/**
 * Created by PC on 2017/10/26.
 */

class RecyclerViewFragment extends Fragment {

    private MyRecyclerView rv;
    private List<SongBean> songList;
    private Context context;

    public RecyclerViewFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment, container, false);
        rv = v.findViewById(R.id.myrecyclerview);
        songList = new ArrayList<>();
        songList.add(new SongBean(R.mipmap.pan, "月光", "1"));
        songList.add(new SongBean(R.mipmap.pan, "命运", "2"));
        songList.add(new SongBean(R.mipmap.pan, "夜曲", "3"));
        songList.add(new SongBean(R.mipmap.pan, "极乐净土", "4"));
        songList.add(new SongBean(R.mipmap.pan, "白夜", "5"));
        songList.add(new SongBean(R.mipmap.pan, "是非题", "6"));
        songList.add(new SongBean(R.mipmap.pan, "花香", "7"));
        songList.add(new SongBean(R.mipmap.pan, "海阔天空", "8"));
        songList.add(new SongBean(R.mipmap.pan, "童话镇", "9"));
        songList.add(new SongBean(R.mipmap.pan, "我的天空", "10"));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new CardRvAdapter(context, songList));
        rv.scrollToPosition(Integer.MAX_VALUE >> 1);
        return v;
    }
}

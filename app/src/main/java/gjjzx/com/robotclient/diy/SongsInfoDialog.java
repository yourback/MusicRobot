package gjjzx.com.robotclient.diy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import gjjzx.com.robotclient.R;
import gjjzx.com.robotclient.bean.SongBean;
import gjjzx.com.robotclient.util.LocalSQLUtil;

/**
 * Created by PC on 2017/10/31.
 */

public class SongsInfoDialog extends DialogFragment {
    private RecyclerView lv_songsinfo;

    private SongsInfoAdapter songsInfoAdapter;

    private Button btn_cancel;

    private List<SongBean> songlist;


    public SongsInfoDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //消失title
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //透明，配合background使用
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //点击外部不消失
        getDialog().setCanceledOnTouchOutside(false);

        View v = inflater.inflate(R.layout.songsinfodialog, null);

        btn_cancel = v.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        lv_songsinfo = v.findViewById(R.id.lv_songsinfo);
        lv_songsinfo.setLayoutManager(new LinearLayoutManager(getActivity()));

        songsInfoAdapter = new SongsInfoAdapter();

        lv_songsinfo.setAdapter(songsInfoAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.5), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 每次打开刷新列表
        songlist = LocalSQLUtil.getNewSongList();
    }


    private class SongsInfoAdapter extends RecyclerView.Adapter<SongsInfoAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songsinfodialogitem, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SongBean songBean = songlist.get(position);
            holder.tv_songName.setText(String.format("歌曲名称：%s", songBean.getSongName()));
            holder.tv_songCode.setText(String.format("歌曲代码：%s", songBean.getSongCode()));
        }

        @Override
        public int getItemCount() {
            return songlist.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_songName, tv_songCode;

            ViewHolder(View itemView) {
                super(itemView);
                tv_songCode = itemView.findViewById(R.id.tv_songCode_songsinfodialogitem);
                tv_songName = itemView.findViewById(R.id.tv_songName_songsinfodialogitem);
            }
        }

    }
}

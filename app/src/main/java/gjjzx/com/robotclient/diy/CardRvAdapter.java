package gjjzx.com.robotclient.diy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import gjjzx.com.robotclient.R;
import gjjzx.com.robotclient.bean.SongBean;

/**
 * Created by Administrator on 2017/2/6.
 */

public class CardRvAdapter extends RecyclerView.Adapter<CardRvAdapter.ItemViewHolder> {
    private List<SongBean> list;
    private Context context;

    public CardRvAdapter(Context context, List<SongBean> list) {
        this.context = context;
        this.list = list;

        Log.e("CardRvAdapter", "list.size: "+list.size() );
    }

    public void listRefresh(List<SongBean> songBeanList) {
        list = songBeanList;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_view, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {

        Log.e("onBindViewHolder", "position: "+position );
        Log.e("onBindViewHolder", "list.size: "+list.size() );

        final int i = position % list.size();

        SongBean sb = list.get(i);
        Glide.with(context).load(sb.getSongPic()).into(holder.iv);
        holder.tv.setText(sb.isPlaying() ? sb.getSongName() + " 正在播放" : sb.getSongName());

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(context, list.get(i).getSongName() + ":" + list.get(i).getSongCode(), Toast.LENGTH_SHORT).show();
//                for (int j = 0; j < list.size(); j++) {
//                    if (j == i) {
//                        list.get(i).setPlaying(true);
//                        //点歌
//                        ((MainActivity) context).orderSong(list.get(i));
//                    } else {
//                        list.get(j).setPlaying(false);
//                    }
//                }
//                notifyDataSetChanged();
//            }
//        });


        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickLitener.onItemClick(holder.itemView, i);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemClickLitener.onItemLongClick(holder.itemView, i);
                    return false;
                }
            });


        }


    }


    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv;
        private TextView tv;

        public ItemViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.imageView);
            tv = (TextView) itemView.findViewById(R.id.item_text);
        }
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}

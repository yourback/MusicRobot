package gjjzx.com.robotclient.diy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import gjjzx.com.robotclient.R;

/**
 * Created by PC on 2017/11/1.
 */

public class DeleteSongDialog extends DialogFragment {

    private Button btn_yes, btn_no;
    private TextView tv_msg;

    private String songName;

    @Override
    public void show(FragmentManager manager, String tag) {
        songName = tag;
        super.show(manager, tag);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //消失title
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //透明，配合background使用
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //点击外部不消失
        getDialog().setCanceledOnTouchOutside(false);

        View v = inflater.inflate(R.layout.judgmentdialog, null);

        btn_yes = v.findViewById(R.id.judgment_yes);
        btn_no = v.findViewById(R.id.judgment_no);
        tv_msg = v.findViewById(R.id.judgment_tv);
        tv_msg.setText("是否删除  "+songName);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteSongListener listener = (DeleteSongListener) getActivity();
                listener.onDeleteSong(songName);
            }
        });


        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }

    public interface DeleteSongListener {
        void onDeleteSong(String songName);
    }


    //弹出框宽度
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.5), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}

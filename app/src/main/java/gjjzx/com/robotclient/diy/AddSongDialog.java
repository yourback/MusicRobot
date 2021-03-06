package gjjzx.com.robotclient.diy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import gjjzx.com.robotclient.R;

/**
 * Created by PC on 2017/10/31.
 */

public class AddSongDialog extends DialogFragment {

    private EditText et_songName;
    private EditText et_songCode;

    private Button addSubmit;

    public AddSongDialog() {
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

        View v = inflater.inflate(R.layout.addsongdialog, null);
        et_songName = v.findViewById(R.id.addsong_name);
        et_songName.setInputType(InputType.TYPE_CLASS_TEXT);
        et_songCode = v.findViewById(R.id.addsong_code);
        et_songCode.setInputType(InputType.TYPE_CLASS_NUMBER);
        addSubmit = v.findViewById(R.id.addsong_submit);
        addSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String songName = et_songName.getText().toString().trim();
                String songCode = et_songCode.getText().toString().trim();
                onAddSongListener listener = (onAddSongListener) getActivity();
                listener.onAddSong(songName, songCode);
            }
        });

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
    }


    public interface onAddSongListener {
        void onAddSong(String songname, String songcode);
    }
}

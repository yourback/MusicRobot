package gjjzx.com.robotclient.diy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
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
import gjjzx.com.robotclient.bean.DesInfo;
import gjjzx.com.robotclient.util.SPUtil;

/**
 * Created by PC on 2018/4/17.
 */

public class SettingDialog extends DialogFragment {
    private EditText desIP;
    private EditText desPort;

    private Button setSubmit;

    private Context mContext;

    public SettingDialog(Context mContext) {
        this.mContext = mContext;
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

        View v = inflater.inflate(R.layout.settingdialog, null);

        // 获得SP中的地址显示
        DesInfo des = SPUtil.getDES();

        desIP = v.findViewById(R.id.des_ip);
        desIP.setText(des.getIp());
        desPort = v.findViewById(R.id.des_port);
        desPort.setText(des.getPort() + "");
        desPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        setSubmit = v.findViewById(R.id.setting_submit);
        setSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = desIP.getText().toString().trim();
                int port = Integer.parseInt(desPort.getText().toString().trim());
                SettingDialog.onSettingListener listener = (onSettingListener) getActivity();
                listener.onSetting(ip, port);
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


    public interface onSettingListener {
        void onSetting(String ip, int port);
    }
}

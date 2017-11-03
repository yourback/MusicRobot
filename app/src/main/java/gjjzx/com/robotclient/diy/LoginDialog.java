package gjjzx.com.robotclient.diy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gjjzx.com.robotclient.R;
import gjjzx.com.robotclient.app.MyApplication;

/**
 * Created by PC on 2017/10/31.
 */

public class LoginDialog extends DialogFragment {
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

    //    验证取消按钮
    private Button btn_submit, btn_cancel;
    private EditText et_managercode;

    private Context mContext;

    public LoginDialog(Context mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //点击外部不让消失
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View v = inflater.inflate(R.layout.logindialog, null);
        et_managercode = v.findViewById(R.id.login_code);
        et_managercode.setInputType(InputType.TYPE_CLASS_NUMBER);
        btn_submit = v.findViewById(R.id.login_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String managercode = et_managercode.getText().toString().trim();
                if (!TextUtils.isEmpty(managercode)) {
                    LoginSuccessListener listener = (LoginSuccessListener) getActivity();
                    listener.onLoginSuccessListener(managercode.equals(MyApplication.MANAGERSTR));
                } else {
                    Toast.makeText(mContext, "请填写管理码后确认", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn_cancel = v.findViewById(R.id.login_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginDialog.this.dismiss();
            }
        });

        return v;
    }

    public interface LoginSuccessListener {
        void onLoginSuccessListener(boolean isSuccess);
    }
}

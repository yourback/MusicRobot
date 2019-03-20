package gjjzx.com.robotclient.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.litepal.LitePal;

import gjjzx.com.robotclient.util.LocalSQLUtil;

/**
 * Created by PC on 2017/10/25.
 */

public class MyApplication extends Application {

    private static final String TAG = "app";

    private static Context content;

    public static boolean isManager = false;

    public static final String MANAGERSTR = "123456";

    //master版本
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        content = this;
        Log.e(TAG, "onCreate");
        LocalSQLUtil.setNoSongPlaying();
    }

    public static Context getContext() {
        return content;
    }


}

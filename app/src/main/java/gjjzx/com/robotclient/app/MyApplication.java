package gjjzx.com.robotclient.app;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by PC on 2017/10/25.
 */

public class MyApplication extends Application {
    private static Context content;

    public static boolean isSocketConnected = false;
    public static boolean isManager = false;

    public static final String MANAGERSTR = "123456";

    //master版本
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        content = this;
    }

    public static Context getContext() {
        return content;
    }
}

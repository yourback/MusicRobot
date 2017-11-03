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
    public static final String DSTIP = "192.168.253.1";
    public static final int DSTPORT = 5000;

    //wu

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        content = getApplicationContext();
    }

    public static Context getContext() {
        return content;
    }
}

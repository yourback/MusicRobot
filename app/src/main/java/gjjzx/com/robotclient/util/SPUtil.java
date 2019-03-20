package gjjzx.com.robotclient.util;

import android.content.Context;
import android.content.SharedPreferences;

import gjjzx.com.robotclient.app.MyApplication;
import gjjzx.com.robotclient.bean.DesInfo;

/**
 * Created by PC on 2018/4/17.
 */

public class SPUtil {
    public static void saveDES(String ip, int port) {
        SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("des", Context.MODE_PRIVATE).edit();
        editor.putString("ip", ip);
        editor.putInt("port", port);
        editor.apply();
    }

    public static DesInfo getDES() {
        SharedPreferences r = MyApplication.getContext().getSharedPreferences("des", Context.MODE_PRIVATE);
        int port = r.getInt("port", 5000);
        String ip = r.getString("ip", "192.168.1.1");
        return new DesInfo(ip, port);
    }

    public static void setStopTime(Integer timedelay) {
        SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("time", Context.MODE_PRIVATE).edit();
        editor.putInt("delay", timedelay);
        editor.apply();
    }

    public static Integer getStopTime() {
        SharedPreferences r = MyApplication.getContext().getSharedPreferences("time", Context.MODE_PRIVATE);
        return r.getInt("delay", 30);
    }
}

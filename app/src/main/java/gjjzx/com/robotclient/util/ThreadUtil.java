package gjjzx.com.robotclient.util;

import android.os.Looper;

/**
 * Created by PC on 2018/4/26.
 */

public class ThreadUtil {
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }
}

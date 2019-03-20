package gjjzx.com.robotclient.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


//定时接收类
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SocketManager.getSocket(context).stopPlaying();
    }
}
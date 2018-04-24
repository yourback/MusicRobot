package gjjzx.com.robotclient.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


//定时接收类
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "音乐播放结束", Toast.LENGTH_SHORT).show();
        SocketManager.getSocket(context).stopPlaying();
    }
}
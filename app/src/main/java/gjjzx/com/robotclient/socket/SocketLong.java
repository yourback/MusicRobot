package gjjzx.com.robotclient.socket;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by PC on 2018/4/24.
 */

public class SocketLong {
    //socket对象
    private Socket socket;

    //输入对象
    private BufferedReader br;
    //输入对象
    private BufferedWriter bw;

    //线程
    private ExecutorService mThreadPool;

    //长连接对象
    private static SocketLong instance;

    private SocketLong() {
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }
    }

    static SocketLong getInstance() {
        if (instance == null) {
            synchronized (SocketLong.class) {
                if (instance == null)
                    instance = new SocketLong();
            }
        }
        return instance;
    }

    private void connectAndSendData(final String data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.e("connnect", "点击连接");
                try {
                    socket = new Socket();
                    Log.e("connnect", "1");
                    SocketAddress isa = new InetSocketAddress("10.1.75.22", 5000);
                    Log.e("connnect", "2");
                    socket.connect(isa, 5000);
                    Log.e("connnect", "连接成功");
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Log.e("connnect", "输出流对象建立成功");
                    sendDataInner(data);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.e("connnect", "输入流对象建立成功");
                    //输入流持续监听
                    brListener();
                } catch (Exception e) {
                    Log.e("连接出错", e.toString());
                }
            }
        });
    }

    //输入流监听
    private void brListener() {
        try {
            Log.e("输入流", "输入流监听中....");
            String s = br.readLine();
            Log.e("输入流", "收到消息：" + s);
        } catch (Exception e) {
            Log.e("输入流", "输入流监听出错");
            Log.e("输入流", e.toString());
        }
    }

    //查看socket状态
    boolean socketStatus() {
        return !(socket == null || !socket.isConnected() || socket.isClosed());
    }

    //发送命令
    void sendOrder(final String data) {
        if (!socketStatus()) {
            connectAndSendData(data);
        } else {
            sendData(data);
        }
    }

    //发送数据
    private void sendData(final String data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                sendDataInner(data);
            }
        });
    }

    //发送数据Inner
    private void sendDataInner(String data) {
        Log.e("输出流", "发送数据：" + data);
        try {
            bw.write(data);
            bw.flush();
            Log.e("输出流", "发送数据：" + data + "成功");
        } catch (IOException e) {
            Log.e("输出流", "输出流出错");
            Log.e("输出流", e.toString());
        }
    }
}

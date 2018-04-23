package gjjzx.com.robotclient.socket;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gjjzx.com.robotclient.bean.DesInfo;
import gjjzx.com.robotclient.bean.SongBean;
import gjjzx.com.robotclient.util.LogUtil;
import gjjzx.com.robotclient.util.OrderUtil;
import gjjzx.com.robotclient.util.SPUtil;

/**
 * Created by PC on 2018/4/23.
 */

public class SocketManager {
    private static SocketManager socketManager;

    private Socket socket;

    // 线程池
    private static ExecutorService mThreadPool;

    //输出流
    private static BufferedWriter bw;
    //输入流
    private static BufferedReader br;

    //连接接口
    public interface ConnectListener {
        void connectFail();

        void connectSuccess();

        void connectStart();
    }

    //连接接口对象
    private ConnectListener connectListener;

    //连接接口管理
    private void connManager(String op) {
        if (connectListener == null)
            return;
        switch (op) {
            case "start":
                connectListener.connectStart();
                break;
            case "success":
                connectListener.connectSuccess();
                break;
            case "fail":
                connectListener.connectFail();
                break;
            default:
        }
    }

    private SocketManager(Context context) {
        connectListener = (ConnectListener) context;
        orderListener = (OrderListener) context;
        powerListener = (powerListener) context;
    }

    //点歌接口
    public interface OrderListener {
        void orderFail(SongBean song);

        void orderSuccess(SongBean song);

        void orderStart(SongBean song);
    }

    //点歌接口对象
    private OrderListener orderListener;

    //点歌接口管理
    private void orderManager(String op,SongBean song) {
        if (orderListener == null)
            return;
        switch (op) {
            case "start":
                orderListener.orderStart(song);
                break;
            case "success":
                orderListener.orderSuccess(song);
                break;
            case "fail":
                orderListener.orderFail(song);
                break;
            default:
        }
    }

    //关机接口
    public interface powerListener {
        void powerStart();

        void powerFail();

        void powerSuccess();
    }

    //关机接口对象
    powerListener powerListener;

    //关机接口管理
    private void powerManager(String op) {
        if (powerListener == null)
            return;
        switch (op) {
            case "start":
                powerListener.powerStart();
                break;
            case "success":
                powerListener.powerSuccess();
                break;
            case "fail":
                powerListener.powerFail();
                break;
            default:
        }
    }


    //初始化socket
    private boolean initSocket() {
        try {
            connManager("start");
            LogUtil.e("socket初始化", "开始连接");
            socket = new Socket();
            // 取目的socket
            DesInfo des = SPUtil.getDES();
            InetSocketAddress isa = new InetSocketAddress(des.getIp(), des.getPort());
            socket.connect(isa, 5000);
            connManager("success");
            LogUtil.e("socket初始化", "连接成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            connManager("fail");
            LogUtil.e("socket初始化", "连接失败");
            return false;
        }
    }


    public static SocketManager getSocket(Context c) {
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }

        if (socketManager == null) {
            synchronized (SocketManager.class) {
                if (socketManager == null)
                    socketManager = new SocketManager(c);
            }
        }
        return socketManager;
    }

    //点歌
    public void orderSong(final SongBean song) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    if (initSocket()) {
                        LogUtil.e("点歌", "连接成功后进行点歌操作" + song.getSongCode());
                        orderSongInner(song);
                    }

                } else {
                    LogUtil.e("点歌", "连接已经建立，直接点歌" + song.getSongCode());
                    orderSongInner(song);
                }
            }
        });
    }

    //关机
    public void powerOff() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    if (initSocket()) {
                        LogUtil.e("关机", "连接成功后进行关机操作");
                        powerOffInner();
                    }

                } else {
                    LogUtil.e("关机", "连接已经建立，直接关机");
                    powerOffInner();
                }
            }
        });
    }

    private void powerOffInner() {
        try {
            powerManager("start");
            LogUtil.e("orderSongInner", "创建输出流");
            if (bw == null) {
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                LogUtil.e("orderSongInner", "创建输出流成功");
            }
            bw.write(OrderUtil.shutDown());
            bw.flush();
            LogUtil.e("orderSongInner", "数据发出成功，创建输入流");
            powerManager("success");
        } catch (Exception e) {
            LogUtil.e("orderSongInner", e.toString());
            LogUtil.e("orderSongInner", "创建输入流/输出流&发送失败");
            powerManager("fail");
        } finally {
            try {
                bw.close();
                bw = null;
                socket.close();
                socket = null;
                LogUtil.e("orderSongInner", "关闭输入输出流成功");
            } catch (IOException e) {
                LogUtil.e("orderSongInner", e.toString());
                LogUtil.e("orderSongInner", "关闭输入流/输出流失败");
            }
        }
    } 

    private void orderSongInner(SongBean song) {

        try {
            orderManager("start",song);
            LogUtil.e("orderSongInner", "创建输出流");
            if (bw == null) {
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                LogUtil.e("orderSongInner", "创建输出流成功");
            }
            bw.write(OrderUtil.switchSong(song.getSongCode()));
            bw.flush();
            LogUtil.e("orderSongInner", "数据发出成功，创建输入流");
            //发送后立即接收
            if (br == null) {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                LogUtil.e("orderSongInner", "创建输入流成功");
            }
            String s = br.readLine();
            LogUtil.e("orderSongInner", "输入流收到数据：" + s);
            if (song.getSongCode().equals(s)) {
                LogUtil.e("orderSongInner", "点歌：" + song.getSongCode() + "，返回：" + s);
                orderManager("success",song);
            } else {
                LogUtil.e("orderSongInner", "songcode与返回值不符合");
                orderManager("fail",song);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("orderSongInner", e.toString());
            LogUtil.e("orderSongInner", "创建输入流/输出流&发送失败");
            orderManager("fail",song);
        } finally {
            try {
                bw.close();
                br.close();
                bw = null;
                br = null;
                socket.close();
                socket = null;
                LogUtil.e("orderSongInner", "关闭输入输出流成功");
            } catch (IOException e) {
                LogUtil.e("orderSongInner", e.toString());
                LogUtil.e("orderSongInner", "关闭输入流/输出流失败");
            }
        }

    }


}

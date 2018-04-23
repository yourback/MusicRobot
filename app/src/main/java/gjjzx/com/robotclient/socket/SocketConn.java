package gjjzx.com.robotclient.socket;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import gjjzx.com.robotclient.app.MyApplication;
import gjjzx.com.robotclient.bean.DesInfo;
import gjjzx.com.robotclient.util.LogUtil;
import gjjzx.com.robotclient.util.OrderUtil;
import gjjzx.com.robotclient.util.SPUtil;

/**
 * Created by PC on 2017/10/23.
 */

public class SocketConn {

    private static final String TAG = "客户端";

    ReadThread readThread;

//    public SocketConn(final String songCode) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //初始化客户端socket
//                //初始化成功则开启监听线程
//                //否则不执行，等待用户手动启动
//                if (initClientSocket(TextUtils.isEmpty(songCode) ? null : songCode)) {
//                    MyApplication.isSocketConnected = true;
//                    //开线程监听服务器返回数据
//                    readThread = new ReadThread();
//                    readThread.start();
//                } else {
//                    MyApplication.isSocketConnected = false;
//                    if (connectedFail != null)
//                        connectedFail.failFunc();
//                }
//            }
//        }).start();
//    }

    public SocketConn() {
    }

    //连接方法
    public void connectToRobot(String songCode) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //初始化可能是耗时操作
//                //连接有可能失败，最有可能的是服务器没有启动
//                try {
//                    socket = new Socket("192.168.43.1", 5000);
//                    output = new PrintStream(socket.getOutputStream(), true, "gbk");
//
//
//                } catch (Exception e) {
//                    Log.e(TAG,"1111"+ e.toString());
//                    if (connectedFail != null)
//                        connectedFail.failFunc();
//                }
//            }
//        }).start();

        //开线程监听服务器返回数据
        //如果监听启动成功了就意味着socket连接成功了
        if (readThread == null) {
            readThread = new ReadThread();
            readThread.start(songCode);
            LogUtil.e("readThread", "线程为空");
        } else {
            readThread.run(songCode);
            LogUtil.e("readThread", "线程不为空");
        }
    }

    //点歌方法
    public void orderSong(String songCode) {
        if (MyApplication.isSocketConnected) {
            try {
                //点歌有可能失败，socket链接丢失之类的
                if (out == null) {
                    //output = new PrintStream(socket.getOutputStream(), true, "gbk");
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                }

                //根据songcode获得命令
                String srd = OrderUtil.switchSong(songCode);
                sendMessage(srd);
            } catch (Exception e) {
                Log.e(TAG, "out 初始化失败");
                MyApplication.isSocketConnected = false;
                if (connectedFail != null)
                    connectedFail.failFunc();
            }
        }

    }


    private Socket socket;
    private InetSocketAddress isa;

    private BufferedWriter out;

    private BufferedReader in;

    //持续监听服务器是否发来数据
    private class ReadThread extends Thread {

        private String code;


        @Override
        public void run() {
            super.run();
            //监听服务器返回数据
            while (true) {
                String data = recevieData(code);

                //data 大于1说明接收到数据了
                //如果data = null 说明socket 已经断开了
                //socket 断开后，监听结束，下次连接后再启动
                if (TextUtils.isEmpty(data)) {
                    if (connectedFail != null)
                        connectedFail.failFunc();
                    break;
                } else if (data.equals("bb")) {
                    MyApplication.isSocketConnected = false;
                    break;
                } else if (data.equals("256")) {
                    //歌曲播放结束
                    if (OrderSongListener != null)
                        OrderSongListener.songfinished();
                } else {
                    //如果有返回的数据，说明点歌成功
                    //就不需要显示服务器无响应的提示了
//                    MyApplication.isServerError = false;
                    //点歌成功，需要在界面上显示
                    Log.e(TAG, "点歌成功！服务器返回值：" + data);
                    if (OrderSongListener != null)
                        OrderSongListener.successFunc(data);
                }


            }
        }

        public void start(String songCode) {
            code = songCode;
            try {
                start();
            } catch (Exception e) {
                LogUtil.e("线程出错", "该线程已经开启");
                run();
            }

        }

        public void run(String songCode) {
            code = songCode;
            run();
        }
    }


    //接收服务器传来的数据
    private String recevieData(String code) {

        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            try {
                Log.e(TAG, "socket开启");
                socket = new Socket();
//                取目的socket
                DesInfo des = SPUtil.getDES();

                isa = new InetSocketAddress(des.getIp(), des.getPort());
                socket.connect(isa, 5000);

                Log.e(TAG, "out生成");
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Log.e(TAG, "in生成");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                MyApplication.isSocketConnected = true;
            } catch (IOException e) {
                Log.e(TAG, "错了" + e.toString());
                //弹出链接服务器失败的提示
                return "";
            }
        }
        String data = null;
        if (socket.isConnected() && !socket.isClosed()) {
            try {
                //点歌有可能失败，socket链接丢失之类的
                if (in == null) {
                    Log.e(TAG, "重新生成in");
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }

                //点歌
                orderSong(code);

                Log.e(TAG, "等待数据");
                data = in.readLine();
                Log.e(TAG, "收到消息：" + data);
//                if (data == null) return null;
                if (data == null) {
                    if (out != null)
                        out.close();
                    if (in != null)
                        in.close();
                    if (socket != null)
                        socket.close();
                    return null;
                } else if (data.equals("bb")) {
                    return "bb";
                }
            } catch (IOException e) {
                Log.e(TAG, "in 初始化失败");
                MyApplication.isSocketConnected = false;
                return "";
            }
        }
        return data;
    }

    //向服务端发送数据
    public void sendMessage(String str) {  
        try {
            LogUtil.e("sendMessage", "发送消息：" + str);
            out.write(str);
            out.flush();
        } catch (Exception e) {
            MyApplication.isSocketConnected = false;
            if (connectedFail != null)
                connectedFail.failFunc();
        }
    }

    //点歌成功接口
    public interface OrderSongListener {
        void successFunc(String str);

        void songfinished();
    }

    OrderSongListener OrderSongListener;

    public void setOrderSongListener(OrderSongListener OrderSongListener) {
        this.OrderSongListener = OrderSongListener;
    }

    //链接失败接口
    public interface ConnectedFail {
        void failFunc();
    }

    ConnectedFail connectedFail;

    public void setConnectedFail(ConnectedFail connectedFail) {
        this.connectedFail = connectedFail;
    }

    public void closeAll() {
        try {
            if (socket != null && !socket.isClosed() && socket.isConnected() && out != null)
                sendMessage("bb");
//            if (out != null)
//                out.close();
//            if (in != null)
//                in.close();
//            if (socket != null)
//                socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package gjjzx.com.robotclient.socket;

import android.content.Context;
import android.text.TextUtils;

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

import gjjzx.com.robotclient.bean.DesInfo;
import gjjzx.com.robotclient.bean.SongBean;
import gjjzx.com.robotclient.util.LocalSQLUtil;
import gjjzx.com.robotclient.util.LogUtil;
import gjjzx.com.robotclient.util.OrderUtil;
import gjjzx.com.robotclient.util.SPUtil;


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

    private SocketLong(Context c) {
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }
        iConnect = (SocketLong.iConnect) c;
        iOrder = (SocketLong.iOrder) c;
        iPowerOff = (SocketLong.iPowerOff) c;
    }

    public static SocketLong getInstance(Context c) {
        if (instance == null) {
            synchronized (SocketLong.class) {
                if (instance == null)
                    instance = new SocketLong(c);
            }
        }
        return instance;
    }

    private void connectAndSendData(final SongBean data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                LogUtil.e("connnect", "点击连接");
                try {
                    iConnectManager(START);
                    socket = new Socket();
                    //获得目的IP和端口号
                    DesInfo des = SPUtil.getDES();
                    SocketAddress isa = new InetSocketAddress(des.getIp(), des.getPort());
                    socket.connect(isa, 5000);
                    LogUtil.e("connnect", "连接成功");
                    //连接成功后开启监听
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    LogUtil.e("connnect", "输入流对象建立成功");
                    iConnectManager(SUCCESS);
                    //连接成功后点歌
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    LogUtil.e("connnect", "输出流对象建立成功");
                    //判断是否是点歌，还是关机
                    if (data == null) {
                        //关机
                        powerOffInner();
                    } else {
                        //点歌
                        sendDataInner(data);
                        //输入流持续监听
                        brListener();
                    }

                } catch (Exception e) {
                    LogUtil.e("connnect出错", e.toString());
                    iConnectManager(FAIL);
                }
            }
        });
    }

    //输入流监听
    private void brListener() {
        try {
            LogUtil.e("输入流", "输入流监听中....");
            String line;
            while ((line = br.readLine()) != null) {
                LogUtil.e("输入流", "收到消息：" + line);
                if (TextUtils.isEmpty(line) || line.equals("bb")) {
                    //断开连接
                    closeAll();
                } else if (line.equals("256")) {
                    //歌曲结束
                    iOrderManager(END);
                } else {
                    //点歌成功
                    //检索固定id的songbean对象
                    SongBean sb = LocalSQLUtil.getSongBeanFromCode(line);
                    iOrderManager(SUCCESS, sb);
                }
            }
        } catch (Exception e) {
            LogUtil.e("输入流", "输入流监听出错");
            LogUtil.e("输入流", e.toString());
            //断开连接
            closeAll();
        }
    }

    public void closeAll() {
        try {
            LogUtil.e("关闭", "关闭br，bw，socket");
            br.close();
            bw.close();
            socket.close();
        } catch (Exception e) {
            LogUtil.e("关闭", "关闭br，bw，socket出错");
        }
    }

    //查看socket状态
    private boolean socketStatus() {
        return !(socket == null || !socket.isConnected() || socket.isClosed());
    }

    //发送点歌命令
    public void sendOrder(final SongBean sb) {
        if (!socketStatus()) {
            connectAndSendData(sb);
        } else {
            sendData(sb);
        }
    }

    //发送点歌数据
    private void sendData(final SongBean data) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (data == null) {
                    powerOffInner();
                } else {
                    sendDataInner(data);
                }

            }
        });
    }

    //关机Inner
    private void powerOffInner() {
        LogUtil.e("关机", "发送关机指令");
        //关机中......
        iPowerOffManager(START);
        try {
            bw.write(OrderUtil.shutDown());
            bw.flush();
            LogUtil.e("关机", "发送关机指令成功");
            iPowerOffManager(SUCCESS);
        } catch (IOException e) {
            LogUtil.e("关机", "关机出错");
            LogUtil.e("关机", e.toString());
            iPowerOffManager(FAIL);
        }
    }

    //发送数据Inner
    private void sendDataInner(SongBean data) {
        LogUtil.e("输出流", "发送数据：" + data);
        //点歌中......
        iOrderManager(START, data);
        try {
            bw.write(OrderUtil.switchSong(data.getSongCode()));
            bw.flush();
            LogUtil.e("输出流", "发送数据：" + data + "成功");
        } catch (IOException e) {
            LogUtil.e("输出流", "输出流出错");
            LogUtil.e("输出流", e.toString());
            iOrderManager(FAIL, data);
        }
    }

    //接口管理变量设置
    private static final int START = 1;
    private static final int SUCCESS = 2;
    private static final int FAIL = 3;
    private static final int END = 4;


    //    --------------------------------------连接接口------------------------------------------------
    public interface iConnect {
        void conncetStart();

        void connectSuccess();

        void connectFail();
    }

    //连接接口对象
    private iConnect iConnect;

    //连接接口管理
    private void iConnectManager(Integer i) {
        if (iConnect == null)
            return;
        switch (i) {
            case START:
                iConnect.conncetStart();
                break;
            case SUCCESS:
                iConnect.connectSuccess();
                break;
            case FAIL:
                iConnect.connectFail();
                break;
            default:
        }
    }

    //    --------------------------------------点歌接口------------------------------------------------
    public interface iOrder {
        void orderStart(SongBean sb);

        void orderSuccess(SongBean sb);

        void orderFail(SongBean sb);

        //        void orderEnd(SongBean sb);
        void orderEnd();
    }

    //连接接口对象
    private iOrder iOrder;

    //连接接口管理
    private void iOrderManager(Integer i, SongBean... sb) {
        if (iOrder == null)
            return;
        switch (i) {
            case START:
                iOrder.orderStart(sb[0]);
                break;
            case SUCCESS:
                iOrder.orderSuccess(sb[0]);
                break;
            case FAIL:
                iOrder.orderFail(sb[0]);
                break;
            case END:
                iOrder.orderEnd();
                break;
            default:
        }
    }


    //    --------------------------------------关机接口------------------------------------------------
    public interface iPowerOff {
        void powerOffStart();

        void powerOffSuccess();

        void powerOffFail();
    }

    //连接接口对象
    private iPowerOff iPowerOff;

    //连接接口管理
    private void iPowerOffManager(Integer i) {
        if (iPowerOff == null)
            return;
        switch (i) {
            case START:
                iPowerOff.powerOffStart();
                break;
            case SUCCESS:
                iPowerOff.powerOffSuccess();
                break;
            case FAIL:
                iPowerOff.powerOffFail();
                break;
            default:
        }
    }

}

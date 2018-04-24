package gjjzx.com.robotclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.lemonsoft.lemonbubble.LemonBubble;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gjjzx.com.robotclient.app.MyApplication;
import gjjzx.com.robotclient.bean.SongBean;
import gjjzx.com.robotclient.diy.AddSongDialog;
import gjjzx.com.robotclient.diy.CardRvAdapter;
import gjjzx.com.robotclient.diy.DeleteSongDialog;
import gjjzx.com.robotclient.diy.LoginDialog;
import gjjzx.com.robotclient.diy.MyRecyclerView;
import gjjzx.com.robotclient.diy.PowerOffDialog;
import gjjzx.com.robotclient.diy.SettingDialog;
import gjjzx.com.robotclient.diy.SongsInfoDialog;
import gjjzx.com.robotclient.socket.AlarmReceiver;
import gjjzx.com.robotclient.socket.SocketConn;
import gjjzx.com.robotclient.socket.SocketManager;
import gjjzx.com.robotclient.util.LocalSQLUtil;
import gjjzx.com.robotclient.util.LogUtil;
import gjjzx.com.robotclient.util.SPUtil;

public class MainActivity extends AppCompatActivity implements SocketManager.songPlayingStopListener, SocketManager.powerListener, SocketManager.ConnectListener, SocketManager.OrderListener, PowerOffDialog.PowerOffListener, SettingDialog.onSettingListener, LoginDialog.LoginSuccessListener, DeleteSongDialog.DeleteSongListener, AddSongDialog.onAddSongListener {

    private static final String TAG = "客户端";
    private static final int CONNECTEDFAIL = 10000;
    private static final int ORDERSONGSUCCESS = 10001;
    private static final int WAITING = 10002;
    private static final int DELETESONGSUCCESS = 10003;
    private static final int DELETESONGFAIL = 10004;
    private static final int DELETEPLAYINGSONGFAIL = 10005;
    private static final int DELETEERRORSONG = 10006;
    private static final int ADDSONGSUCCESS = 10007;
    private static final int ADDSONGFAIL = 10008;
    private static final int SONGPLAYFINISHED = 10009;
    private static final int POWEROFF = 10010;


    //歌曲停止定制操作，相关变量

    //歌曲停止时间
    private static final long SONGSTOPTIME = 5 * 1000;

    private Intent i;

    private AlarmManager am;

    private PendingIntent sender;
    //----------------------------------------


    private DrawerLayout drawerlayout;

    private NavigationView navigationView;
    //headview子视图
    private ImageView headImage;
    private TextView headTv;

    //recyclerview
    private MyRecyclerView rv;
    CardRvAdapter rvAdapter;
    private List<SongBean> songList;

    private TextView titleText;


    private Button titleLeftBtn;

    private AddSongDialog addSongDialog;
    private SongsInfoDialog songsInfoDialog;
    private LoginDialog loginDialog;
    private DeleteSongDialog deleteSongDialog;
    private SettingDialog settingDialog;
    private PowerOffDialog powerOffDialog;

    //socket链接对象
    private SocketConn sc;


    //弱引用对象
    private UIhandler mHandler;


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case CONNECTEDFAIL:
                    currentSong = null;
                    LocalSQLUtil.setNoSongPlaying();
                    titleText.setText("歌曲列表");
                    //显示点歌失败
                    showFail("失败\n请检查机器人是否开启");
                    break;
                case WAITING:
                    waitingShow((String) message.obj);
                    break;
                case ORDERSONGSUCCESS:
                    Toast.makeText(MainActivity.this, "点歌成功，发来消息：" + message.obj, Toast.LENGTH_SHORT).show();
//                    switchSongs(currentSong.getSongName());
                    rvAdapter.listRefresh(songList);
                    //点歌成功
                    showSuccess("点歌成功");
                    break;
                case DELETESONGSUCCESS:
                    showSuccess("删除歌曲成功");
                    break;
                case DELETESONGFAIL:
                    showFail("删除歌曲失败");
                    break;
                case DELETEPLAYINGSONGFAIL:
                    showFail("歌曲正在播放\n无法删除");
                    break;
                case DELETEERRORSONG:
                    showFail("删除出错，未找到歌曲");
                    break;
                case ADDSONGSUCCESS:
                    showSuccess("添加歌曲成功");
                    break;
                case ADDSONGFAIL:
                    showFail("添加歌曲失败");
                    break;

                case SONGPLAYFINISHED:
                    String endSong = titleText.getText().toString().trim().split("：")[1];

                    LogUtil.e("end", endSong);
                    Toast.makeText(MainActivity.this, endSong + "播放结束", Toast.LENGTH_SHORT).show();
                    titleText.setText("歌曲列表");
                    rvAdapter.listRefresh(songList);
                    break;

                case POWEROFF:
                    showSuccess("电机关闭成功");
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        findView();

        initRecyclerView();

//        initSocketConn();

        initDialog();

        initAlarm();

    }


    //recyclerview初始化
    private void initRecyclerView() {
        songList = LocalSQLUtil.setNoSongPlaying();

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvAdapter = new CardRvAdapter(this, songList);
        rvAdapter.setOnItemClickLitener(new CardRvAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
//                Toast.makeText(MainActivity.this, songList.get(position).getSongName() + ":" + songList.get(position).getSongCode(), Toast.LENGTH_SHORT).show();
                for (int j = 0; j < songList.size(); j++) {
                    if (j == position) {
                        SongBean bean = songList.get(position);
                        if (currentSong != null && bean.equals(currentSong)) {
                            Toast.makeText(MainActivity.this, bean.getSongName() + "已经在播放了", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //点歌
                        orderSong(bean);
                        return;
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (MyApplication.isManager) {
//                    如果是管理者才让删除
                    deleteSongDialog.show(getFragmentManager(), songList.get(position).getSongName());
                }
            }
        });
        rv.setAdapter(rvAdapter);
        rv.scrollToPosition(Integer.MAX_VALUE >> 1);
    }

    //添加歌曲按钮
    @Override
    public void onAddSong(String sn, String sc) {

        if (Integer.parseInt(sc) < 1 || Integer.parseInt(sc) > 255) {
            Toast.makeText(this, "歌曲序号需在1~255之间", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(sn) || TextUtils.isEmpty(sc)) {
            Toast.makeText(this, "请完善歌曲信息后添加", Toast.LENGTH_LONG).show();
        } else if (LocalSQLUtil.isSongExist("songName", sn)) {
            Toast.makeText(this, "歌曲名称已存在，请重试", Toast.LENGTH_LONG).show();
        } else if (LocalSQLUtil.isSongExist("songCode", sc)) {
            Toast.makeText(this, "歌曲代码已存在，请重试", Toast.LENGTH_SHORT).show();
        } else {
            //添加数据到本地数据库中
            SongBean sb = new SongBean(R.mipmap.pan, sn, sc);
            Message msg = new Message();
            msg.what = WAITING;
            msg.obj = "歌曲添加中...";
            handler.sendMessage(msg);
            addSongDialog.dismiss();
            try {
                songList = LocalSQLUtil.addSong(sb);
                rvAdapter.listRefresh(songList);
                handler.sendEmptyMessageDelayed(ADDSONGSUCCESS, 1000);
            } catch (Exception e) {
                handler.sendEmptyMessageDelayed(ADDSONGFAIL, 1000);
            }
        }
    }

    //删除歌曲按钮
    @Override
    public void onDeleteSong(String songName) {
        Message msg = new Message();
        msg.what = WAITING;
        msg.obj = "歌曲删除中...";
        handler.sendMessage(msg);
        deleteSongDialog.dismiss();
        if (currentSong != null)
            if (songName.equals(currentSong.getSongName())) {
                handler.sendEmptyMessageDelayed(DELETEPLAYINGSONGFAIL, 1000);
                return;
            }
        try {
            for (SongBean sb : songList) {
                if (sb.getSongName().equals(songName)) {
                    songList = LocalSQLUtil.delSong(sb.getSongCode());
                    rvAdapter.listRefresh(songList);
                    handler.sendEmptyMessageDelayed(DELETESONGSUCCESS, 1000);
                    return;
                }
            }
            handler.sendEmptyMessageDelayed(DELETEERRORSONG, 1000);
        } catch (Exception e) {
            handler.sendEmptyMessageDelayed(DELETESONGFAIL, 1000);
        }
    }

    private void initDialog() {
        addSongDialog = new AddSongDialog(this);
        loginDialog = new LoginDialog(this);
        deleteSongDialog = new DeleteSongDialog();
        settingDialog = new SettingDialog(this);
        songsInfoDialog = new SongsInfoDialog();
        powerOffDialog = new PowerOffDialog();
    }

    //loginDialog管理员验证成功后方法
    @Override
    public void onLoginSuccessListener(boolean isSuccess) {
        if (isSuccess) {
            loginDialog.dismiss();
            Glide.with(MainActivity.this).load(R.mipmap.logina).into(headImage);
            headTv.setText("管理员");
            MyApplication.isManager = true;
            Toast.makeText(MainActivity.this, "进入管理模式", Toast.LENGTH_SHORT).show();
        }
        //密码错误
        else {
            Toast.makeText(MainActivity.this, "管理码错误", Toast.LENGTH_SHORT).show();
        }
    }

    //socket连接监听初始化
    private void initSocketConn() {
        sc = new SocketConn();
        //如果有监听，请在这里初始化
        //socket连接失败监听
        sc.setConnectedFail(new SocketConn.ConnectedFail() {
            @Override
            public void failFunc() {
                handler.sendEmptyMessageDelayed(CONNECTEDFAIL, 1000);
            }
        });

        //点歌成功监听初始化
        sc.setOrderSongListener(new SocketConn.OrderSongListener() {
            @Override
            public void successFunc(String str) {
                /**
                 * 1、从本地数据库中找到正在播放的歌曲
                 *          以下if部分放在点歌开始的时候！！！！！
                 *
                 *         if 歌曲的代码 == str
                 *              提示：歌曲正在播放中...
                 *              （这里可以弹出对话框，停止播放 | 重新播放）
                 *         else
                 *              播放状态全部置false
                 *              更新code为str的歌曲的播放状态为true
                 *
                 *
                 */


                //点歌成功后设置本地数据库全歌曲不在播放
                LocalSQLUtil.setNoSongPlaying();
                //更新本地数据库中对应的歌曲code为str的播放状态
                songList = LocalSQLUtil.setSongPlaying(str);
                //点歌成功，切换歌曲
//                handler.sendEmptyMessageDelayed(ORDERSONGSUCCESS, 1000);
                Message message = new Message();
                message.what = ORDERSONGSUCCESS;
                message.obj = str;
                handler.sendMessage(message);
            }

            @Override
            public void songfinished() {
                //歌曲播放完毕
                songList = LocalSQLUtil.setNoSongPlaying();
                handler.sendEmptyMessage(SONGPLAYFINISHED);
            }
        });
    }


    //点歌成功的回调需要执行的
    public void switchSongs(SongBean song) {
        //点歌成功后设置本地数据库全歌曲不在播放
        LocalSQLUtil.setNoSongPlaying();
        //更新本地数据库中对应的歌曲code为str的播放状态
        songList = LocalSQLUtil.setSongPlaying(song.getSongCode());
        //切换
        titleText.setText(String.format("当前播放曲目：%s", song.getSongName()));
        //刷新列表
        rvAdapter.listRefresh(songList);
    }

    //关机成功后的执行函数
    public void powerOff() {
        songList = LocalSQLUtil.setNoSongPlaying();

        titleText.setText("歌曲列表");

        //刷新列表
        rvAdapter.listRefresh(songList);
        //当前播放音乐为空
        currentSong = null;
    }


    //保存当前点的歌曲
    private SongBean currentSong;

    //点歌（adapter调用）
    public void orderSong(SongBean songBean) {
        //显示等待的圈圈
//        Message msg = new Message();
//        msg.what = WAITING;
//        msg.obj = "点歌中，请稍后...";
//        handler.sendMessage(msg);
//        currentSong = songBean;

//        orderSongs(currentSong.getSongCode());
        orderSongs(songBean);


        //检查是否已经链接到服务器
//        if (MyApplication.isSocketConnected) {
//            Log.e(TAG, "已连接点歌：" + currentSong.getSongCode());
//            //若已经连接，直接sendMessage
//            sc.orderSong(currentSong.getSongCode());
//
//
//
//
//
//        } else {
//            Log.e(TAG, "未连接点歌：" + currentSong.getSongCode());
//            //如果没有链接到服务器则进行初始化链接
//            sc.connectToRobot(currentSong.getSongCode());


//            //如果初始化未出错，则进行点歌操作
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (currentSong != null)
//                        sc.orderSong(currentSong.getSongCode());
//                }
//            }, 2000);
//        }
    }


    //等待弹窗
    private void waitingShow(String str) {
        LemonBubble.showRoundProgress(this, str);
    }

    //失败弹窗
    private void showFail(String str) {
        LemonBubble.showError(this, str, 2000);
    }

    //成功弹窗
    private void showSuccess(String str) {
        LemonBubble.showRight(this, str, 1000);
    }

    //弹窗消失
    private void waitingHide(){LemonBubble.hide();}

    //视图绑定
    private void findView() {

        //弱引用对象
        mHandler = new UIhandler(this);


        rv = (MyRecyclerView) findViewById(R.id.myrecyclerview);

        navigationView = (NavigationView) findViewById(R.id.navigation);
        drawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        titleText = (TextView) findViewById(R.id.title_text);
        titleLeftBtn = (Button) findViewById(R.id.title_left_btn);

        View headerView = navigationView.getHeaderView(0);

        headImage = headerView.findViewById(R.id.nav_head_image);
        headTv = headerView.findViewById(R.id.nav_head_tv);

        headerView.setBackgroundColor(Color.parseColor("#40808080"));

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MyApplication.isManager) {
                    loginDialog.show(getFragmentManager(), "logindialog");
                }
            }
        });

        // 侧面菜单设置
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int id = item.getItemId();

                if (id == R.id.nav_about) {
                    Toast.makeText(MainActivity.this, "关于", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_songs) {
                    songsInfoDialog.show(getFragmentManager(), "songsInfoDialog");
                } else {
                    //如果是点的增加或者删除如果不是管理者提示登录后进行曲目管理
                    if (MyApplication.isManager) {
                        switch (id) {
                            case R.id.nav_add:
                                addSongDialog.show(getFragmentManager(), "addsongdialog");
                                break;
                            case R.id.nav_del:
                                Toast.makeText(MainActivity.this, "长按曲目进行删除操作", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_settings:
                                settingDialog.show(getFragmentManager(), "settingDialog");
                                break;
                            case R.id.nav_shutdown:
                                powerOffDialog.show(getFragmentManager(), "powerOffDialog");
                                break;
                            default:
                                break;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "请登录后进行操作", Toast.LENGTH_SHORT).show();
                    }
                }


                drawerlayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        titleLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerlayout.openDrawer(Gravity.START);
            }
        });
    }

    //活动销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
//       所有歌曲都不在播放
        LocalSQLUtil.setNoSongPlaying();
//        断开socket链接
//        sc.closeAll();
    }


    @Override
    public void onBackPressed() {
        exitBy2Click();
    }


    //双击退出函数
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (!isExit) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
        }
    }

    @Override
    public void onSetting(String ip, int port) {
        //存储数据，并断开连接
        SPUtil.saveDES(ip, port);
        //断开socket链接
        if (sc != null)
            sc.closeAll();
        //隐藏
        settingDialog.dismiss();
        //修改成功提示
        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onPowerOff() {
//        Message msg = new Message();
//        msg.what = WAITING;
//        msg.obj = "电机关闭中...";
//        handler.sendMessage(msg);
//        sc.sendMessage(OrderUtil.shutDown());
//        handler.sendEmptyMessageDelayed(POWEROFF, 2000);
//    }


    //    -----------------------------------------新点歌操作------------------------------------------------

    //点歌操作
    public void orderSongs(SongBean song) {
        SocketManager.getSocket(this).orderSong(song);
    }

    //关机操作
    @Override
    public void onPowerOff() {
        SocketManager.getSocket(this).powerOff();
    }


    //---------------------------------------------重写连接接口--------------------------------------
    //连接失败
    @Override
    public void connectFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showFail("连接失败");
            }
        });
    }

    //连接成功
    @Override
    public void connectSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingShow("连接成功！");
            }
        });
    }

    //开始连接
    @Override
    public void connectStart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingShow("正在连接机器人...");
            }
        });
    }

    //---------------------------------------------重写点歌接口--------------------------------------

    //点歌失败
    @Override
    public void orderFail(SongBean song) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showFail("点歌失败");
            }
        });
    }

    //点歌成功
    @Override
    public void orderSuccess(final SongBean song) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //切歌
                switchSongs(song);
                //重新设置定时器
                setAlarm();
                showSuccess("点歌成功！");
            }
        });
    }

    //开始点歌
    @Override
    public void orderStart(SongBean song) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingShow("正在点歌...");
            }
        });
    }

    //---------------------------------------------重写关机接口--------------------------------------

    //开始关机
    @Override
    public void powerStart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingShow("正在关机...");
            }
        });
    }

    //关机失败
    @Override
    public void powerFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showFail("关机失败");
            }
        });
    }

    //关机成功
    @Override
    public void powerSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //刷新题头 和 数据库 和列表
                powerOff();
                showSuccess("关机成功");

            }
        });
    }

    //---------------------------------------------重写关机接口--------------------------------------

    //开始发送音乐停止信息
    @Override
    public void stopStart() {
        LogUtil.e("音乐停止", "开始停止");
    }

    //发送停止失败
    @Override
    public void stopFail() {
        LogUtil.e("音乐停止", "停止失败");
    }

    //发送停止成功
    @Override
    public void stopSuccess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingHide();
                Toast.makeText(MainActivity.this, "当前音乐播放完毕", Toast.LENGTH_SHORT).show();
                powerOff();
            }
        });
    }


//    ----------------------------------------------------------------------------------------------

    //弱引用
    class UIhandler extends Handler {
        WeakReference<MainActivity> weakReference = null;


        public UIhandler(MainActivity mainActivity) {
            this.weakReference = new WeakReference<MainActivity>(mainActivity);
        }
    }

    //---------------------------------------------定时器部分--------------------------------------

    private void initAlarm() {
        i = new Intent(this, AlarmReceiver.class);
        i.setAction("STOPPLAYING");
        sender = PendingIntent.getBroadcast(this, 0, i, 0);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    //重新开始定时
    public void setAlarm() {
        LogUtil.e("setAlarm", "定时器设置成功歌曲将在 " + SONGSTOPTIME + "秒 后结束播放");
        if (am != null) {
            am.cancel(sender);
        }

        //5秒后发送广播
        assert am != null;
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , SONGSTOPTIME, sender);
    }



}

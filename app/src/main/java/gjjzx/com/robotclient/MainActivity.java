package gjjzx.com.robotclient;

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
import gjjzx.com.robotclient.util.LocalSQLUtil;
import gjjzx.com.robotclient.util.SPUtil;

public class MainActivity extends AppCompatActivity implements PowerOffDialog.PowerOffListener, SettingDialog.onSettingListener, LoginDialog.LoginSuccessListener, DeleteSongDialog.DeleteSongListener, AddSongDialog.onAddSongListener {

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

    //当前播放的音乐，切换音乐的时候更改
    private SongBean currentSong;


    class UIhandler extends Handler {
        WeakReference<MainActivity> weakReference;

        public UIhandler(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADDSONGSUCCESS:
                    showSuccess("添加歌曲成功");
                    break;
                case ADDSONGFAIL:
                    showFail("添加歌曲失败");
                    break;
                case WAITING:
                    waitingShow(msg.obj.toString());
                    break;
                case DELETEPLAYINGSONGFAIL:
                    showFail("无法删除正在播放的音乐");
                    break;
                case DELETESONGSUCCESS:
                    showSuccess("删除歌曲成功");
                    break;
                case DELETEERRORSONG:
                    showFail("要删除的歌曲不存在");
                    break;
                case DELETESONGFAIL:
                    showFail("删除歌曲失败");
                    break;

                default:
                    break;
            }
        }
    }

    //UIhandler对象
    private UIhandler ihandler;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //题头透明设置
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //视图绑定与监听设置
        findView();
        //recyclerview初始化设置
        initRecyclerView();
        //弹窗初始化
        initDialog();

    }

    //recyclerview初始化
    private void initRecyclerView() {
        songList = LocalSQLUtil.getNewSongList();

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvAdapter = new CardRvAdapter(this, songList);
        rvAdapter.setOnItemClickLitener(new CardRvAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
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
            ihandler.sendMessage(msg);
            addSongDialog.dismiss();
            try {
                //添加歌曲到数据库并返回最新列表
                songList = LocalSQLUtil.addSong(sb);
                //刷新列表
                rvAdapter.listRefresh(songList);
                ihandler.sendEmptyMessageDelayed(ADDSONGSUCCESS, 1000);
            } catch (Exception e) {
                ihandler.sendEmptyMessageDelayed(ADDSONGFAIL, 1000);
            }
        }
    }

    //删除歌曲按钮
    @Override
    public void onDeleteSong(String songName) {
        Message msg = new Message();
        msg.what = WAITING;
        msg.obj = "歌曲删除中...";
        ihandler.sendMessage(msg);
        deleteSongDialog.dismiss();
        if (currentSong != null)
            if (songName.equals(currentSong.getSongName())) {
                ihandler.sendEmptyMessageDelayed(DELETEPLAYINGSONGFAIL, 1000);
                return;
            }
        try {
            for (SongBean sb : songList) {
                if (sb.getSongName().equals(songName)) {
                    songList = LocalSQLUtil.delSong(sb.getSongCode());
                    rvAdapter.listRefresh(songList);
                    ihandler.sendEmptyMessageDelayed(DELETESONGSUCCESS, 1000);
                    return;
                }
            }
            ihandler.sendEmptyMessageDelayed(DELETEERRORSONG, 1000);
        } catch (Exception e) {
            ihandler.sendEmptyMessageDelayed(DELETESONGFAIL, 1000);
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


    //点歌成功的回调需要执行的——需要在主线程调用
    public void switchSongs(String songName) {

        titleText.setText(String.format("当前播放曲目：%s", songName));
    }


    //点歌（adapter调用）
    public void orderSong(SongBean songBean) {

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
        LemonBubble.showRight(this, str, 2000);
    }

    //视图绑定
    private void findView() {
        //UIhandler初始化
        ihandler = new UIhandler(this);
        //横向滚动recyclerview
        rv = (MyRecyclerView) findViewById(R.id.myrecyclerview);
        //左侧抽屉
        navigationView = (NavigationView) findViewById(R.id.navigation);
        //整个布局，为抽屉准备的
        drawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //题头
        titleText = (TextView) findViewById(R.id.title_text);
        //题头左侧按钮
        titleLeftBtn = (Button) findViewById(R.id.title_left_btn);
        //左侧抽屉题头
        View headerView = navigationView.getHeaderView(0);
        //左侧抽屉题头——头像
        headImage = headerView.findViewById(R.id.nav_head_image);
        //左侧抽屉题头——头像下文字
        headTv = headerView.findViewById(R.id.nav_head_tv);
        //左侧抽屉题头背景色
        headerView.setBackgroundColor(Color.parseColor("#40808080"));
        //左侧抽屉题头点击事件
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

        //标题文字点击事件
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //标题左侧按钮点击事件
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
        //所有歌曲都不在播放
        LocalSQLUtil.setNoSongPlaying();
    }


    //---------------------------------------双击退出函数-------------------------------------------------------
    @Override
    public void onBackPressed() {
        exitBy2Click();
    }


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

    //----------------------------------------------------------------------------------------------

    @Override
    public void onSetting(String ip, int port) {
        //存储数据，并断开连接
        SPUtil.saveDES(ip, port);
        //断开socket链接
//        sc.closeAll();
        //隐藏
        settingDialog.dismiss();
        //修改成功提示
        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPowerOff() {

    }


}

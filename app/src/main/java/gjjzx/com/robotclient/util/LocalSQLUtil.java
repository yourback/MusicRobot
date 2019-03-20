package gjjzx.com.robotclient.util;

import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.List;

import gjjzx.com.robotclient.R;
import gjjzx.com.robotclient.bean.SongBean;

/**
 * Created by PC on 2017/10/31.
 */

public class LocalSQLUtil {
    public static List<SongBean> addSong(SongBean sb) {
        sb.save();
        return DataSupport.findAll(SongBean.class);
    }

    public static List<SongBean> getNewSongList() {
        List<SongBean> list = DataSupport.findAll(SongBean.class);
        Log.e("getNewSongList", "之前：" + list);
        if (list.size() == 0) {
            addSong(new SongBean(R.mipmap.pan, "世上只有妈妈好", "1"));
            addSong(new SongBean(R.mipmap.pan, "女儿情", "2"));
            addSong(new SongBean(R.mipmap.pan, "茉莉花", "3"));
            addSong(new SongBean(R.mipmap.pan, "在水一方", "4"));
            addSong(new SongBean(R.mipmap.pan, "明天会更好", "5"));
            addSong(new SongBean(R.mipmap.pan, "让我们荡起双桨", "6"));
            addSong(new SongBean(R.mipmap.pan, "我的中国心", "7"));
            addSong(new SongBean(R.mipmap.pan, "生日快乐歌", "8"));
            addSong(new SongBean(R.mipmap.pan, "童年", "9"));
            addSong(new SongBean(R.mipmap.pan, "两只老虎", "10"));
            addSong(new SongBean(R.mipmap.pan, "歌唱祖国", "11"));
            addSong(new SongBean(R.mipmap.pan, "绒花", "12"));
            addSong(new SongBean(R.mipmap.pan, "少年壮志不言愁", "13"));
            addSong(new SongBean(R.mipmap.pan, "太阳出来喜洋洋", "14"));
            addSong(new SongBean(R.mipmap.pan, "滚滚长江东逝水", "15"));
            addSong(new SongBean(R.mipmap.pan, "敖包相会", "16"));
            addSong(new SongBean(R.mipmap.pan, "欢乐颂", "17"));
            addSong(new SongBean(R.mipmap.pan, "小星星", "18"));
            addSong(new SongBean(R.mipmap.pan, "沧海一声笑", "19"));
            addSong(new SongBean(R.mipmap.pan, "送别", "20"));
            addSong(new SongBean(R.mipmap.pan, "浏阳河", "21"));
        }
        Log.e("getNewSongList", "之后：" + list);
        list = DataSupport.findAll(SongBean.class);
//        LogUtil.e("LocalSQLUtil", "从本地数据库取出的数据：");
//        for (SongBean s : list) {
//            LogUtil.e("LocalSQLUtil", s.toString());
//        }
        return list;
    }

    public static List<SongBean> delSong(String code) {
        DataSupport.deleteAll(SongBean.class, "songCode = ?", code);
        return getNewSongList();
    }

    public static List<SongBean> setNoSongPlaying() {
        Log.e("LocalSQLUtil", "置全部歌曲都不播放");
        SongBean s = new SongBean();
        s.setToDefault("isPlaying");
        s.updateAll();
        return getNewSongList();
    }

    public static boolean isSongExist(String columeName, String columeValue) {
        List<SongBean> sl = DataSupport.where(columeName + " = ?", columeValue).find(SongBean.class);
        return sl.size() != 0;
    }

    public static List<SongBean> setSongPlaying(String str) {
        Log.e("LocalSQLUtil", "设置歌曲代码为：" + str + "歌曲播放");
        //设置其他歌曲都不播放
        setNoSongPlaying();
        SongBean s = new SongBean();
        s.setPlaying(true);
        s.updateAll("songCode = ?", str);
        return getNewSongList();
    }


    public static SongBean getSongBeanFromCode(String code) {
        List<SongBean> list = DataSupport.where("songCode = ?", code).find(SongBean.class);
        return list.get(0);
    }
}

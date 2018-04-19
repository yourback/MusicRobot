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
        if (list.size() == 0) {
            addSong(new SongBean(R.mipmap.pan, "月光", "0"));
            addSong(new SongBean(R.mipmap.pan, "命运", "1"));
            addSong(new SongBean(R.mipmap.pan, "夜曲", "2"));
            addSong(new SongBean(R.mipmap.pan, "极乐净土", "3"));
            addSong(new SongBean(R.mipmap.pan, "白夜", "4"));
            addSong(new SongBean(R.mipmap.pan, "是非题", "5"));
            addSong(new SongBean(R.mipmap.pan, "花香", "6"));
            addSong(new SongBean(R.mipmap.pan, "海阔天空", "7"));
            addSong(new SongBean(R.mipmap.pan, "童话镇", "8"));
            addSong(new SongBean(R.mipmap.pan, "我的天空", "9"));
        }
        list = DataSupport.findAll(SongBean.class);
        LogUtil.e("LocalSQLUtil", "从本地数据库取出的数据：");
        for (SongBean s : list) {
            LogUtil.e("LocalSQLUtil", s.toString());
        }
        return list;
    }

    public static List<SongBean> delSong(String code) {
        DataSupport.deleteAll(SongBean.class, "songCode = ?", code);
        return DataSupport.findAll(SongBean.class);
    }

    public static List<SongBean> setNoSongPlaying() {
        Log.e("LocalSQLUtil", "置全部歌曲都不播放");
        SongBean s = new SongBean();
        s.setToDefault("isPlaying");
        s.updateAll();
        return DataSupport.findAll(SongBean.class);
    }

    public static boolean isSongExist(String columeName, String columeValue) {
        List<SongBean> sl = DataSupport.where(columeName + " = ?", columeValue).find(SongBean.class);
        return sl.size() != 0;
    }

    public static List<SongBean> setSongPlaying(String str) {
        Log.e("LocalSQLUtil", "设置歌曲代码为：" + str + "歌曲播放");
        SongBean s = new SongBean();
        s.setPlaying(true);
        s.updateAll("songCode = ?", str);
        return getNewSongList();
    }
}

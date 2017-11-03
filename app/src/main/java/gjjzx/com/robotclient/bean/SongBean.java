package gjjzx.com.robotclient.bean;


import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

public class SongBean extends DataSupport implements Parcelable {
    private int songPic;
    private boolean isPlaying;
    private String songName;
    private String songCode;

    public SongBean(int songPic, String songName, String songCode) {
        this.isPlaying = false;
        this.songPic = songPic;
        this.songName = songName;
        this.songCode = songCode;
    }

    public SongBean() {
    }

    protected SongBean(Parcel in) {
        songPic = in.readInt();
        isPlaying = in.readByte() != 0;
        songName = in.readString();
        songCode = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        SongBean temp = (SongBean) obj;
        if (temp.getSongName().equals(songName) && temp.getSongCode().equals(songCode))
            return true;
        return false;
    }

    public static final Creator<SongBean> CREATOR = new Creator<SongBean>() {
        @Override
        public SongBean createFromParcel(Parcel in) {
            return new SongBean(in);
        }

        @Override
        public SongBean[] newArray(int size) {
            return new SongBean[size];
        }
    };

    public int getSongPic() {
        return songPic;
    }

    public void setSongPic(int songPic) {
        this.songPic = songPic;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongCode() {
        return songCode;
    }

    public void setSongCode(String songCode) {
        this.songCode = songCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(songPic);
        parcel.writeByte((byte) (isPlaying ? 1 : 0));
        parcel.writeString(songName);
        parcel.writeString(songCode);
    }

    @Override
    public String toString() {
        return "歌曲名：" + getSongName() + "|歌曲代码：" + getSongCode() + "|是否在播放：" + isPlaying;
    }
}

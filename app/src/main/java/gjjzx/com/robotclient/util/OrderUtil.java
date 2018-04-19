package gjjzx.com.robotclient.util;

/**
 * 此类用户命令转换
 * 1、开机
 * 2、关机
 * 3、点歌操作
 */


public class OrderUtil {

    // 开机
    public static String boot() {
        /**
         * 开机发送的消息：
         * 0xEE 0xEE ID=0x03 MusicID=0x00 SW=0x01 CRC=~(ID+MusicID+SW)&0xFF
         */
        return int2Order(3, 0, 1);
    }


    //关机
    public static String shutDown() {
        /**
         * 关机发送的消息：
         * 0xEE 0xEE ID=0x03 MusicID=0x00 SW=0x00 CRC=~(ID+MusicID+SW)&0xFF
         */
        return int2Order(3, 0, 0);
    }

    // 切歌
    public static String switchSong(String musicid) {
        /**
         * 关机发送的消息：
         * 0xEE 0xEE ID=0x01 MusicID=musicid SW=0x01 CRC=~(ID+MusicID+SW)&0xFF
         */
        return int2Order(1, Integer.parseInt(musicid), 1);
    }

    // 主动断开连接
    public static String breakConn() {
        return int2Hex(0xEE) + " "
                + int2Hex(0xEE) + " "
                + int2Hex(5);
    }

    private static String int2Order(int id, int musicid, int sw) {
        String s = int2Hex(0xEE) + " "
                + int2Hex(0xEE) + " "
                + int2Hex(id) + " "
                + int2Hex(musicid) + " "
                + int2Hex(sw) + " "
                + getCRC(id, musicid, sw);
        return s.toUpperCase();
    }

    private static String getCRC(int mid, int mmusicid, int msw) {
        int i = (~(mid + mmusicid + msw)) & 0xff;
        String s = int2Hex(i);
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }

    private static String int2Hex(int i) {
        String s = Integer.toHexString(i);
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }
}

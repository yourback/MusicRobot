package gjjzx.com.robotclient.bean;

/**
 * Created by PC on 2018/4/17.
 */

public class DesInfo {
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DesInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}

package ja;

public class User {

    private String ip;
    private String name = null;

    private long lastNotification;

    public User(String ip, String name) {
        this.ip = ip;
        this.name = name;
        lastNotification = System.currentTimeMillis();
    }

    public long getLastNotification() {
        return lastNotification;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

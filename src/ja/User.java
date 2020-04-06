package ja;

class User {

    private String ip;
    private String name;

    private long lastNotification;

    User(String ip, String name) {
        this.ip = ip;
        this.name = name;
        lastNotification = System.currentTimeMillis();
    }

    long getLastNotification() {
        return lastNotification;
    }

    String getIp() {
        return ip;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}

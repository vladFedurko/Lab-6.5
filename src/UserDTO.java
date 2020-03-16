import java.util.Date;

public class UserDTO {

    private String ip;
    private String name;
    private Date lastCheck;

    public UserDTO(String ip, String name, Date lastCheck) {
        this.ip = ip;
        this.name = name;
        this.lastCheck = lastCheck;
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

    public Date getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    public boolean equals(Object obj) {
        return obj instanceof UserDTO && ((UserDTO) obj).ip != null && ((UserDTO) obj).ip.equals(this.ip);
    }
}

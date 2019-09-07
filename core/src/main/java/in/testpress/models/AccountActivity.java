package in.testpress.models;

public class AccountActivity {

    private Integer id;
    private String userAgent;
    private String ipAddress;
    private String lastUsed;
    private String location;
    private String currentDevice;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCurrentDevice() {
        return currentDevice;
    }

    public Boolean isCurrentDevice() {
        return Boolean.parseBoolean(currentDevice);
    }

    public void setCurrentDevice(String currentDevice) {
        this.currentDevice = currentDevice;
    }

}

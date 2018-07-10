package in.testpress.exam.models;

public class Permission {

    private boolean hasPermission;
    private String nextRetakeTime;

    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public String getNextRetakeTime() {
        return nextRetakeTime;
    }

    public void setNextRetakeTime(String nextRetakeTime) {
        this.nextRetakeTime = nextRetakeTime;
    }

}
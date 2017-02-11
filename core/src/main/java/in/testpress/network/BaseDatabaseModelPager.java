package in.testpress.network;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

public abstract class BaseDatabaseModelPager<T> extends BaseResourcePager<T> {

    protected String latestModifiedDate;
    protected SimpleDateFormat simpleDateFormat;

    @SuppressLint("SimpleDateFormat")
    public BaseDatabaseModelPager() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public void setLatestModifiedDate(String latestModifiedDate) {
        this.latestModifiedDate = latestModifiedDate;
    }
}

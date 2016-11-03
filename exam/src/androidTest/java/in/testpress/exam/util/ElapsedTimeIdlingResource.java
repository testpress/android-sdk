package in.testpress.exam.util;

import android.support.test.espresso.IdlingResource;

public class ElapsedTimeIdlingResource implements IdlingResource {
    private final long mStartTime;
    private final long mWaitingTime;
    private ResourceCallback mResourceCallback;

    public ElapsedTimeIdlingResource(long waitingTime) {
        this.mStartTime = System.currentTimeMillis();
        this.mWaitingTime = waitingTime;
    }

    @Override
    public String getName() {
        return ElapsedTimeIdlingResource.class.getName() + ":" + mWaitingTime;
    }

    @Override
    public boolean isIdleNow() {
        long elapsedTime = System.currentTimeMillis() - mStartTime;
        boolean elapsed = (elapsedTime >= mWaitingTime);
        if (elapsed) {
            mResourceCallback.onTransitionToIdle();
        }
        return elapsed;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.mResourceCallback = resourceCallback;
    }
}
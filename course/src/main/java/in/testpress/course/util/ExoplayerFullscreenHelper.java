package in.testpress.course.util;

import android.content.Context;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.view.OrientationEventListener;

public class ExoplayerFullscreenHelper {

    private OrientationEventListener mOrientationListener;
    public boolean misLandscape;
    private ExoPlayerUtil mexoPlayerUtil;
    private Context mcontext;

    public ExoplayerFullscreenHelper(Context context){
        mcontext = context;
    }

    public ExoplayerFullscreenHelper(Context context, ExoPlayerUtil playerUtil){
        mcontext = context;
        mexoPlayerUtil = playerUtil;
    }

    public void initializeOrientationListener() {
        mOrientationListener = new OrientationEventListener(mcontext,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {

                // Check does user have turned ON or OFF  the auto rotation
                boolean isAutoRotationIsON = (android.provider.Settings.System.getInt(mcontext.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0) == 1);

                if (mexoPlayerUtil != null && isAutoRotationIsON) {
                    boolean isLandscape = misLandscape;

                    if ((orientation > 0 && orientation < 20) || (orientation > 170 && orientation < 190)) {
                        isLandscape = false;
                    } else if ((orientation > 80 && orientation < 110) || (orientation > 220) && orientation < 270) {
                        isLandscape = true;
                    }

                    if (isLandscape != misLandscape) {
                        misLandscape = isLandscape;
                        mexoPlayerUtil.onOrientationChange(misLandscape);
                    }
                }
            }
        };

        if (mOrientationListener.canDetectOrientation() == true) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
    }

    public void setExoplayerUtil(ExoPlayerUtil playerUtil){
        mexoPlayerUtil = playerUtil;
    }

    public void disableOrientationListener(){

        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
    }
}

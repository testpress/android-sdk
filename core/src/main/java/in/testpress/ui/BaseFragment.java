package in.testpress.ui;

import android.support.v4.app.Fragment;

import in.testpress.network.RetrofitCall;
import in.testpress.util.CommonUtils;

public abstract class BaseFragment extends Fragment {

    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {};
    }

    @Override
    public void onDestroyView() {
        CommonUtils.cancelAPIRequests(getRetrofitCalls());
        super.onDestroyView();
    }

}

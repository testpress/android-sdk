package in.testpress.exam.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.testpress.exam.R;

public class ExamsListFragment extends Fragment {

    public static void show(Activity activity, int containerViewId) {
        activity.getFragmentManager().beginTransaction()
                .replace(containerViewId, new ExamsListFragment())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exams_list, container, false);
    }

}

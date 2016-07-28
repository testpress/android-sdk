package in.testpress.exam.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import in.testpress.core.TestpressAuthToken;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;

public class AuthenticateFragment extends Fragment {

    public static final String BASE_URL = "baseUrl";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    LinearLayout emptyView;
    TextView emptyTitleView;
    TextView emptyDescView;
    Button retryButton;
    int containerViewId;

    public static void show(Activity activity, int containerViewId, String baseUrl, String username,
                            String password) {
        AuthenticateFragment fragment = new AuthenticateFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BASE_URL, baseUrl);
        bundle.putString(USER_NAME, username);
        bundle.putString(PASSWORD, password);
        fragment.setArguments(bundle);
        activity.getFragmentManager().beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        containerViewId = container.getId();
        return inflater.inflate(R.layout.fragment_testpress_exam, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyView = (LinearLayout) view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
        if (getArguments() != null) {
            authenticate();
        }
    }

    private void authenticate() {
        TestpressSdk.initialize(getActivity(), getArguments().getString(BASE_URL),
                getArguments().getString(USER_NAME), getArguments().getString(PASSWORD),
                new TestpressCallback<TestpressAuthToken>() {
                    @Override
                    public void onSuccess(TestpressAuthToken response) {
                        ExamsListFragment.show(getActivity(), containerViewId);
                    }

                    @Override
                    public void onException(Exception e) {
                        if (e.getCause() instanceof IOException) {
                            setEmptyText(R.string.network_error, R.string.no_internet_try_again);
                        } else {
                            setEmptyText(R.string.loading_failed, R.string.some_thing_went_wrong_try_again);
                        }
                    }
                });
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
    }
}

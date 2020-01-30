package in.testpress.exam.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Exam;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.util.TextWatcherAdapter;
import in.testpress.util.UIUtils;

import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class AccessCodeFragment extends BaseFragment {

    private ProgressDialog progressDialog;
    private EditText accessCodeView;
    private View view;
    private RetrofitCall<TestpressApiResponse<Exam>> examsApiRequest;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new AccessCodeFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.testpress_access_code_layout, container, false);
        TextView enterCodeLabel = (TextView) view.findViewById(R.id.enter_code_label);
        enterCodeLabel.setTypeface(TestpressSdk.getRubikRegularFont(getActivity()));
        accessCodeView = (EditText) view.findViewById(R.id.access_code);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getActivity(), progressDialog, 4);
        final Button submitButton = (Button) view.findViewById(R.id.get_exams);
        submitButton.setTypeface(TestpressSdk.getRubikMediumFont(getActivity()));
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadExams();
            }
        });
        accessCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && submitButton.isEnabled()) {
                    loadExams();
                    return true;
                }
                return false;
            }
        });
        accessCodeView.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitButton.setEnabled(accessCodeView.getText().toString().trim().length() > 0);
            }
        });
        accessCodeView.requestFocus();
        return view;
    }

    void loadExams() {
        progressDialog.show();
        UIUtils.hideSoftKeyboard(getActivity());
        final String accessCode = accessCodeView.getText().toString().trim();
        Map<String, Object> queryParams = new HashMap<>();
        examsApiRequest = new TestpressExamApiClient(getContext()).getExams(accessCode, queryParams)
                .enqueue(new TestpressCallback<TestpressApiResponse<Exam>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Exam> response) {
                        if (getActivity() == null) {
                            return;
                        }

                        progressDialog.dismiss();
                        startActivity(AccessCodeExamsActivity.getIntent(getActivity(), accessCode,
                                response.getResults()));
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        exception.printStackTrace();
                        progressDialog.dismiss();
                        if (exception.isNetworkError()) {
                            Snackbar.make(view, R.string.testpress_no_internet_connection,
                                    LENGTH_SHORT).show();

                        } else if (exception.isClientError()) {
                            accessCodeView.setError(getString(R.string.testpress_invalid_access_code));
                        } else {
                            Snackbar.make(view, R.string.testpress_some_thing_went_wrong_try_again,
                                    LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { examsApiRequest };
    }

}

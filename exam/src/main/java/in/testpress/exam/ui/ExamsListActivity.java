package in.testpress.exam.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import in.testpress.exam.R;

public class  ExamsListActivity extends BaseToolBarActivity {
    public static final String CREDENTIALS = "credentials";

    public static Intent getNewIntent(Context context, String baseUrl, String username, String password) {
        Bundle bundle = new Bundle();
        bundle.putString(AuthenticateFragment.BASE_URL, baseUrl);
        bundle.putString(AuthenticateFragment.USER_NAME, username);
        bundle.putString(AuthenticateFragment.PASSWORD, password);
        return new Intent(context, ExamsListActivity.class).putExtra(CREDENTIALS, bundle);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_with_tool_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getBundleExtra(CREDENTIALS) != null) {
            // Need to authenticate
            AuthenticateFragment fragment = new AuthenticateFragment();
            fragment.setArguments(getIntent().getBundleExtra(CREDENTIALS));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return;
        }
        CarouselFragment fragment = new CarouselFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}

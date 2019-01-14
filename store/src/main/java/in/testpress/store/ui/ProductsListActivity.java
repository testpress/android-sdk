package in.testpress.store.ui;

import android.content.Intent;
import android.os.Bundle;

import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.store.TestpressStore.CONTINUE_PURCHASE;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductsListActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ProductListFragment.show(this, R.id.fragment_container);

        if (getIntent().getStringExtra("title") != "" && getIntent().getStringExtra("title") != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == STORE_REQUEST_CODE && resultCode == RESULT_OK
                && data != null && !data.getBooleanExtra(CONTINUE_PURCHASE, false)) {

            setResult(RESULT_OK, data);
            finish();
        }
    }

}

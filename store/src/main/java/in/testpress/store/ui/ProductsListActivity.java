package in.testpress.store.ui;

import android.os.Bundle;

import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

public class ProductsListActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout);
        ProductListFragment.show(this, R.id.fragment_container);
    }

}

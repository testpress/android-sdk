package in.testpress.samples.store;

import android.content.Intent;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.database.entities.ProductLiteEntity;
import in.testpress.samples.BaseNavigationDrawerActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.store.TestpressStore;
import in.testpress.store.ui.ProductListFragmentV2;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class NavigationDrawerActivity extends BaseNavigationDrawerActivity {

    @Override
    protected int getNavigationViewMenu() {
        return R.menu.store_drawer_items;
    }

    @Override
    protected void onDrawerItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.store:
                showSDK(R.id.store);
                break;
            case R.id.products:
                showSDK(R.id.products);
                break;
        }
        super.onDrawerItemSelected(menuItem);
    }

    private void showSDK(int position) {
        selectedItem = position;
        if (TestpressSdk.hasActiveSession(this)) {
            if (position == R.id.store) {
                TestpressSession session = TestpressSdk.getTestpressSession(this);
                //noinspection ConstantConditions
                session.getInstituteSettings().setAccessCodeEnabled(true);
                TestpressSdk.setTestpressSession(this, session);
                TestpressStore.show(this, R.id.fragment_container, session);
            } else if (position == R.id.products) {
                TestpressSession session = TestpressSdk.getTestpressSession(this);
                //noinspection ConstantConditions
                session.getInstituteSettings().setAccessCodeEnabled(true);
                TestpressSdk.setTestpressSession(this, session);
                ProductListFragmentV2.Companion.show(this, R.id.fragment_container, new ProductListFragmentV2.OnProductClickListener() {
                    @Override
                    public void onClick(@NonNull ProductLiteEntity product) {
                        NavigationDrawerActivity.this.startActivity(
                                CoursePreviewActivity.Companion.createIntent(
                                        new ArrayList<>(product.getCourseIds()),
                                        NavigationDrawerActivity.this,
                                        product.getSlug()
                                )
                        );
                    }
                });
            }
        } else {
            Intent intent = new Intent(this, TestpressCoreSampleActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            showSDK(selectedItem);
            logoutMenu.setVisible(true);
        }
    }

}

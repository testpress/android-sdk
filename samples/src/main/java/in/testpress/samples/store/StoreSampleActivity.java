package in.testpress.samples.store;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.samples.BaseToolBarActivity;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;
import in.testpress.samples.util.ViewUtils;
import in.testpress.store.TestpressStore;
import in.testpress.util.Assert;

import static in.testpress.samples.core.TestpressCoreSampleActivity.AUTHENTICATE_REQUEST_CODE;

public class StoreSampleActivity extends BaseToolBarActivity {

    private int selectedItem;
    private String productSlug;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.new_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSDK(R.id.new_activity_button);
            }
        });
        findViewById(R.id.show_product).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExamSlug(R.id.show_product);
            }
        });
        findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreSampleActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showSDK(int clickedButtonId) {
        selectedItem = clickedButtonId;
        if (TestpressSdk.hasActiveSession(this)) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            Assert.assertNotNull("TestpressSession must not be null.", session);
            switch (clickedButtonId) {
                case R.id.store:
                    TestpressStore.show(this, session);
                    break;
                case R.id.show_product:
                    TestpressStore.showProduct(this, productSlug, session);
                    break;
                default:
                    TestpressStore.show(this, session);
                    break;
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
        }
    }

    void getExamSlug(final int clickedButtonId) {
        ViewUtils.showInputDialogBox(this, "Enter Product Slug",
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String inputText) {
                        productSlug = inputText;
                        showSDK(clickedButtonId);
                    }
                });
    }

}

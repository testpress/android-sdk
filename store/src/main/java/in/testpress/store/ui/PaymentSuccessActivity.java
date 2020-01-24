package in.testpress.store.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import in.testpress.store.R;
import in.testpress.store.models.Order;
import in.testpress.store.models.Product;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.store.TestpressStore.CONTINUE_PURCHASE;
import static in.testpress.store.TestpressStore.PAYMENT_SUCCESS;
import static in.testpress.store.ui.OrderConfirmActivity.ORDER;
import static in.testpress.store.ui.ProductDetailsActivity.PRODUCT;

public class PaymentSuccessActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_payment_success);
        TextView orderId = (TextView) findViewById(R.id.order_id);
        TextView amount = (TextView) findViewById(R.id.amount);
        TextView bookMessage = (TextView) findViewById(R.id.book_message);
        TextView furtherDetails = (TextView) findViewById(R.id.further_details);

        Order order = getIntent().getParcelableExtra(ORDER);
        orderId.setText(getString(R.string.testpress_order_id, order.getOrderId()));
        amount.setText(getString(R.string.testpress_amount, order.getAmount()));
        Product product = getIntent().getParcelableExtra(PRODUCT);
        if(!product.getTypes().contains("Books")) {
            bookMessage.setVisibility(View.GONE);
        }
        furtherDetails.setText(getString(R.string.testpress_check_your_mail, order.getEmail()));
        Button gotoHomeButton = (Button) findViewById(R.id.home_button);
        gotoHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuePurchase(false);
            }
        });
        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuePurchase(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        continuePurchase(false);
    }

    private void continuePurchase(boolean continuePurchase) {
        Intent intent = new Intent();
        intent.putExtra(CONTINUE_PURCHASE, continuePurchase);
        intent.putExtra(PAYMENT_SUCCESS, true);
        setResult(RESULT_OK, intent);
        finish();
    }
}

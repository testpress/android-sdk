package in.testpress.store.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.store.R;
import in.testpress.store.models.Order;
import in.testpress.store.models.OrderConfirmErrorDetails;
import in.testpress.store.models.OrderItem;
import in.testpress.store.models.Product;
import in.testpress.store.network.TestpressStoreApiClient;
import in.testpress.store.payu.PaymentModeActivity;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.TextWatcherAdapter;
import in.testpress.util.UIUtils;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;
import static in.testpress.store.network.TestpressStoreApiClient.URL_PAYMENT_RESPONSE_HANDLER;
import static in.testpress.store.ui.ProductDetailsActivity.PRODUCT;

public class OrderConfirmActivity extends BaseToolBarActivity {

    public static final String ORDER = "order";

    private EditText address;
    private EditText zip;
    private EditText landmark;
    private EditText phone;
    private TextView fillAllDetailsText;
    private Button continueButton;
    private ProgressBar progressBar;
    private RelativeLayout shippingDetails;
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private TextWatcher watcher = validationTextWatcher();
    private Product product;
    private List<OrderItem> orderItems;
    private Order order;
    private OrderItem orderItem = new OrderItem();
    private TestpressStoreApiClient apiClient;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.testpress_shipping_address);
        address = (EditText) findViewById(R.id.address);
        zip = (EditText) findViewById(R.id.zip);
        landmark = (EditText) findViewById(R.id.landmark);
        phone = (EditText) findViewById(R.id.phone);
        fillAllDetailsText = (TextView) findViewById(R.id.fill_all_details);
        continueButton = (Button) findViewById(R.id.continue_button);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        shippingDetails = (RelativeLayout) findViewById(R.id.shipping_details);
        shippingDetails.setVisibility(View.GONE);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrder();
            }
        });
        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);

        product = getIntent().getParcelableExtra(PRODUCT);
        orderItem.setProduct(product.getUrl());
        orderItem.setQuantity(1);
        orderItem.setPrice(product.getPrice());
        orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        apiClient = new TestpressStoreApiClient(this);
        order();
    }

    void order() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        apiClient.order(orderItems).enqueue(new TestpressCallback<Order>() {
            @Override
            public void onSuccess(Order createdOrder) {
                order = createdOrder;
                progressBar.setVisibility(View.GONE);
                if (createdOrder.getStatus().equals("Completed")) {
                    showPaymentStatus();
                } else if(product.getRequiresShipping()) {
                    landmark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        public boolean onEditorAction(final TextView v, final int actionId,
                                                      final KeyEvent event) {

                            if (actionId == IME_ACTION_DONE && continueButton.isEnabled()) {
                                confirmOrder();
                                return true;
                            }
                            return false;
                        }
                    });
                    phone.setText(order.getPhone());
                    address.addTextChangedListener(watcher);
                    zip.addTextChangedListener(watcher);
                    phone.addTextChangedListener(watcher);
                    shippingDetails.setVisibility(View.VISIBLE);
                } else {
                    confirmOrder();
                }
            }

            @Override
            public void onException(TestpressException exception) {
                if (exception.isNetworkError()) {
                    setEmptyText(R.string.testpress_network_error,
                            R.string.testpress_no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);

                    retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            order();
                        }
                    });
                    retryButton.setVisibility(View.VISIBLE);
                } else {
                    setEmptyText(R.string.testpress_error_ordering,
                            R.string.testpress_some_thing_went_wrong_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                }
            }
        });
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(address) && populated(zip) && populated(phone);
        if(populated) {
            fillAllDetailsText.setVisibility(View.GONE);
            continueButton.setEnabled(true);
        } else {
            fillAllDetailsText.setVisibility(View.VISIBLE);
            continueButton.setEnabled(false);
        }
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    private void confirmOrder() {
        progressBar.setVisibility(View.VISIBLE);
        shippingDetails.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        UIUtils.hideSoftKeyboard(this);
        order.setShippingAddress(address.getText().toString().trim());
        order.setPhone(phone.getText().toString().trim());
        order.setZip(zip.getText().toString().trim());
        order.setLandMark(landmark.getText().toString().trim());
        apiClient.orderConfirm(order)
                .enqueue(new TestpressCallback<Order>() {
                    @Override
                    public void onSuccess(Order order) {
                        progressBar.setVisibility(View.GONE);

                        //noinspection ConstantConditions
                        String redirectUrl = TestpressSdk.getTestpressSession(OrderConfirmActivity.this)
                                .getInstituteSettings().getBaseUrl() + URL_PAYMENT_RESPONSE_HANDLER;

                        PaymentParams paymentParams = new PaymentParams();
                        paymentParams.setKey(order.getApikey());
                        paymentParams.setTxnId(order.getOrderId());
                        paymentParams.setAmount(order.getAmount());
                        paymentParams.setProductInfo(order.getProductInfo());
                        paymentParams.setFirstName(order.getName());
                        paymentParams.setEmail(order.getEmail());
                        paymentParams.setUdf1("");
                        paymentParams.setUdf2("");
                        paymentParams.setUdf3("");
                        paymentParams.setUdf4("");
                        paymentParams.setUdf5("");
                        paymentParams.setSurl(redirectUrl);
                        paymentParams.setFurl(redirectUrl);

                        PayuConfig payuConfig = new PayuConfig();
                        payuConfig.setEnvironment(PayuConstants.PRODUCTION_ENV);

                        PayuHashes payuHashes = new PayuHashes();
                        payuHashes.setPaymentHash(order.getChecksum());
                        paymentParams.setHash(payuHashes.getPaymentHash());
                        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(order.getMobileSdkHash());

                        Intent intent = new Intent(OrderConfirmActivity.this, PaymentModeActivity.class);
                        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                        intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
                        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
                        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.ic_error_outline_black_18dp);

                            retryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    confirmOrder();
                                }
                            });
                            retryButton.setVisibility(View.VISIBLE);
                        } else if (exception.isClientError()) {
                            OrderConfirmErrorDetails errorDetails = exception.getErrorBodyAs(
                                    exception.getResponse(), OrderConfirmErrorDetails.class);

                            if(!errorDetails.getShippingAddress().isEmpty()) {
                                address.setError(errorDetails.getShippingAddress().get(0));
                                address.requestFocus();
                            }
                            if(!errorDetails.getZip().isEmpty()) {
                                zip.setError(errorDetails.getZip().get(0));
                                zip.requestFocus();
                            }
                            if(!errorDetails.getPhone().isEmpty()) {
                                phone.setError(errorDetails.getPhone().get(0));
                                phone.requestFocus();
                            }
                            if(!errorDetails.getLandMark().isEmpty()) {
                                landmark.setError(errorDetails.getLandMark().get(0));
                                landmark.requestFocus();
                            }
                        } else {
                            setEmptyText(R.string.testpress_error_confirm_order,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        }
                    }
                });
    }

    void showPaymentStatus() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra(ORDER, order);
        //noinspection ConstantConditions
        intent.putExtras(getIntent().getExtras());
        startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE && resultCode == RESULT_OK) {
            showPaymentStatus();
        } else {
            // requestCode = PAYU_REQUEST_CODE & payment failed, RESULT_CANCELED
            // or
            // requestCode = STORE_REQUEST_CODE & payment success, RESULT_OK
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel_order)
                .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.testpress_no, null)
                .show();
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
    }
}

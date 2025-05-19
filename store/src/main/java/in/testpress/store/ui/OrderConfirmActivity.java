package in.testpress.store.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.store.PaymentGatewayFactory;
import in.testpress.store.PaymentGateway;
import in.testpress.store.PaymentGatewayListener;
import in.testpress.store.R;
import in.testpress.store.TestpressStore;
import in.testpress.store.models.NetworkOrderStatus;
import in.testpress.store.models.Order;
import in.testpress.store.models.OrderConfirmErrorDetails;
import in.testpress.store.models.OrderItem;
import in.testpress.store.models.Product;
import in.testpress.store.network.StoreApiClient;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.FBEventsTrackerFacade;
import in.testpress.util.TextWatcherAdapter;
import in.testpress.util.UIUtils;

import com.razorpay.PaymentResultListener;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static in.testpress.store.TestpressStore.PAYMENT_SUCCESS;
import static in.testpress.store.TestpressStore.PAYMENT_FAILURE;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;
import static in.testpress.store.ui.ProductDetailsActivity.PRICE_ID;
import static in.testpress.store.ui.ProductDetailsActivity.PRODUCT;


public class OrderConfirmActivity extends BaseToolBarActivity implements PaymentGatewayListener, PaymentResultListener {

    public static final String ORDER = "order";
    private static final String TAG = "OrderConfirmActivity";

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
    private int priceId;
    private List<OrderItem> orderItems;
    public Order order;
    private OrderItem orderItem = new OrderItem();
    private StoreApiClient apiClient;
    private FBEventsTrackerFacade fbEventsLogger;
    private EventsTrackerFacade eventsTrackerFacade;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.testpress_shipping_address);
        order = getIntent().getParcelableExtra(ORDER);
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
        priceId = getPriceId();
        orderItem.setProduct(product.getSlug());
        orderItem.setQuantity(1);
        orderItem.setPrice(String.valueOf(priceId));
        orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        apiClient = new StoreApiClient(this);
        eventsTrackerFacade = new EventsTrackerFacade(getApplicationContext());
        if (order == null){
            order();
        } else {
            // If the user has already applied a coupon, the order was created on the
            // Product Details screen (previous screen), so we proceed directly with
            // order confirmation.
            confirmOrder();
        }
    }

    private int getPriceId() {
        try {
            return getIntent().getIntExtra(PRICE_ID, product.getPrices().get(0).getId());
        } catch (Exception e) {
            return -1;
        }
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
                    logEvent(EventsTrackerFacade.PAYMENT_SUCCESS);
                    showPaymentSuccessScreen();
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
                    public void onSuccess(final Order confirmedOrder) {
                        progressBar.setVisibility(View.GONE);
                        order.setOrderId(confirmedOrder.getOrderId()); // orderId for Razorpay order gets assigned by Razorpay on confirming
                        PaymentGateway paymentGateway = new PaymentGatewayFactory().create(confirmedOrder, OrderConfirmActivity.this);
                        paymentGateway.setPaymentGatewayListener(OrderConfirmActivity.this);
                        paymentGateway.showPaymentPage();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        logEvent(EventsTrackerFacade.PAYMENT_FAILURE);
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

    public void showPaymentSuccessScreen() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtras(getIntent().getExtras());
        intent.putExtra(ORDER, order);
        //noinspection ConstantConditions
        startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            if (requestCode == STORE_REQUEST_CODE && data.getBooleanExtra(PAYMENT_SUCCESS, false) || data.getBooleanExtra(PAYMENT_FAILURE, false)){
                setResult(resultCode, data);
                finish();
            }
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
        refreshOrderStatus(false);
        new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel_order)
                .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logEvent(EventsTrackerFacade.CANCELLED_PAYMENT);
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.testpress_no, null)
                .show();
    }

    private void logEvent(String eventName) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("product_name", product.getTitle());
        params.put("product_id", product.getId());
        params.put("order_id", order.getOrderId());
        params.put("email", order.getEmail());
        eventsTrackerFacade.logEvent(eventName, params);
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
    }

    private void refreshOrderStatus(Boolean reconciliation) {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.refreshOrderStatus(order.getOrderId(), reconciliation).enqueue(new TestpressCallback<NetworkOrderStatus>() {
            @Override
            public void onSuccess(NetworkOrderStatus result) {
                progressBar.setVisibility(View.GONE);
                if (result.getStatus().equals("Completed")) {
                    logEvent(EventsTrackerFacade.PAYMENT_SUCCESS);
                    showPaymentSuccessScreen();
                } else {
                    showPaymentFailedScreen();
                }
            }

            @Override
            public void onException(TestpressException exception) {
                progressBar.setVisibility(View.GONE);
                showPaymentFailedScreen();
            }
        });
    }

    @Override
    public void onPaymentSuccess() {
        refreshOrderStatus(false);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        refreshOrderStatus(false);
    }

    public void showPaymentFailedScreen() {
        Intent intent = new Intent(this, PaymentFailureActivity.class);
        startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    @Override
    public void onPaymentFailure() {
        progressBar.setVisibility(View.VISIBLE);
        refreshOrderStatus(false);
    }

    @Override
    public void onPaymentError(String errorMessage) {
        logEvent(EventsTrackerFacade.PAYMENT_FAILURE);
        progressBar.setVisibility(View.GONE);
        showPaymentFailedScreen();
    }

    @Override
    public void onPaymentError(int razorpayErrorCode, String razorpayErrorResponse) {
        progressBar.setVisibility(View.VISIBLE);
        refreshOrderStatus(true);
    }

    @Override
    public void onPaymentCancel() {
        logEvent(EventsTrackerFacade.CANCELLED_PAYMENT);
        progressBar.setVisibility(View.GONE);
        showPaymentFailedScreen();
    }
}

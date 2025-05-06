package in.testpress.store.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.models.InstituteSettings;
import in.testpress.store.R;
import in.testpress.store.models.Order;
import in.testpress.store.models.OrderItem;
import in.testpress.store.models.Product;
import in.testpress.store.network.StoreApiClient;
import in.testpress.store.util.UtilKt;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.ImageUtils;
import in.testpress.util.UILImageGetter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;
import io.sentry.Scope;
import io.sentry.ScopeCallback;
import io.sentry.Sentry;

import static in.testpress.store.TestpressStore.PAYMENT_SUCCESS;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class ProductDetailsActivity extends BaseToolBarActivity {

    public static final String PRODUCT_SLUG = "productSlug";
    public static final String PRODUCT = "product";
    public static final String ORDER = "order";
    public static final String PRICE_ID = "price_id";

    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private Product product;
    private String productSlug;
    private EventsTrackerFacade eventsTrackerFacade;
    private LinearLayout discountContainer;
    private TextView discountPrompt;
    private TextView couponAppliedText;
    private EditText couponEditText;
    private Button applyCouponButton;

    private StoreApiClient apiClient;

    public Order order;
    ProgressDialog progressDialog;
    private InstituteSettings settings;
    private TestpressSession session;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_product_details_layout);
        if (getIntent().getStringExtra(PRODUCT_SLUG) != null) {
            productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        } else if (getIntent().getParcelableExtra(PRODUCT) != null) {
            productSlug = ((Product) getIntent().getParcelableExtra(PRODUCT)).getSlug();
        }
        session = TestpressSdk.getTestpressSession(this);
        settings = session.getInstituteSettings();
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        progressDialog = new ProgressDialog(this);
        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        couponAppliedText = (TextView) findViewById(R.id.coupon_applied_text);
        couponEditText = (EditText) findViewById(R.id.coupon);
        applyCouponButton = (Button) findViewById(R.id.apply_coupon);
        discountPrompt = findViewById(R.id.discount_prompt);
        discountContainer = findViewById(R.id.discount_container);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductDetails();
            }
        });
        eventsTrackerFacade = new EventsTrackerFacade(getApplicationContext());
        apiClient = new StoreApiClient(this);

        initOnClickListeners();
        loadProductDetails();
    }

    void initOnClickListeners() {
        discountPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (discountContainer.getVisibility() == View.GONE) {
                    discountContainer.setVisibility(View.VISIBLE);
                    discountPrompt.setVisibility(View.GONE);
                }
            }
        });

        couponEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    applyCouponButton.setEnabled(true);
                } else {
                    applyCouponButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        applyCouponButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                createOrder();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);
        new StoreApiClient(this).getProductDetail(productSlug)
                .enqueue(new TestpressCallback<Product>() {
                    @Override
                    public void onSuccess(Product product) {
                        onProductLoaded(product);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                        } else if (exception.isClientError()) {
                            setEmptyText(R.string.testpress_no_product_found,
                                    R.string.testpress_no_product_found_description,
                                    R.drawable.ic_error_outline_black_18dp);
                            retryButton.setVisibility(View.INVISIBLE);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_products,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.ic_error_outline_black_18dp);
                            retryButton.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public void onProductLoaded(Product product) {
        ImageView image = (ImageView) findViewById(R.id.thumbnail_image);
        TextView titleText = (TextView) findViewById(R.id.title);
        View totalExamsContainer = findViewById(R.id.total_exams_container);
        TextView totalExams = (TextView) findViewById(R.id.total_exams);
        View totalNotesContainer = findViewById(R.id.total_notes_container);
        TextView totalNotes = (TextView) findViewById(R.id.total_notes);
        TextView priceText = (TextView) findViewById(R.id.price);
        View descriptionContainer = findViewById(R.id.description_container);
        TextView descriptionText = (TextView) findViewById(R.id.description);
        View descriptionContainerLine = (View) findViewById(R.id.description_line);
        View examsListContainer = findViewById(R.id.exams_list_container);
        ListView examsListView = (ListView) findViewById(R.id.exams_list);
        View notesListContainer = findViewById(R.id.notes_list_container);
        ListView notesListView = (ListView) findViewById(R.id.notes_list);
        View productDetailsView = findViewById(R.id.main_content);
        Button buyButton = (Button) findViewById(R.id.buy_button);
        progressBar.setVisibility(View.GONE);
        findViewById(R.id.coupon_and_buy_button_container).setVisibility(View.VISIBLE);
        productDetailsView.setVisibility(View.VISIBLE);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order();
            }
        });

        ImageLoader imageLoader = ImageUtils.initImageLoader(this);
        DisplayImageOptions options = ImageUtils.getPlaceholdersOption();
        String productImageURL = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getOriginal()
                : "";
        imageLoader.displayImage(productImageURL, image, options);
        titleText.setText(product.getTitle());
        buyButton.setText(product.getBuyNowText());

        if(product.getExams().size() != 0) {
            int examsCount = product.getExams().size();
            totalExams.setText(getResources().getQuantityString(R.plurals.exams_count, examsCount,
                    examsCount));

            TextView accessCodeButton = (TextView) findViewById(R.id.have_access_code);
            if (settings.isAccessCodeEnabled()) {
                accessCodeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TestpressExam.showExamsForAccessCode(ProductDetailsActivity.this, session);
                    }
                });
                accessCodeButton.setVisibility(View.VISIBLE);
            } else {
                accessCodeButton.setVisibility(View.GONE);
            }
            totalExamsContainer.setVisibility(View.VISIBLE);
        } else {
            totalExamsContainer.setVisibility(View.GONE);
        }

        if(product.getNotes().size() != 0) {
            int notesCount = product.getNotes().size();
            totalNotes.setText(getResources().getQuantityString(R.plurals.documents_count,
                    notesCount, notesCount));

            totalNotesContainer.setVisibility(View.VISIBLE);
        } else {
            totalNotesContainer.setVisibility(View.GONE);
        }

        // Price
        priceText.setText(product.getPrice());

        // Update product description
        if(product.getDescription().isEmpty()) {
            descriptionContainer.setVisibility(View.GONE);
            descriptionContainerLine.setVisibility(View.GONE);
        } else {
            descriptionContainer.setVisibility(View.VISIBLE);
            descriptionContainerLine.setVisibility(View.VISIBLE);
            Spanned html = Html.fromHtml(product.getDescription(),
                    new UILImageGetter(descriptionText, ProductDetailsActivity.this), null);

            descriptionText.setText(
                    ZoomableImageString.convertString(html, ProductDetailsActivity.this, false),
                    TextView.BufferType.SPANNABLE);

            descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
            descriptionText.setVisibility(View.VISIBLE);
        }

        // Update exams list
        if(product.getExams().isEmpty()) {
            examsListContainer.setVisibility(View.GONE);
        } else {
            examsListContainer.setVisibility(View.VISIBLE);
            examsListView.setFocusable(false);
            examsListView.setAdapter(new ProductExamsAdapter(this, product.getExams()));
            ViewUtils.setListViewHeightBasedOnChildren(examsListView);
        }

        // Update notes list
        if(product.getNotes().isEmpty()) {
            notesListContainer.setVisibility(View.GONE);
        } else {
            notesListContainer.setVisibility(View.VISIBLE);
            notesListView.setFocusable(false);
            notesListView.setAdapter(new NotesListAdapter(getLayoutInflater(),
                    product.getNotes(), R.layout.testpress_product_notes_list_item));

            ViewUtils.setListViewHeightBasedOnChildren(notesListView);
        }

        ProductDetailsActivity.this.product = product;
        logEvent(EventsTrackerFacade.VIEWED_PRODUCT_EVENT);
    }

    void createOrder() {
        showProgressDialog("Creating your order...");

        if (order != null) {
            applyCoupon((long) order.getId());
            return;
        }

        OrderItem orderItem = createOrderItem();
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        apiClient.order(orderItems).enqueue(new TestpressCallback<Order>() {
            @Override
            public void onSuccess(Order createdOrder) {
                order = createdOrder;
                applyCoupon((long) order.getId());
            }

            @Override
            public void onException(TestpressException exception) {
                handleOrderCreationFailure(exception);
            }
        });
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private OrderItem createOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product.getSlug());
        orderItem.setQuantity(1);
        orderItem.setPrice(String.valueOf(product.getPrices().get(0).getId()));
        return orderItem;
    }

    private void handleOrderCreationFailure(TestpressException exception) {
        progressDialog.dismiss();
        if (exception.isNetworkError()) {
            showToast("Please check your internet connection");
        } else {
            String orderCreationId = UtilKt.generateRandom10CharString();
            Sentry.captureException(exception, scope -> scope.setTag("orderCreationId", orderCreationId));
            showToast("Failed to create order. Please contact support with ID: " + orderCreationId);
        }
    }

    private void showToast(String message) {
        Toast.makeText(ProductDetailsActivity.this, message, Toast.LENGTH_LONG).show();
    }

    void applyCoupon(Long orderId) {
        showProgressDialog("Applying Coupon code...");

        String couponCode = couponEditText.getText().toString();
        apiClient.applyCoupon(orderId, couponCode, settings != null && settings.getUseNewDiscountFeat()).enqueue(new TestpressCallback<Order>() {
            @Override
            public void onSuccess(Order createdOrder) {
                order = createdOrder;
                updateCouponAppliedText(couponCode, createdOrder);
                updatePriceDisplay(createdOrder);
                progressDialog.dismiss();
            }

            @Override
            public void onException(TestpressException exception) {
                handleCouponApplicationFailure(exception);
            }
        });
    }

    private void updateCouponAppliedText(String couponCode, Order createdOrder) {
        couponAppliedText.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.testpress_text_gray));
        couponAppliedText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_24, 0, 0, 0);

        try {
            double originalPrice = Double.parseDouble(product.getPrice());
            double discountedPrice = Double.parseDouble(createdOrder.getOrderItems().get(0).getPrice());
            double savings = originalPrice - discountedPrice;

            couponAppliedText.setText(couponCode + " Applied! You have saved ₹" + savings + " on this course.");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            couponAppliedText.setText(couponCode + " Applied! Discount has been applied successfully.");
        }
        couponAppliedText.setVisibility(View.VISIBLE);
    }

    private void updatePriceDisplay(Order createdOrder) {
        TextView priceText = findViewById(R.id.price);
        String newPrice = createdOrder.getOrderItems().get(0).getPrice();
        String oldPrice = product.getPrice();

        SpannableString oldPriceStrikethrough = new SpannableString(oldPrice);
        oldPriceStrikethrough.setSpan(new StrikethroughSpan(), 0, oldPrice.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder finalText = new SpannableStringBuilder();
        finalText.append(newPrice).append("  ").append(oldPriceStrikethrough);

        priceText.setText(finalText);
    }

    private void handleCouponApplicationFailure(TestpressException exception) {
        TextView priceText = findViewById(R.id.price);
        priceText.setText(product.getPrice());
        order = null;

        if (exception.isNetworkError()) {
            showToast("Please check your internet connection");
        } else {
            showInvalidCouponMessage();
        }
        progressDialog.dismiss();
    }

    private void showInvalidCouponMessage() {
        couponAppliedText.setVisibility(View.VISIBLE);
        couponAppliedText.setText("Invalid coupon code.");
        couponAppliedText.setTextColor(Color.RED);
        couponAppliedText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    public void order() {
        if (product == null) {
            return;
        }
        logEvent(EventsTrackerFacade.CLICKED_BUY_NOW);
        if (this.product.getPaymentLink().isEmpty()) {
            Intent intent = new Intent(ProductDetailsActivity.this, OrderConfirmActivity.class);
            intent.putExtra(PRODUCT, product);
            intent.putExtra(ORDER, order);
            startActivityForResult(intent, STORE_REQUEST_CODE);
        } else {
            Uri uri = Uri.parse(this.product.getPaymentLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void logEvent(String eventName) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", this.product.getTitle());
        params.put("slug", this.product.getSlug());
        params.put("id", this.product.getId());
        eventsTrackerFacade.logEvent(eventName, params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == STORE_REQUEST_CODE && resultCode == RESULT_OK && data.getBooleanExtra(PAYMENT_SUCCESS, false)) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

}

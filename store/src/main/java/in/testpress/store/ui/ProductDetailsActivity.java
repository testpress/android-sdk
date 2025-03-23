package in.testpress.store.ui;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
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
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.ImageUtils;
import in.testpress.util.UILImageGetter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;

import static in.testpress.store.TestpressStore.PAYMENT_SUCCESS;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductDetailsActivity extends BaseToolBarActivity {

    public static final String PRODUCT_SLUG = "productSlug";
    public static final String PRODUCT = "product";
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

    private List<OrderItem> orderItems;
    public Order order;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_product_details_layout);
        if (getIntent().getStringExtra(PRODUCT_SLUG) != null) {
            productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        } else if (getIntent().getParcelableExtra(PRODUCT) != null) {
            productSlug = ((Product) getIntent().getParcelableExtra(PRODUCT)).getSlug();
        }
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);

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
        imageLoader.displayImage(product.getImages().get(0).getOriginal(), image, options);

        titleText.setText(product.getTitle());
        buyButton.setText(product.getBuyNowText());

        if(product.getExams().size() != 0) {
            int examsCount = product.getExams().size();
            totalExams.setText(getResources().getQuantityString(R.plurals.exams_count, examsCount,
                    examsCount));

            TextView accessCodeButton = (TextView) findViewById(R.id.have_access_code);
            final TestpressSession session = TestpressSdk.getTestpressSession(this);
            //noinspection ConstantConditions
            InstituteSettings settings = session.getInstituteSettings();
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
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product.getSlug());
        orderItem.setQuantity(1);
        orderItem.setPrice(String.valueOf(product.getPrices().get(0).getId()));
        orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        apiClient.order(orderItems).enqueue(new TestpressCallback<Order>() {
            @Override
            public void onSuccess(Order createdOrder) {
                order = createdOrder;
                applyCoupon((long)order.getId());

                Log.d("TAG", "onSuccess: "+order);

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
                            createOrder();
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

    void applyCoupon(Long orderId) {
        apiClient.applyCoupon(orderId, "test 123").enqueue(new TestpressCallback<Order>() {
            @Override
            public void onSuccess(Order createdOrder) {
                //order = createdOrder;

                Log.d("TAG", "onSuccess: "+createdOrder);
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
                            //createOrder();
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

    public void order() {
        if (product == null) {
            return;
        }
        logEvent(EventsTrackerFacade.CLICKED_BUY_NOW);
        if (this.product.getPaymentLink().isEmpty()) {
            Intent intent = new Intent(ProductDetailsActivity.this, OrderConfirmActivity.class);
            intent.putExtra(PRODUCT, product);
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

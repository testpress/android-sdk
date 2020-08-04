package in.testpress.store.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.HashMap;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.models.InstituteSettings;
import in.testpress.store.R;
import in.testpress.store.models.CouponCodeResponse;
import in.testpress.store.models.Product;
import in.testpress.store.models.ProductDetailResponse;
import in.testpress.store.network.TestpressStoreApiClient;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.ImageUtils;
import in.testpress.util.UILImageGetter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductDetailsActivity extends BaseToolBarActivity {
    public static final String PRODUCT_SLUG = "productSlug";
    public static final String PRODUCT = "product";
    public static final String CURRENT_AMOUNT = "currentAmount";
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private Product product;
    private String productSlug;
    private EventsTrackerFacade eventsTrackerFacade;
    private TextView haveCouponCode;
    private LinearLayout couponCodeContainer;
    private EditText couponCode;
    private Button applyCoupon;
    private TextView priceText;
    private TextView couponCodeStatus;
    private Button buyButton;
    private View notesListContainer;
    private ListView notesListView;
    private View examsListContainer;
    private ListView examsListView;
    private View descriptionContainer;
    private TextView descriptionText;
    private View descriptionContainerLine;
    private TextView accessCodeButton;
    private ProductDetailResponse productDetailResponse;
    private TextView discountAmount;
    private String discountedAmount = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_product_details_layout);
        eventsTrackerFacade = new EventsTrackerFacade(getApplicationContext());
        getDataFromIntent();
        initializeViews();
        initClickListeners();
        getProductDetails();
        loadProductDetails();
    }

    private void getDataFromIntent() {
        if (getIntent().getStringExtra(PRODUCT_SLUG) != null) {
            productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        } else if (getIntent().getParcelableExtra(PRODUCT) != null) {
            productSlug = ((Product) getIntent().getParcelableExtra(PRODUCT)).getSlug();
        }
    }

    private void initializeViews() {
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        haveCouponCode = findViewById(R.id.have_coupon_code);
        applyCoupon = findViewById(R.id.button_apply_coupon);
        couponCode = findViewById(R.id.et_coupon_code);
        couponCodeContainer = findViewById(R.id.coupon_code_container);
        couponCodeStatus = findViewById(R.id.tv_coupon_code_status);
        buyButton = (Button) findViewById(R.id.buy_button);
        notesListContainer = findViewById(R.id.notes_list_container);
        notesListView = (ListView) findViewById(R.id.notes_list);
        examsListContainer = findViewById(R.id.exams_list_container);
        examsListView = (ListView) findViewById(R.id.exams_list);
        descriptionContainer = findViewById(R.id.description_container);
        descriptionText = (TextView) findViewById(R.id.description);
        descriptionContainerLine = (View) findViewById(R.id.description_line);
        accessCodeButton = (TextView) findViewById(R.id.have_access_code);
        discountAmount = findViewById(R.id.tv_discount_amount);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
    }

    private void initClickListeners() {
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProductDetails();
            }
        });
        haveCouponCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCouponCodeContainerVisibility();
            }
        });
        applyCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                verifyCouponCode();
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order();
            }
        });
    }

    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);
        getProductDetailsFromNetwork();
    }

    private void getProductDetailsFromNetwork() {
        new TestpressStoreApiClient(this).getProductDetail(productSlug)
                .enqueue(new TestpressCallback<Product>() {
                    @Override
                    public void onSuccess(Product product) {
                        onProductLoaded(product);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleNetworkException(exception, false);
                    }
                });
    }

    public void onProductLoaded(Product product) {
        priceText = (TextView) findViewById(R.id.price);
        View productDetailsView = findViewById(R.id.main_content);
        progressBar.setVisibility(View.GONE);
        productDetailsView.setVisibility(View.VISIBLE);
        ProductDetailsActivity.this.product = product;
        setProductDetail(product);
        setExamsView();
        setNotesView();
        updateProductDescription();
        updateExamsList();
        updateNotesList();
        logEvent(EventsTrackerFacade.VIEWED_PRODUCT_EVENT);
    }

    private void setProductDetail(Product product) {
        ImageView image = (ImageView) findViewById(R.id.thumbnail_image);
        TextView titleText = (TextView) findViewById(R.id.title);
        ImageLoader imageLoader = ImageUtils.initImageLoader(this);
        DisplayImageOptions options = ImageUtils.getPlaceholdersOption();
        imageLoader.displayImage(product.getImages().get(0).getOriginal(), image, options);
        titleText.setText(product.getTitle());
        buyButton.setText(product.getBuyNowText());
        priceText.setText(product.getPrice());
    }

    private void setExamsView() {
        View totalExamsContainer = findViewById(R.id.total_exams_container);
        if (product.getExams().size() != 0) {
            setTotalExamCount();
            final TestpressSession session = TestpressSdk.getTestpressSession(this);
            //noinspection ConstantConditions
            InstituteSettings settings = session.getInstituteSettings();
            if (settings.isAccessCodeEnabled()) {
                handleAccessCode(session);
            } else {
                accessCodeButton.setVisibility(View.GONE);
            }
            totalExamsContainer.setVisibility(View.VISIBLE);
        } else {
            totalExamsContainer.setVisibility(View.GONE);
        }
    }

    private void setTotalExamCount() {
        TextView totalExams = (TextView) findViewById(R.id.total_exams);
        int examsCount = product.getExams().size();
        totalExams.setText(getResources().getQuantityString(R.plurals.exams_count, examsCount,
                examsCount));
    }

    private void handleAccessCode(final TestpressSession session) {
        accessCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestpressExam.showExamsForAccessCode(ProductDetailsActivity.this, session);
            }
        });
        accessCodeButton.setVisibility(View.VISIBLE);
    }

    private void setNotesView() {
        View totalNotesContainer = findViewById(R.id.total_notes_container);
        TextView totalNotes = (TextView) findViewById(R.id.total_notes);
        if (product.getNotes().size() != 0) {
            int notesCount = product.getNotes().size();
            totalNotes.setText(getResources().getQuantityString(R.plurals.documents_count,
                    notesCount, notesCount));

            totalNotesContainer.setVisibility(View.VISIBLE);
        } else {
            totalNotesContainer.setVisibility(View.GONE);
        }
    }

    private void updateProductDescription() {
        if (product.getDescription().isEmpty()) {
            descriptionContainer.setVisibility(View.GONE);
            descriptionContainerLine.setVisibility(View.GONE);
        } else {
            setDescriptionContainer();
        }
    }

    private void setDescriptionContainer() {
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

    private void updateExamsList() {
        if (product.getExams().isEmpty()) {
            examsListContainer.setVisibility(View.GONE);
        } else {
            setListView(examsListContainer, examsListView);
            setExamListAdapter();
        }
    }

    private void setExamListAdapter() {
        examsListView.setAdapter(new ProductExamsAdapter(this, product.getExams()));
        ViewUtils.setListViewHeightBasedOnChildren(examsListView);
    }

    private void updateNotesList() {
        if (product.getNotes().isEmpty()) {
            notesListContainer.setVisibility(View.GONE);
        } else {
            setListView(notesListContainer, notesListView);
            setNotesListAdapter();
        }
    }

    private void setListView(View container, ListView listView) {
        container.setVisibility(View.VISIBLE);
        listView.setFocusable(false);
    }

    private void setNotesListAdapter() {
        notesListView.setAdapter(new NotesListAdapter(getLayoutInflater(),
                product.getNotes(), R.layout.testpress_product_notes_list_item));
        ViewUtils.setListViewHeightBasedOnChildren(notesListView);
    }

    private void setCouponCodeContainerVisibility() {
        haveCouponCode.setVisibility(View.GONE);
        couponCodeContainer.setVisibility(View.VISIBLE);
    }

    private void verifyCouponCode() {
        new TestpressStoreApiClient(this).applyCouponCode(productDetailResponse.getOrder().getId(), couponCode.getText())
                .enqueue(new TestpressCallback<CouponCodeResponse>() {
                    @Override
                    public void onSuccess(CouponCodeResponse result) {
                        setCouponCodeSuccess(result);
                    }
                    @Override
                    public void onException(TestpressException exception) {
                        handleNetworkException(exception,true);
                    }
                });
    }

    private void setCouponCodeSuccess(CouponCodeResponse result) {
        progressBar.setVisibility(View.GONE);
        priceText.setText(result.getAmount());
        couponCodeStatus.setText(result.getVoucher().getCode() + " applied");
        discountedAmount = result.getAmount();
        float discountedAmount = Float.parseFloat(result.getAmountWithoutDiscounts()) - Float.parseFloat(result.getAmount());
        discountAmount.setText("You have saved " + getString(R.string.rupee_symbol) + discountedAmount + " on this course.");
    }

    public void order() {
        if (product == null) {
            return;
        }
        logEvent(EventsTrackerFacade.CLICKED_BUY_NOW);
        if (this.product.getPaymentLink().isEmpty()) {
            navigateToOrderConfirmActivity();
        } else {
            openPayment();
        }
    }

    private void navigateToOrderConfirmActivity() {
        Intent intent = new Intent(ProductDetailsActivity.this, OrderConfirmActivity.class);
        intent.putExtra(PRODUCT, product);
        intent.putExtra(CURRENT_AMOUNT,discountedAmount);
        startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    private void openPayment() {
        Uri uri = Uri.parse(this.product.getPaymentLink());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void getProductDetails() {
        new TestpressStoreApiClient(this).getProductDetails(productSlug)
                .enqueue(new TestpressCallback<ProductDetailResponse>() {
                    @Override
                    public void onSuccess(ProductDetailResponse result) {
                        productDetailResponse = result;
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleNetworkException(exception,false);
                    }
                });
    }

    private void handleNetworkException(TestpressException exception, boolean isCouponCodeException) {
        if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        } else if (exception.isClientError()) {
            handleClientException(isCouponCodeException);
        } else {
            setEmptyText(R.string.testpress_error_loading_products,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.INVISIBLE);
        }
    }

    private void handleClientException(boolean isCouponCodeException) {
        if (isCouponCodeException) {
            discountAmount.setText(R.string.invalid_coupon_code);
        } else {
            setEmptyText(R.string.testpress_no_product_found,
                    R.string.testpress_no_product_found_description,
                    R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.INVISIBLE);
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
        if (requestCode == STORE_REQUEST_CODE && resultCode == RESULT_OK) {
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

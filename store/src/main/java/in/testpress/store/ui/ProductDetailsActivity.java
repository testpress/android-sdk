package in.testpress.store.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Arrays;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.store.R;
import in.testpress.store.models.Product;
import in.testpress.store.network.TestpressStoreApiClient;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.util.FormatDate;
import in.testpress.util.ImageUtils;
import in.testpress.util.UILImageGetter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;

public class ProductDetailsActivity extends BaseToolBarActivity {

    public static final String PRODUCT_SLUG = "productSlug";


    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private Product productDetails;
    private String productSlug;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_product_details_layout);
        if (getIntent().getStringExtra(PRODUCT_SLUG) != null) {
            productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        } else if (getIntent().getParcelableExtra("product") != null) {
            productSlug = ((Product) getIntent().getParcelableExtra("product")).getSlug();
        }
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);

        emptyView = (LinearLayout) findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);

        loadProductDetails();
    }

    void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);
        new TestpressStoreApiClient(this).getProductDetail(productSlug)
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
        TextView dateText = (TextView) findViewById(R.id.date);
        TextView categoriesText = (TextView) findViewById(R.id.categories);
        TextView priceText = (TextView) findViewById(R.id.price);
        View descriptionContainer = findViewById(R.id.description_container);
        TextView descriptionText = (TextView) findViewById(R.id.description);
        View examsListContainer = findViewById(R.id.exams_list_container);
        ListView examsListView = (ListView) findViewById(R.id.exams_list);
        View notesListContainer = findViewById(R.id.notes_list_container);
        ListView notesListView = (ListView) findViewById(R.id.notes_list);
        View productDetailsView = findViewById(R.id.main_content);
        Button buyButton = (Button) findViewById(R.id.buy_button);
        progressBar.setVisibility(View.GONE);
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

        String date = FormatDate.getDate(product.getStartDate(), product.getEndDate());
        if(date != null) {
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(date);
        }

        // Price & Categories
        String categories = Arrays.toString(product.getCategories().toArray());
        categoriesText.setText(categories.substring(1, categories.length() - 1));
        priceText.setText(product.getPrice());

        // Update product description
        if(product.getDescription().isEmpty()) {
            descriptionContainer.setVisibility(View.GONE);
        } else {
            descriptionContainer.setVisibility(View.VISIBLE);
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

        ProductDetailsActivity.this.productDetails = product;
    }

    public void order() {
        if (productDetails == null) {
            return;
        }
        if (this.productDetails.getPaymentLink().isEmpty()) {
            // TODO: Goto Order Confirm
        } else {
            Uri uri = Uri.parse(this.productDetails.getPaymentLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

}

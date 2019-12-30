package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.ContentPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;
import static in.testpress.course.TestpressCourse.CHAPTER_ID;
import static in.testpress.course.TestpressCourse.FROM_PRODUCT;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;


public class ContentsListFragment extends BaseDataBaseFragment<Content, Long> {

    long chapterId;

    public static ContentsListFragment getInstance(long chapterId) {
        Log.d("ContentsListFragment", "getInstance: ");
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CHAPTER_ID, chapterId);
        fragment.setArguments(bundle);
        return fragment;
    }
    private TestpressCourseApiClient mApiClient;
    private String contentsUrlFrag;
    private ContentDao contentDao;
    private Button buyNowButton;
    private String product_slug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        chapterId = getArguments().getLong(CHAPTER_ID);
        product_slug = getArguments().getString(PRODUCT_SLUG);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        firstCallBack = false;
        listShown = true;
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected AbstractDao<Content, Long> getDao() {
        return null;
    }

    @Override
    protected SingleTypeAdapter<Content> createAdapter(List<Content> items) {
        if (getActivity() == null) {
            return null;
        }
        return new ContentsListAdapter(getActivity(), chapterId);
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RelativeLayout ll_Main  = new RelativeLayout(getActivity());
        buyNowButton = new Button(getActivity());
        buyNowButton.setText("Buy Now");
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        buyNowButton.setLayoutParams(lp);
        buyNowButton.setVisibility(View.INVISIBLE);

        if (product_slug != null) {
            buyNowButton.setVisibility(View.VISIBLE);
            buyNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                    intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product_slug);
                    getActivity().startActivityForResult(intent, STORE_REQUEST_CODE);
                }
            });
        }
        ll_Main.addView(buyNowButton);
        listView.addFooterView(ll_Main);
    }

    @Override
    protected boolean isItemsEmpty() {
        return TestpressSDKDatabase.getContentDao(getContext()).queryBuilder().where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
        ).listLazy().isEmpty();
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        return 0;
    }

    @Override
    protected ContentPager getPager() {
        return null;
    }

}

package in.testpress.course.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Chapter;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.BaseGridFragment;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

public class ChaptersGridFragment extends BaseGridFragment<Chapter> {

    public static final String CHAPTERS_URL_FRAG = "chaptersUrlFrag";
    public static final String PARENT_ID = "parentId";
    private TestpressCourseApiClient mApiClient;
    private String chaptersUrlFrag;
    private String parentId = "null";
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chaptersUrlFrag =  getArguments().getString(CHAPTERS_URL_FRAG);
        if (getArguments() == null || chaptersUrlFrag == null || chaptersUrlFrag.isEmpty()) {
            throw new IllegalArgumentException("CHAPTERS_URL_FRAG must not be null or empty");
        }
        if (getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }
        mApiClient = new TestpressCourseApiClient(getActivity());
        mOptions = ImageUtils.getPlaceholdersOption();
        mImageLoader = ImageUtils.initImageLoader(getActivity());
    }

    @Override
    protected ChapterPager getPager() {
        if (pager == null) {
            pager = new ChapterPager(chaptersUrlFrag, parentId, mApiClient);
        }
        return (ChapterPager)pager;
    }

    @Override
    protected View getChildView(final Chapter chapter) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.testpress_chapter_grid_item, null, false);
        TextView name = (TextView) view.findViewById(R.id.title);
        ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnail_image);
        name.setText(chapter.getName());
        if (chapter.getImage() == null || chapter.getImage().isEmpty()) {
            thumbnailImage.setVisibility(View.GONE);
        } else {
            thumbnailImage.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(chapter.getImage(), thumbnailImage, mOptions);
        }
        View lock = view.findViewById(R.id.lock);
        View whiteForeground = view.findViewById(R.id.white_foreground);
        if (chapter.getIsLocked()) {
            lock.setVisibility(View.VISIBLE);
            whiteForeground.setVisibility(View.VISIBLE);
        } else {
            lock.setVisibility(View.GONE);
            whiteForeground.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chapter.getChildrenCount() > 0) {
                        getActivity().startActivity(
                                ChaptersGridActivity.createIntent(chapter.getName(),
                                        chaptersUrlFrag, chapter.getId().toString(), getContext()));
                    } else {
                        getActivity().startActivity(ContentsListActivity.createIntent(chapter.getName(),
                                chapter.getContentUrlFrag(), getContext()));
                    }
                }
            });
        }
        name.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        return view;
    }

    @Override
    protected TableRow.LayoutParams getLayoutParams() {
        return new TableRow.LayoutParams(getChildColumnWidth(), TableRow.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int getChildColumnWidth() {
        return (int)UIUtils.getPixelFromDp(getContext(), 118);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_chapters,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_chapter, R.string.testpress_no_chapter_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}

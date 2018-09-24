package in.testpress.course.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.ContentPager;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.TestpressCourse.CHAPTER_ID;

public class ContentsListFragment extends BaseDataBaseFragment<Content, Long> {

    long chapterId;

    public static ContentsListFragment getInstance(long chapterId) {
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CHAPTER_ID, chapterId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        chapterId = getArguments().getLong(CHAPTER_ID);

        List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(getContext()).queryBuilder()
                .where(ChapterDao.Properties.Id.eq(chapterId))
                .list();

        if (!chapters.isEmpty()) {
            //noinspection ConstantConditions
            ((ExpandableContentsActivity) getActivity())
                    .setActionBarTitle(chapters.get(0).getName());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                if (listView.getChildAt(0) != null) {
                    //noinspection ConstantConditions
                    ((ExpandableContentsActivity) getActivity()).swipeRefreshLayout.setEnabled(
                            listView.getFirstVisiblePosition() == 0 &&
                                    listView.getChildAt(0).getTop() == 0
                    );
                }
            }
        });
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

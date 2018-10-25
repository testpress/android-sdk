package in.testpress.course.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import pl.openrnd.multilevellistview.ItemInfo;
import pl.openrnd.multilevellistview.MultiLevelListView;
import pl.openrnd.multilevellistview.Node;
import pl.openrnd.multilevellistview.OnItemClickListener;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_CHAPTER_ID;

public class ExpandableContentsFragment extends Fragment {

    private MultiLevelListView multiLevelListView;
    private ListView listView;

    private ExpandableContentsAdapter adapter;
    private ExpandableContentsActivity expandableContentsActivity;
    private List<Long> chapterPathIds = new ArrayList<>();
    public long parentChapterId;
    public int currentExpandedLevel;

    public static ExpandableContentsFragment getInstance(long courseId) {
        return getInstance(courseId, null);
    }

    public static ExpandableContentsFragment getInstance(long courseId, Long parentChapterId) {
        ExpandableContentsFragment fragment = new ExpandableContentsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(COURSE_ID, courseId);
        if (parentChapterId != null) {
            bundle.putLong(PARENT_CHAPTER_ID, parentChapterId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(R.layout.testpress_fragment_expanable_contents, container, false);

        multiLevelListView = view.findViewById(R.id.listView);
        adapter = new ExpandableContentsAdapter(getContext());
        multiLevelListView.setAdapter(adapter);
        listView = view.findViewById(android.R.id.list);
        multiLevelListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(MultiLevelListView parent, View view, Object item,
                                      ItemInfo itemInfo) {

                if (item instanceof Chapter) {
                    Snackbar.make(listView, R.string.testpress_no_content_description,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Content content = (Content) item;
                    startActivity(ContentActivity.createIntent(
                            content.getId(),
                            content.getChapterId(),
                            (AppCompatActivity) getActivity())
                    );
                }
            }

            @Override
            public void onGroupItemClicked(MultiLevelListView parent, final View view, final int position,
                                           Object item, final ItemInfo itemInfo) {

                Chapter chapter = (Chapter) item;
                if (!chapterPathIds.isEmpty() && position + 1 < adapter.getFlatItems().size()) {
                    expandChapters(chapterPathIds, position + 1);
                } else {
                    if (parentChapterId != 0 && itemInfo.getLevel() <= currentExpandedLevel
                            && chapter.getId() != parentChapterId) {
                        
                        parentChapterId = 0;
                        currentExpandedLevel = 0;
                    }
                    if (chapter.getRawChildrenCount(getContext()) == 1) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                int childPosition = position + 1;
                                Node node = adapter.getFlatItems().get(childPosition);
                                if (node.isExpandable() && !node.isExpanded()) {
                                    multiLevelListView.onGroupItemClicked(
                                            listView.getAdapter().getView(childPosition, null, null),
                                            node,
                                            childPosition
                                    );
                                }
                            }
                        });
                    }
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                if (listView.getChildAt(0) != null) {
                    expandableContentsActivity.swipeRefreshLayout.setEnabled(
                            listView.getFirstVisiblePosition() == 0 &&
                                    listView.getChildAt(0).getTop() == 0
                    );
                }
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        expandableContentsActivity = ((ExpandableContentsActivity) getActivity());

        assert getArguments() != null;
        long courseId = getArguments().getLong(COURSE_ID, 0);

        List<Chapter> rootChapters = Chapter.getRootChaptersQueryBuilder(getContext(), courseId).list();
        adapter.setDataItems(rootChapters);

        parentChapterId = getArguments().getLong(PARENT_CHAPTER_ID, 0);
        if (parentChapterId != 0) {
            chapterPathIds = getChapterPathIds(parentChapterId);
            expandChapters(chapterPathIds, 0);
        } else if (rootChapters.size() < 3) {
            for (Chapter chapter : rootChapters) {
                if (adapter.isExpandable(chapter)) {
                    chapterPathIds.add(chapter.getId());
                }
            }
            expandChapters(chapterPathIds, 0);
        }
    }

    List<Long> getChapterPathIds(Long parentChapterId) {
        List<Chapter> chaptersFromDB = TestpressSDKDatabase.getChapterDao(getContext()).queryBuilder()
                .where(ChapterDao.Properties.Id.eq(parentChapterId))
                .list();

        if (!chaptersFromDB.isEmpty()) {
            chapterPathIds.add(0, parentChapterId);
            Long parentId = chaptersFromDB.get(0).getParentId();
            if (parentId == null || parentId == 0) {
                return chapterPathIds;
            } else {
                return getChapterPathIds(chaptersFromDB.get(0).getParentId());
            }
        }
        return Collections.emptyList();
    }

    void expandChapters(List<Long> chapterPathIds, int searchPosition) {
        if (!chapterPathIds.isEmpty()) {
            Long currentPathId = chapterPathIds.remove(0);
            do {
                Node node = adapter.getFlatItems().get(searchPosition);
                if (node.getObject() instanceof Chapter) {
                    Chapter chapter = (Chapter) node.getObject();
                    if (chapter.getId().equals(currentPathId)) {
                        if (parentChapterId != 0 && currentPathId == parentChapterId) {
                            currentExpandedLevel = searchPosition;
                        }
                        if (node.isExpandable() && !node.isExpanded()) {
                            multiLevelListView.onGroupItemClicked(
                                    listView.getAdapter().getView(searchPosition, null, null),
                                    node,
                                    searchPosition
                            );
                        }
                        break;
                    }
                }
            } while (++searchPosition < adapter.getFlatItems().size());
        }
    }
}

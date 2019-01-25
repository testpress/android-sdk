package in.testpress.course.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Exam;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import pl.openrnd.multilevellistview.ItemInfo;
import pl.openrnd.multilevellistview.MultiLevelListAdapter;

import static in.testpress.models.greendao.Content.ATTACHMENT_TYPE;
import static in.testpress.models.greendao.Content.HTML_TYPE;
import static in.testpress.models.greendao.Content.VIDEO_TYPE;

public class ExpandableContentsAdapter extends MultiLevelListAdapter {

    private Context context;
    private ImageLoader mImageLoader;
    private ObjectAnimator animator;
    private long backgroundShadeItemId;

    ExpandableContentsAdapter(Context context) {
        this.context = context;
        mImageLoader = ImageUtils.initImageLoader(context);
    }

    @Override
    public List<?> getSubObjects(Object object) {
        Chapter chapter = (Chapter) object;
        return getQueryBuilder(chapter).listLazy();
    }

    private QueryBuilder getQueryBuilder(Chapter chapter) {
        if (chapter.getRawChildrenCount(context) > 0) {
            return TestpressSDKDatabase.getChapterDao(context).queryBuilder().where(
                    ChapterDao.Properties.ParentId.eq(chapter.getId()),
                    ChapterDao.Properties.Active.eq(true)
            ).orderAsc(ChapterDao.Properties.Order);
        }
        return TestpressSDKDatabase.getContentDao(context).queryBuilder().where(
                ContentDao.Properties.ChapterId.eq(chapter.getId()),
                ContentDao.Properties.Active.eq(true)
        ).orderAsc(ContentDao.Properties.Order);
    }

    @Override
    public boolean isExpandable(Object object) {
        if (object instanceof Chapter) {
            Chapter chapter = (Chapter) object;
            return chapter.getRawChildrenCount(context) > 0 ||
                    chapter.getRawContentsCount(context) > 0;
        }
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getViewForObject(Object object, View convertView, ItemInfo itemInfo) {
        if (object instanceof Chapter) {
            Chapter chapter = (Chapter) object;
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert layoutInflater != null;
            convertView =
                    layoutInflater.inflate(R.layout.testpress_chapter_item, null);

            TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setText(chapter.getName());
            ImageView thumbnailImageView = convertView.findViewById(R.id.chapter_image);
            ImageView expandableItemIndicator = convertView.findViewById(R.id.exp_list_indicator);
            LinearLayout contentLayout = convertView.findViewById(R.id.content_layout);
            View divider = convertView.findViewById(R.id.divider);
            int paddingVertical = (int) UIUtils.getPixelFromDp(context, 15);
            if (itemInfo.getLevel() == 0) {
                lblListHeader.setTypeface(TestpressSdk.getRubikMediumFont(context));
                expandableItemIndicator.setVisibility(View.GONE);
                mImageLoader.displayImage(chapter.getImage(), thumbnailImageView);
                thumbnailImageView.setVisibility(View.VISIBLE);
                contentLayout.setPadding(0, paddingVertical, 0, paddingVertical);
                divider.setVisibility(View.VISIBLE);
            } else {
                int marginInDp;
                if (itemInfo.getLevel() > 1) {
                    divider.setVisibility(View.GONE);
                    int paddingLeft = (int) UIUtils.getPixelFromDp(context, 30);
                    contentLayout.setPadding(paddingLeft*(itemInfo.getLevel()-1), paddingVertical, 0, paddingVertical);
                    marginInDp = 12;
                } else {
                    divider.setVisibility(View.VISIBLE);
                    contentLayout.setPadding(0, paddingVertical, 0, paddingVertical);
                    marginInDp = 16;
                }
                int marginInPx = (int) UIUtils.getPixelFromDp(context, marginInDp);
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) expandableItemIndicator.getLayoutParams();

                params.rightMargin = marginInPx;

                int expandedIndicator;

                if (itemInfo.getLevel() > 1) {
                    expandedIndicator = itemInfo.isExpanded() ?
                            R.drawable.testpress_arrow_down_light : R.drawable.testpress_arrow_right_light;
                } else {
                    expandedIndicator = itemInfo.isExpanded() ?
                            R.drawable.testpress_arrow_down : R.drawable.testpress_arrow_right;
                }

                expandableItemIndicator.setImageResource(expandedIndicator);
                lblListHeader.setTypeface(TestpressSdk.getRubikRegularFont(context));
                thumbnailImageView.setVisibility(View.GONE);
                expandableItemIndicator.setVisibility(View.VISIBLE);
            }
            if (backgroundShadeItemId == chapter.getId() && itemInfo.isExpanded()) {
                backgroundShadeItemId = 0;
                animator = ViewUtils.showColorFadeAnimation(context, animator, convertView);
            }
        } else {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.testpress_content_item, null);
            TextView txtListChild = convertView.findViewById(R.id.lblListItem);
            Content content = (Content) object;
            txtListChild.setTypeface(TestpressSdk.getRubikRegularFont(context));
            txtListChild.setText(content.getTitle());
            ImageView contentImageView = convertView.findViewById(R.id.content_image);
            ImageView contentStateImageView = convertView.findViewById(R.id.content_state);
            switch (content.getContentType()) {
                case VIDEO_TYPE:
                    contentImageView.setImageResource(R.drawable.testpress_video_content_icon);
                    break;
                case ATTACHMENT_TYPE:
                    contentImageView.setImageResource(R.drawable.testpress_attachment_content_icon);
                    break;
                case HTML_TYPE:
                    contentImageView.setImageResource(R.drawable.testpress_ebook_content_icon);
                    break;
                default:
                    contentImageView.setImageResource(R.drawable.testpress_test_icon);
                    break;
            }
            Exam exam = content.getRawExam();
            if (content.getIsLocked()) {
                contentStateImageView.setImageResource(R.drawable.testpress_lock_filled);
                contentStateImageView.setVisibility(View.VISIBLE);
            } else {
                if (content.getAttemptsCount() == 0 ||
                        (content.getAttemptsCount() == 1 && exam != null &&
                                exam.getAttemptsCount() == 0 && exam.getPausedAttemptsCount() == 1)) {

                    contentStateImageView.setVisibility(View.INVISIBLE);
                } else {
                    contentStateImageView.setImageResource(R.drawable.testpress_green_tick);
                    contentStateImageView.setVisibility(View.VISIBLE);
                }
            }
        }
        return convertView;
    }

    void setBackgroundShadeItemId(long backgroundShadeItemId) {
        this.backgroundShadeItemId = backgroundShadeItemId;
        if (animator != null) {
            animator.end();
        }
    }
}

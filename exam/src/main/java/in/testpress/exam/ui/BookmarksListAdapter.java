package in.testpress.exam.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;

import org.greenrobot.greendao.query.LazyList;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.util.SingleTypeAdapter;

/**
 * Adapter that used to show the list of questions in review
 */
class BookmarksListAdapter extends SingleTypeAdapter<Bookmark> {

    private Activity activity;
    private LazyList<Bookmark> bookmarks;
    private String currentFolder;
    private int backgroundShadePosition;
    private ObjectAnimator animator;

    BookmarksListAdapter(Activity activity, String currentFolder) {
        super(activity.getLayoutInflater(), R.layout.testpress_bookmark_panel_list_item);
        this.currentFolder = currentFolder;
        this.activity = activity;
        bookmarks = Bookmark.getQueryBuilderToDisplay(activity, currentFolder).listLazy();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.content, R.id.thumbnail_image };
    }

    @Override
    public Bookmark getItem(int position) {
        return bookmarks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getCount() {
        return bookmarks.size();
    }

    @Override
    public void notifyDataSetChanged() {
        bookmarks = Bookmark.getQueryBuilderToDisplay(activity, currentFolder).listLazy();
        super.notifyDataSetChanged();
    }

    @Override
    protected void update(final int position, final Bookmark bookmark) {
        textView(0).setTypeface(TestpressSdk.getRubikRegularFont(activity));
        Object object = bookmark.getBookmarkedObject();
        if (object instanceof ReviewItem) {
            ReviewItem reviewItem = (ReviewItem) object;
            setText(0, Html.fromHtml(reviewItem.getQuestion().getQuestionHtml()).toString().trim());
            imageView(1).setImageResource(R.drawable.testpress_question_content_icon);
        } else if (object instanceof Content) {
            Content content = (Content) object;
            setText(0, content.getName());
            if (content.getHtmlId() != null) {
                imageView(1).setImageResource(R.drawable.testpress_ebook_content_icon);
            } else if (content.getVideoId() != null) {
                imageView(1).setImageResource(R.drawable.testpress_video_content_icon);
            } else if (content.getAttachmentId() != null) {
                imageView(1).setImageResource(R.drawable.testpress_attachment_content_icon);
            }
        }
    }

    @Override
    protected void update(int position, View view, Bookmark item) {
        super.update(position, view, item);
        if (backgroundShadePosition == position) {
            backgroundShadePosition = -1;
            int colorFrom = ContextCompat.getColor(activity,
                    R.color.testpress_blue_light_background_transparent);

            int colorTo = Color.WHITE;
            int duration = 2000;
            if (animator == null) {
                animator = ObjectAnimator
                        .ofObject(view, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                        .setDuration(duration);
            } else {
                animator.setTarget(view);
            }
            animator.start();
        }
    }

    public void setBackgroundShadePosition(int backgroundShadePosition) {
        this.backgroundShadePosition = backgroundShadePosition;
        if (animator != null) {
            animator.end();
        }
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }
}
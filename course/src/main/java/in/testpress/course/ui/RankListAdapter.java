package in.testpress.course.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Reputation;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class RankListAdapter extends SingleTypeAdapter<Reputation> {

    private final Context context;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int userId;
    private int startingRank = 1;

    RankListAdapter(Context context, final List<Reputation> items) {
        this(context, items, 0);
    }

    RankListAdapter(Context context, final List<Reputation> items, int userId) {
        super(context, R.layout.testpress_leaderboard_item);
        this.context = context;
        this.userId = userId;
        imageLoader = ImageUtils.initImageLoader(context);
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.testpress_profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.testpress_profile_image_place_holder)
                .showImageOnLoading(R.drawable.testpress_profile_image_place_holder).build();

        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.rank, R.id.username, R.id.user_image, R.id.trophies,
                R.id.difference, R.id.difference_image, R.id.stats_layout };
    }

    @Override
    protected void update(final int position, final Reputation reputation) {
        // Set font for the child view indices
        setTypeface(new int[] { 0, 1 }, TestpressSdk.getRubikMediumFont(context));
        setTypeface(new int[] { 3, 4 }, TestpressSdk.getRubikRegularFont(context));
        setText(0, startingRank + position + "");
        imageLoader.displayImage(reputation.getUser().getMediumImage(), imageView(2), options);
        setText(1, reputation.getUser().getDisplayName());
        setText(3, reputation.getTrophiesCount().toString());
        Integer difference = reputation.getDifference() == null ?
                reputation.getTrophiesCount() : reputation.getDifference();

        setText(4, difference.toString());
        if (difference > 0) {
            textView(4).setTextColor(ContextCompat.getColor(context, R.color.testpress_green_light));
            imageView(5).setImageResource(R.drawable.testpress_arrow_up_green);
        } else {
            textView(4).setTextColor(ContextCompat.getColor(context, R.color.testpress_red_incorrect));
            imageView(5).setImageResource(R.drawable.testpress_arrow_down_red);
        }

        // Set transparent blue background for the current user
        int color = reputation.getUser().getId() == userId ?
                Color.argb(25, 23, 147, 230) : Color.WHITE;
        view(6).setBackgroundColor(color);
    }

    void setStartingRank(int startingRank) {
        this.startingRank = startingRank;
    }

}

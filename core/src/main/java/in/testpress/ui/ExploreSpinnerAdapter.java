package in.testpress.ui;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.R;

/**
 * Adapter that provides views for our spinners.
 */
public class ExploreSpinnerAdapter extends BaseAdapter {

    private int mDotSize;
    protected boolean mTopLevel;
    protected LayoutInflater inflater;
    private Resources resources;
    private boolean hideSpinner; // Icon will be used instead of showing the selected item in spinner.
    private int layoutId;

    public ExploreSpinnerAdapter(LayoutInflater inflater, Resources resources, boolean topLevel) {
        this.inflater = inflater;
        this.resources = resources;
        this.mTopLevel = topLevel;
    }

    private class ExploreSpinnerItem {
        boolean isHeader;
        String tag, title;
        int color;
        boolean indented;

        ExploreSpinnerItem(boolean isHeader, String tag, String title, boolean indented, int color) {
            this.isHeader = isHeader;
            this.tag = tag;
            this.title = title;
            this.indented = indented;
            this.color = color;
        }
    }

    // Pairs of (tag, title)
    private ArrayList<ExploreSpinnerItem> mItems = new ArrayList<ExploreSpinnerItem>();

    public void clear() {
        mItems.clear();
    }

    /**
     *
     * @param items List of items to add as indented items
     */
    public void addItems(List<String> items) {
        for (String item : items) {
            mItems.add(new ExploreSpinnerItem(false, item, item, true, 0));
        }
    }

    public void addItem(String tag, String title, boolean indented, int color) {
        mItems.add(new ExploreSpinnerItem(false, tag, title, indented, color));
    }

    public void addItem(int index, String tag, String title, boolean indented, int color) {
        mItems.add(index, new ExploreSpinnerItem(false, tag, title, indented, color));
    }

    public void updateItem(int index, String tag, String title, boolean indented, int color) {
        mItems.set(index, new ExploreSpinnerItem(false, tag, title, indented, color));
    }

    public void removeItem(int index) {
        mItems.remove(index);
    }

    public void addHeader(String title) {
        mItems.add(new ExploreSpinnerItem(true, "", title, false, 0));
    }


    /**
     * @param title Title of item
     */
    public int getItemPosition(String title) {
        for (ExploreSpinnerItem item : mItems) {
            if (item.title.equals(title)) {
                return mItems.indexOf(item);
            }
        }
        return -1;
    }

    /**
     * @param tag Tag of item
     */
    public int getItemPositionFromTag(String tag) {
        for (ExploreSpinnerItem item : mItems) {
            if (item.tag.equals(tag)) {
                return mItems.indexOf(item);
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected boolean isHeader(int position) {
        return position >= 0 && position < mItems.size()
                && mItems.get(position).isHeader;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_explore_spinner_item_dropdown,
                    parent, false);
            view.setTag("DROPDOWN");
        }

        TextView headerTextView = (TextView) view.findViewById(R.id.header_text);
        View dividerView = view.findViewById(R.id.divider_view);
        TextView normalTextView = (TextView) view.findViewById(android.R.id.text1);

        if (isHeader(position)) {
            headerTextView.setText(getTitle(position));
            headerTextView.setVisibility(View.VISIBLE);
            normalTextView.setVisibility(View.GONE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            headerTextView.setVisibility(View.GONE);
            normalTextView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.GONE);

            setUpNormalDropdownView(position, normalTextView);
        }

        return view;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = inflater.inflate(getLayoutId(), parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));
        if (hideSpinner) {
            view.setVisibility(View.GONE);
        }
        return view;
    }

    @LayoutRes
    protected int getLayoutId() {
        if (layoutId != 0) {
            return layoutId;
        }
        return mTopLevel
                ? R.layout.testpress_explore_spinner_item_actionbar
                : R.layout.testpress_explore_spinner_item;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public void hideSpinner(boolean hideSelectedItem) {
        this.hideSpinner = hideSelectedItem;
    }

    public String getTitle(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).title : "";
    }

    public int getColor(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).color : 0;
    }

    public String getTag(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).tag : "";
    }

    protected void setUpNormalDropdownView(int position, TextView textView) {
        textView.setText(getTitle(position));
        ShapeDrawable colorDrawable = (ShapeDrawable) textView.getCompoundDrawables()[2];
        int color = getColor(position);
        if (color == 0) {
            if (colorDrawable != null) {
                textView.setCompoundDrawables(null, null, null, null);
            }
        } else {
            if (mDotSize == 0) {
                mDotSize = resources.getDimensionPixelSize(
                        R.dimen.testpress_tag_color_dot_size);
            }
            if (colorDrawable == null) {
                colorDrawable = new ShapeDrawable(new OvalShape());
                colorDrawable.setIntrinsicWidth(mDotSize);
                colorDrawable.setIntrinsicHeight(mDotSize);
                colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, colorDrawable, null);
            }
            colorDrawable.getPaint().setColor(color);
        }

    }

    @Override
    public boolean isEnabled(int position) {
        return !isHeader(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
}
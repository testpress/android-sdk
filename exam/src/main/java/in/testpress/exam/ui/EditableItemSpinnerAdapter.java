package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import in.testpress.exam.R;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.UIUtils;

import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

public class EditableItemSpinnerAdapter extends ExploreSpinnerAdapter {

    private OnEditItemListener onEditItemListener;
    private Activity activity;
    protected ArrayList<Integer> counts = new ArrayList<>();

    public EditableItemSpinnerAdapter(Activity activity, boolean topLevel,
                                      OnEditItemListener onEditItemListener) {

        super(activity.getLayoutInflater(), activity.getResources(), topLevel);
        this.onEditItemListener = onEditItemListener;
        this.activity = activity;
    }

    public EditableItemSpinnerAdapter(Activity activity, OnEditItemListener onEditItemListener) {
        this(activity, false, onEditItemListener);
    }

    public void addItem(String tag, String title, int count) {
        super.addItem(tag, title, false, 0);
        counts.add(count);
    }

    public void addItem(int index, String tag, String title, int count) {
        super.addItem(index, tag, title, false, 0);
        counts.add(index, count);
    }

    public void updateItem(int index, String tag, String title, int count) {
        super.updateItem(index, tag, title, false, 0);
        counts.set(index, count);
    }

    @Override
    public void clear() {
        super.clear();
        counts.clear();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getDropDownView(final int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView headerTextView = view.findViewById(R.id.header_text);
        View dividerView = view.findViewById(R.id.divider_view);
        LinearLayout itemTextLayout = view.findViewById(R.id.item_text_layout);
        if (position == 0) {
            if (!isHeader(position)) {
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) itemTextLayout.getLayoutParams();

                params.topMargin = (int) UIUtils.getPixelFromDp(activity, 8);
                itemTextLayout.setLayoutParams(params);
            }
        } else {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) itemTextLayout.getLayoutParams();

            params.topMargin = 0;
            itemTextLayout.setLayoutParams(params);
        }
        if (isHeader(position)) {
            headerTextView.setText(getTitle(position));
            headerTextView.setVisibility(View.VISIBLE);
            itemTextLayout.setVisibility(View.GONE);
        } else {
            headerTextView.setVisibility(View.GONE);
            itemTextLayout.setVisibility(View.VISIBLE);
            TextView normalTextView = view.findViewById(R.id.text);
            setUpNormalDropdownView(position, normalTextView);
        }
        dividerView.setVisibility(View.GONE);

        TextView countTextView = view.findViewById(R.id.count);
        ImageView editButton = view.findViewById(R.id.edit);
        if (getTag(position).isEmpty() || getTag(position).equals(UNCATEGORIZED)) {
            countTextView.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEditItemListener.onClickEdit(position);
                }
            });
            countTextView.setVisibility(View.VISIBLE);
            Integer count = counts.get(position);
            if (count < 0) {
                count = 0;
            }
            countTextView.setText(count.toString());
        }
        return view;
    }

    public interface OnEditItemListener {
        void onClickEdit(int position);
    }

}
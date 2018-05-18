package in.testpress.exam.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.testpress.exam.R;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.UIUtils;

import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

public class EditableItemSpinnerAdapter extends ExploreSpinnerAdapter {

    private OnEditItemListener onEditItemListener;
    private Activity activity;

    public EditableItemSpinnerAdapter(Activity activity, boolean topLevel,
                                      OnEditItemListener onEditItemListener) {

        super(activity.getLayoutInflater(), activity.getResources(), topLevel);
        this.onEditItemListener = onEditItemListener;
        this.activity = activity;
    }

    public EditableItemSpinnerAdapter(Activity activity, OnEditItemListener onEditItemListener) {
        this(activity, false, onEditItemListener);
    }

    @Override
    public View getDropDownView(final int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView headerTextView = view.findViewById(R.id.header_text);
        View dividerView = view.findViewById(R.id.divider_view);
        LinearLayout itemTextLayout = view.findViewById(R.id.item_text_layout);
        LinearLayout editLayout = view.findViewById(R.id.edit);
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

        if (getTag(position).isEmpty() || getTag(position).equals(UNCATEGORIZED)) {
            editLayout.setVisibility(View.GONE);
        } else {
            editLayout.setVisibility(View.VISIBLE);
            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEditItemListener.onClickEdit(position);
                }
            });
        }
        return view;
    }

    public interface OnEditItemListener {
        void onClickEdit(int position);
    }

}
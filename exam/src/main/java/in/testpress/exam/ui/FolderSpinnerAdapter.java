package in.testpress.exam.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.testpress.exam.R;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

public class FolderSpinnerAdapter extends ExploreSpinnerAdapter {

    private Activity activity;
    private ViewUtils.OnInputCompletedListener inputCompletedListener;

    public FolderSpinnerAdapter(Activity activity, Resources resources, boolean topLevel,
                         ViewUtils.OnInputCompletedListener inputCompletedListener) {

        super(activity.getLayoutInflater(), resources, topLevel);
        this.activity = activity;
        this.inputCompletedListener = inputCompletedListener;
    }

    public FolderSpinnerAdapter(Activity activity, Resources resources,
                         ViewUtils.OnInputCompletedListener inputCompletedListener) {

        this(activity, resources, false, inputCompletedListener);
    }

    @Override
    public View getDropDownView(final int position, View view, ViewGroup parent) {
        if (position == getCount() - 1) {
            if (view == null || !view.getTag().toString().equals("BUTTON")) {
                view = inflater.inflate(R.layout.testpress_add_folder_button_layout,
                        parent, false);

                view.findViewById(R.id.add_folder).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewUtils.showInputDialogBox(
                                activity,
                                activity.getString(R.string.testpress_enter_folder_name),
                                inputCompletedListener
                        );
                    }
                });
                view.setTag("BUTTON");
            }
            if (getCount() > 1) {
                view.findViewById(R.id.add_folder_separator).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.add_folder_separator).setVisibility(View.GONE);
            }
            return view;
        }
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_spinner_item_dropdown,
                    parent, false);
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
        return view;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }
}
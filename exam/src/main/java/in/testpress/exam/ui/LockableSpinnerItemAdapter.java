package in.testpress.exam.ui;

import android.app.Activity;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import in.testpress.exam.R;

public class LockableSpinnerItemAdapter extends PlainSpinnerItemAdapter {

    private int selectedItem;

    public LockableSpinnerItemAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getDropDownView(final int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_lockable_spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView itemTitle = view.findViewById(R.id.text);
        setUpNormalDropdownView(position, itemTitle);
        ImageView currentStateImage = view.findViewById(R.id.current_state_image);
        int imageColor;
        int textColor;
        int imageResId;
        if (selectedItem == position) {
            imageColor = ContextCompat.getColor(activity, R.color.testpress_green);
            textColor = ContextCompat.getColor(activity, R.color.testpress_black);
            imageResId = R.drawable.testpress_check_mark;
        } else {
            imageColor = Color.parseColor("#bfbfbf");
            textColor = Color.parseColor("#bfbfbf");
            if (selectedItem > position) {
                imageResId = R.drawable.ic_lock_with_tick_18dp;
            } else {
                imageResId = R.drawable.ic_lock_outline_18dp;
            }
        }
        itemTitle.setTextColor(textColor);
        currentStateImage.setColorFilter(imageColor);
        currentStateImage.setImageResource(imageResId);
        return view;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public int getSelectedItem() {
        return selectedItem;
    }
}
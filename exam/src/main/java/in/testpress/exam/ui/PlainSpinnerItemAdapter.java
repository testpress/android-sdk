package in.testpress.exam.ui;

import android.app.Activity;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import in.testpress.exam.R;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.ViewUtils;

public class PlainSpinnerItemAdapter extends ExploreSpinnerAdapter {

    protected Activity activity;
    private boolean showSectionInfo = false;
    private SectionInfoClickListener sectionInfoClickListener;

    public PlainSpinnerItemAdapter(Activity activity) {
        super(activity.getLayoutInflater(), activity.getResources(), false);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = inflater.inflate(R.layout.testpress_plain_spinner_item, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));
        Button info = view.findViewById(R.id.info_button);
        if (showSectionInfo) {
            info.setVisibility(View.VISIBLE);
        }
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sectionInfoClickListener.showInfo();
            }
        });
        ViewUtils.setDrawableColor(textView, R.color.testpress_black);
        return view;
    }

    public void showSectionInfoButton(boolean showSectionInfo) {
        this.showSectionInfo = showSectionInfo;
    }

    public void setSectionInfoClickListener(SectionInfoClickListener listener) {
        this.sectionInfoClickListener = listener;
    }

    @Override
    protected void setUpNormalDropdownView(int position, TextView textView) {
        super.setUpNormalDropdownView(position, textView);
        textView.setGravity(Gravity.CENTER);
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        view = super.getDropDownView(position, view, parent);
        View dividerView = view.findViewById(R.id.divider_view);
        dividerView.setBackgroundColor(ContextCompat.getColor(activity, R.color.testpress_gray_light));
        dividerView.setVisibility(View.VISIBLE);
        return view;
    }

    public interface SectionInfoClickListener {
        void showInfo();
    }
}

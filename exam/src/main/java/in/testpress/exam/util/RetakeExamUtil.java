package in.testpress.exam.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.util.UIUtils;

public class RetakeExamUtil {

    public static void showRetakeOptions(Context context, final SelectionListener selectionListener) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_retake_with);

        String[] retakeOptions =
                context.getResources().getStringArray(R.array.testpress_retake_options);

        RetakeOptionsAdapter optionsAdapter =
                new RetakeOptionsAdapter(context, retakeOptions, selectionListener);

        builder.setAdapter(optionsAdapter, null);
        AlertDialog dialog = builder.show();
        optionsAdapter.setDialog(dialog);
        ListView listView = dialog.getListView();
        int padding = (int) UIUtils.getPixelFromDp(context, 25);
        int topPadding = (int) UIUtils.getPixelFromDp(context, 10);
        listView.setPadding(padding, topPadding, padding, padding);
        listView.setDividerHeight((int) UIUtils.getPixelFromDp(context, 15));
    }

    private static class RetakeOptionsAdapter extends ArrayAdapter<String> {

        private AlertDialog dialog;
        private SelectionListener selectionListener;

        RetakeOptionsAdapter(@NonNull Context context, @NonNull String[] objects,
                             SelectionListener selectionListener) {

            super(context, R.layout.testpress_retake_option_button, objects);
            this.selectionListener = selectionListener;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView,
                            @NonNull ViewGroup parent) {

            final Button item = (Button) super.getView(position, convertView, parent);
            item.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isPartial = position == 1;
                    selectionListener.onOptionSelected(isPartial);
                    dialog.dismiss();
                }
            });
            return item;
        }

        public void setDialog(AlertDialog dialog) {
            this.dialog = dialog;
        }
    }

    public interface SelectionListener {
        void onOptionSelected(boolean isPartial);
    }
}

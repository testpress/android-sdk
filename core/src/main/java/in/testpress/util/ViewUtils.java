package in.testpress.util;

/*
 * Copyright 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import in.testpress.R;
import in.testpress.core.TestpressException;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Utilities for working with the {@link View} class
 */
public class ViewUtils {

    /**
     * Set visibility of given view to be gone or visible
     * <p/>
     * This method has no effect if the view visibility is currently invisible
     *
     * @param view
     * @param gone
     * @return view
     */
    public static <V extends View> V setGone(final V view, final boolean gone) {
        if (view != null)
            if (gone) {
                if (GONE != view.getVisibility())
                    view.setVisibility(GONE);
            } else {
                if (VISIBLE != view.getVisibility())
                    view.setVisibility(VISIBLE);
            }
        return view;
    }

    /**
     * Set visibility of given view to be invisible or visible
     * <p/>
     * This method has no effect if the view visibility is currently gone
     *
     * @param view
     * @param invisible
     * @return view
     */
    public static <V extends View> V setInvisible(final V view,
                                                  final boolean invisible) {
        if (view != null)
            if (invisible) {
                if (INVISIBLE != view.getVisibility())
                    view.setVisibility(INVISIBLE);
            } else {
                if (VISIBLE != view.getVisibility())
                    view.setVisibility(VISIBLE);
            }
        return view;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void slide_down(Context ctx, View v){
        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.testpress_slide_down);
        if(a != null){
            a.reset();
            if(v != null){
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public static void setMenuIconsColor(Context context, MenuItem[] menuItems) {
        for (MenuItem menuItem : menuItems) {
            Drawable searchIcon = menuItem.getIcon();
            searchIcon.mutate().setColorFilter(ContextCompat.getColor(context,
                    R.color.testpress_actionbar_text), PorterDuff.Mode.SRC_IN);
            menuItem.setIcon(searchIcon);
        }
    }

    public static void setSpinnerIconColor(Context context, Spinner spinner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spinner.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.testpress_actionbar_text));
        } else {
            ViewCompat.setBackgroundTintList(spinner,
                    ContextCompat.getColorStateList(context, R.color.testpress_actionbar_text));
        }
    }

    public static void setTypeface(TextView[] textViews, Typeface typeface) {
        for (TextView view : textViews) {
            view.setTypeface(typeface);
        }
    }

    public static void setLeftDrawable(Context context, Button button, @DrawableRes int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        assert drawable != null;
        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context,
                R.color.testpress_button_text_color), PorterDuff.Mode.SRC_IN));
        button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        button.setCompoundDrawablePadding((int) context.getResources().getDimension(
                R.dimen.testpress_button_left_drawable_padding));
    }

    public static void setDrawableColor(TextView textView, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(textView.getContext(), colorRes);
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    // Used to put ListView inside ScrollView
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @SuppressLint("InflateParams")
    public static void showInputDialogBox(final Activity activity, String title,
                                          final OnInputCompletedListener inputCompletedListener) {

        final View dialogView =
                activity.getLayoutInflater().inflate(R.layout.testpress_edit_text_dialog_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                R.style.TestpressAppCompatAlertDialogStyle);

        builder.setTitle(title);
        builder.setView(dialogView);
        final EditText editText = dialogView.findViewById(R.id.edit_text);
        builder.setPositiveButton(activity.getString(R.string.testpress_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        UIUtils.hideSoftKeyboard(activity, editText);
                        String inputText = editText.getText().toString();
                        if (inputText.trim().isEmpty()) {
                            return;
                        }
                        inputCompletedListener.onInputComplete(inputText);
                    }
        });
        builder.setNegativeButton(activity.getString(R.string.testpress_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        UIUtils.hideSoftKeyboard(activity, editText);
                    }
        });
        Dialog dialog = builder.create();
        //noinspection ConstantConditions
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    public interface OnInputCompletedListener {
        void onInputComplete(String inputText);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void handleException(TestpressException exception, View rootLayout) {
        handleException(exception, rootLayout, R.string.testpress_some_thing_went_wrong_try_again);
    }

    public static void handleException(TestpressException exception, View rootLayout,
                                       @StringRes int clientErrorMessage) {

        if(exception.isUnauthenticated()) {
            showSnackbar(rootLayout, R.string.testpress_authentication_failed);
        } else if (exception.isNetworkError()) {
            showSnackbar(rootLayout, R.string.testpress_no_internet_connection);
        } else if (exception.isClientError()) {
            showSnackbar(rootLayout, clientErrorMessage);
        } else {
            showSnackbar(rootLayout, R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    public static void showSnackbar(View rootLayout, @StringRes int message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void setTextViewDrawableColor(TextView textView, @ColorRes int color, Context context) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
        drawable.setColorFilter(
            new PorterDuffColorFilter(
                ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private ViewUtils() {
    }
}

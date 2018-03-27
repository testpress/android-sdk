package in.testpress.exam.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import in.testpress.exam.R;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.ui.ExploreSpinnerAdapter;

public class MultiLanguagesUtil {

    public static void supportMultiLanguage(final Activity activity, final Exam exam, Button startButton,
                                            final LanguageSelectionListener listener) {

        View languageLayout = activity.findViewById(R.id.language_layout);
        final ArrayList<Language> languages = new ArrayList<>(exam.getLanguages());
        if (languages.size() > 1) {
            final ExploreSpinnerAdapter languageSpinnerAdapter =
                    new ExploreSpinnerAdapter(activity.getLayoutInflater(), activity.getResources(), false);

            languageSpinnerAdapter.addHeader("--Select--");
            for (Language language : languages) {
                languageSpinnerAdapter.addItem(language.getCode(), language.getTitle(), true, 0);
            }
            final Spinner languageSpinner = (Spinner) activity.findViewById(R.id.language_spinner);
            languageSpinner.setAdapter(languageSpinnerAdapter);
            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {

                    exam.setSelectedLanguage(languageSpinnerAdapter.getTag(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            languageLayout.setVisibility(View.VISIBLE);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedItemTag =
                            languageSpinnerAdapter.getTag(languageSpinner.getSelectedItemPosition());

                    if (!selectedItemTag.isEmpty()) {
                        listener.onLanguageSelected();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                activity, R.style.TestpressAppCompatAlertDialogStyle)
                                .setTitle(R.string.testpress_select_language)
                                .setPositiveButton(R.string.testpress_continue,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {

                                                listener.onLanguageSelected();
                                            }
                                        });
                        CharSequence[] languageTitles = new CharSequence[languages.size()];
                        for (int i = 0; i < languages.size(); i++) {
                            languageTitles[i] = languages.get(i).getTitle();
                        }
                        builder.setSingleChoiceItems(languageTitles, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int checkedId) {
                                        languageSpinner.setSelection(checkedId + 1);
                                        Button continueButton = ((AlertDialog) dialog)
                                                .getButton(DialogInterface.BUTTON_POSITIVE);

                                        continueButton.setEnabled(true);
                                    }
                                });
                        AlertDialog dialog = builder.show();
                        Button continueButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        continueButton.setEnabled(false);
                    }
                }
            });
        } else {
            languageLayout.setVisibility(View.GONE);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLanguageSelected();
                }
            });
        }
    }

    public interface LanguageSelectionListener {
        void onLanguageSelected();
    }
}

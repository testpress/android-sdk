package in.testpress.exam.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.TestpressSDK;
import in.testpress.ui.ExploreSpinnerAdapter;

public class MultiLanguagesUtil {

    private static List<Language> languages = new List<Language>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @NonNull
        @Override
        public Iterator<Language> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] a) {
            return null;
        }

        @Override
        public boolean add(Language language) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends Language> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, @NonNull Collection<? extends Language> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public Language get(int index) {
            return null;
        }

        @Override
        public Language set(int index, Language element) {
            return null;
        }

        @Override
        public void add(int index, Language element) {

        }

        @Override
        public Language remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @NonNull
        @Override
        public ListIterator<Language> listIterator() {
            return null;
        }

        @NonNull
        @Override
        public ListIterator<Language> listIterator(int index) {
            return null;
        }

        @NonNull
        @Override
        public List<Language> subList(int fromIndex, int toIndex) {
            return null;
        }
    };
    private static Activity activity;
    public static void supportMultiLanguage(final Activity activity, final Exam exam, Button startButton,
                                            final LanguageSelectionListener listener) {

        View languageLayout = activity.findViewById(R.id.language_layout);
        languages.set(0, new Language("en", "English", exam.getSlug()));
        Log.e("Inside","MultiLanguagesUtil-before loadExamLanguage");
        //loadExamLanguage(activity, exam.getSlug());
        Log.e("Inside","MultiLanguagesUtil-after loadExamLanguage");
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

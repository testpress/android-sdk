package in.testpress.exam.ui.fragments.attempt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;

import in.testpress.exam.R;
import in.testpress.exam.ui.LockableSpinnerItemAdapter;
import in.testpress.exam.ui.PlainSpinnerItemAdapter;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.ui.ExploreSpinnerAdapter;

public class SectionsFilterFragment extends Fragment {
    Spinner sectionsFilter;
    boolean sectionsLocked;
    ArrayList<AttemptSection> sections;
    ExploreSpinnerAdapter adapter;
    public static final String PARAM_ARE_SECTIONS_LOCKED = "locked";
    public static final String PARAM_SECTIONS = "sections";
    private OnSectionSelectedListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sectionsLocked = getArguments().getBoolean(PARAM_ARE_SECTIONS_LOCKED);
        sections = getArguments().getParcelableArrayList(PARAM_SECTIONS);
        initializeListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.attempt_sections_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sectionsFilter = view.findViewById(R.id.attempt_sections_filter);
        initializeAdapter();
    }

    private void initializeAdapter() {
        if (sectionsLocked) {
            adapter = new LockableSpinnerItemAdapter(getActivity());
        } else {
            adapter = new PlainSpinnerItemAdapter(getActivity());
        }
        for (AttemptSection section : sections) {
            adapter.addItem(section.getName(), section.getName(), true, 0);
        }
        sectionsFilter.setAdapter(adapter);
        sectionsFilter.setOnItemSelectedListener(new SectionItemSelectListener());
//            ((LockableSpinnerItemAdapter) adapter).setSelectedItem(attempt.getCurrentSectionPosition());
//            sectionsFilter.setSelection(attempt.getCurrentSectionPosition());
    }

    public void initializeListener()
    {
        Fragment parentFragment = getParentFragment();
        try
        {
            listener = (OnSectionSelectedListener)parentFragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    parentFragment.toString() + " must implement OnSectionSelectedListener");
        }
    }

    public interface OnSectionSelectedListener {
        // This can be any number of events to be sent to the activity
        public int getCurrentSectionPosition();

        public void lockedSectionSelected(String value, int position);

        public void sectionSelected(String value, int position);
    }

    private class SectionItemSelectListener implements AdapterView.OnItemSelectedListener {
        boolean firstTimeCallback;

        @Override
        public void onItemSelected(AdapterView<?> spinner, View view, int position,
                                   long itemId) {

            if (!firstTimeCallback) {
                firstTimeCallback = true;
                return;
            }

            if (position == listener.getCurrentSectionPosition()) {
                return;
            }

            //Inform TestFragment

            // If no sectional lock, change the viewpager
            // If sectional lock, show alert

//

            String value = adapter.getTag(position);
            if (sectionsLocked) {
                listener.lockedSectionSelected(value, position);
            } else {
                listener.sectionSelected(value, position);
            }

            //TODO This line might not be needed. This was present in original code.
            // Need to check and remove
            // sectionsFilter.setSelection(attempt.getCurrentSectionPosition());

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }
}

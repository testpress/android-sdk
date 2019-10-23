package in.testpress.exam.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Subject;

import static in.testpress.exam.ui.AnalyticsFragment.ANALYTICS_URL_FRAG;
import static in.testpress.exam.ui.AnalyticsFragment.SUBJECTS;

public class IndividualSubjectAnalyticsFragment extends Fragment {

    private RecyclerView listView;
    private RecyclerView individualList;
    private String analyticsUrlFrag;
    private List<Subject> subjects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjects = getArguments().getParcelableArrayList(SUBJECTS);
        }
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalStateException("Subjects must not be null or empty");
        }
        analyticsUrlFrag = getArguments().getString(ANALYTICS_URL_FRAG);
        Assert.assertNotNull("ANALYTICS_URL_FRAG must not be null", analyticsUrlFrag);
        Subject.sortSubjects(subjects);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.testpress_individual_subject_analytics_fragment, container, false);
        listView = (RecyclerView) view.findViewById(R.id.subjects_answer_count_list);
        individualList = (RecyclerView) view.findViewById(R.id.individual_subjects_list);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setNestedScrollingEnabled(false);
        individualList.setNestedScrollingEnabled(false);
        individualList.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        individualList.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(
                new SubjectAnswersCountListAdapter(getActivity(), subjects, analyticsUrlFrag));
        IndividualSubjectAnalyticsGraphAdapter listAdapter =
                new IndividualSubjectAnalyticsGraphAdapter(getActivity(), subjects);
        individualList.setAdapter(listAdapter);
    }

}

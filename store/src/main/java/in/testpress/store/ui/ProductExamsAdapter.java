package in.testpress.store.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import in.testpress.exam.models.Exam;
import in.testpress.store.R;

public class ProductExamsAdapter extends ArrayAdapter<Exam> {
    private static class ViewHolder {
        TextView title;
        TextView date;
        TextView noOfQuestions;
        TextView duration;
        TextView courseCategory;
        View courseCategoryLayout;
    }

    public ProductExamsAdapter(Context context, List<Exam> exams) {
        super(context, R.layout.testpress_product_exams_list_item, exams);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.testpress_product_exams_list_item, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.exam_title);
            viewHolder.date = (TextView) convertView.findViewById(R.id.exam_date);
            viewHolder.noOfQuestions = (TextView) convertView.findViewById(R.id.number_of_questions);
            viewHolder.duration = (TextView) convertView.findViewById(R.id.exam_duration);
            viewHolder.courseCategory = (TextView) convertView.findViewById(R.id.course_category);
            viewHolder.courseCategoryLayout = convertView.findViewById(R.id.course_category_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Get the data item for this position
        Exam exam = getItem(position);
        if (exam != null) {
            // Populate the data into the template view using the data object
            viewHolder.title.setText(exam.getTitle());
            viewHolder.date.setText(exam.getFormattedStartDate() + " to " +
                    exam.getFormattedEndDate());

            viewHolder.noOfQuestions.setText(exam.getNumberOfQuestionsString());
            viewHolder.duration.setText(exam.getDuration());

            if (!exam.getCategories().isEmpty()) {
                viewHolder.courseCategoryLayout.setVisibility(View.VISIBLE);
                String categories = Arrays.toString(exam.getCategories().toArray());
                viewHolder.courseCategory.setText(categories.substring(1, categories.length() - 1));
            } else {
                viewHolder.courseCategoryLayout.setVisibility(View.GONE);
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }
}

package in.testpress.store.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import in.testpress.store.R;
import in.testpress.store.models.InstallmentPlan;

import in.testpress.util.UIUtils;


public class InstallmentPlanListBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PLANS = "plans";
    private static final String ARG_PRODUCT_PRICE = "product_price";

    private List<InstallmentPlan> plans;
    private String productPrice;
    private PlanSelectedListener listener;

    public static InstallmentPlanListBottomSheet newInstance(List<InstallmentPlan> plans, String productPrice) {
        InstallmentPlanListBottomSheet sheet = new InstallmentPlanListBottomSheet();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PLANS, new ArrayList<>(plans));
        args.putString(ARG_PRODUCT_PRICE, productPrice);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plans = getArguments().getParcelableArrayList(ARG_PLANS);
            productPrice = getArguments().getString(ARG_PRODUCT_PRICE);
        }
    }

    public void setPlanSelectedListener(PlanSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_installment_plan_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSubtitle(view);
        populatePlans(view);
    }

    private void setupSubtitle(View view) {
        TextView subtitle = view.findViewById(R.id.sheet_subtitle);
        subtitle.setText(getString(R.string.installment_plan_subtitle, productPrice));
    }

    private void populatePlans(View view) {
        if (plans == null || plans.isEmpty()) {
            dismiss();
            return;
        }

        LinearLayout container = view.findViewById(R.id.plans_container);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (InstallmentPlan plan : plans) {
            addPlanRow(container, inflater, plan);
        }
    }

    private void addPlanRow(LinearLayout container, LayoutInflater inflater, InstallmentPlan plan) {
        View row = inflater.inflate(R.layout.item_installment_plan, container, false);
        TextView nameView = row.findViewById(R.id.plan_display_name);
        nameView.setText(plan.getDisplayName());

        row.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlanSelected(plan);
            }
            dismiss();
        });

        container.addView(row);
    }

    public interface PlanSelectedListener {
        void onPlanSelected(InstallmentPlan plan);
    }
}
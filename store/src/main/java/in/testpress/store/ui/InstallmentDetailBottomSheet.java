package in.testpress.store.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import in.testpress.store.R;
import in.testpress.store.models.Installment;
import in.testpress.store.models.InstallmentPlan;
import in.testpress.util.DateUtils;
import in.testpress.util.StringUtils;

public class InstallmentDetailBottomSheet extends BottomSheetDialogFragment {

    private InstallmentPlan plan;
    private boolean showBack;
    private PayInstallmentListener listener;
    private Runnable onBackPressed;

    private static final String ARG_PLAN = "plan";
    private static final String ARG_SHOW_BACK = "showBack";

    public static InstallmentDetailBottomSheet newInstance(InstallmentPlan plan, boolean showBack) {
        InstallmentDetailBottomSheet sheet = new InstallmentDetailBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLAN, plan);
        args.putBoolean(ARG_SHOW_BACK, showBack);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plan = getArguments().getParcelable(ARG_PLAN);
            showBack = getArguments().getBoolean(ARG_SHOW_BACK);
        }
    }

    public void setPayInstallmentListener(PayInstallmentListener listener) {
        this.listener = listener;
    }

    public void setOnBackPressedCallback(Runnable callback) {
        this.onBackPressed = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_installment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Installment> installments = plan.getInstallments();
        if (installments == null || installments.isEmpty()) {
            dismiss();
            return;
        }

        setupHeader(view);
        Installment currentInstallment = findCurrentInstallment(installments);
        setupTimeline(view, installments);
        setupFooter(view, currentInstallment);
    }

    private void setupHeader(View view) {
        TextView titleView = view.findViewById(R.id.sheet_title);
        titleView.setText(plan.getDisplayName());

        TextView btnBack = view.findViewById(R.id.btn_back);
        if (showBack) {
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(v -> {
                dismiss();
                if (onBackPressed != null) onBackPressed.run();
            });
        }
    }

    private Installment findCurrentInstallment(List<Installment> installments) {
        for (Installment inst : installments) {
            if (Boolean.TRUE.equals(inst.getIsCurrentInstallment())) {
                return inst;
            }
        }
        return null;
    }

    private void setupTimeline(View view, List<Installment> installments) {
        LinearLayout timelineContainer = view.findViewById(R.id.timeline_container);
        LayoutInflater rowInflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < installments.size(); i++) {
            boolean isLast = (i == installments.size() - 1);
            View row = rowInflater.inflate(R.layout.item_installment_timeline, timelineContainer, false);
            setupInstallmentRow(row, installments.get(i), isLast);
            timelineContainer.addView(row);
        }
    }

    private void setupInstallmentRow(View row, Installment installment, boolean isLast) {
        ImageView icon = row.findViewById(R.id.timeline_icon);
        View line = row.findViewById(R.id.timeline_line);
        TextView label = row.findViewById(R.id.installment_label);
        TextView payNowBadge = row.findViewById(R.id.pay_now_badge);
        TextView amount = row.findViewById(R.id.installment_amount);
        TextView dueDate = row.findViewById(R.id.installment_due_date);

        if (isLast) {
            line.setVisibility(View.INVISIBLE);
        }

        String ordinal = StringUtils.getOrdinal(installment.getOrder() + 1);
        label.setText(getString(R.string.installment_label, ordinal));
        amount.setText(getString(R.string.testpress_amount_with_symbol, installment.getPrice()));
        dueDate.setText(getString(R.string.installment_due_on, DateUtils.formatDateToReadable(installment.getDueDate())));

        setupTimelineIcon(icon, installment);

        if (Boolean.TRUE.equals(installment.getIsCurrentInstallment())) {
            payNowBadge.setVisibility(View.VISIBLE);
        }
    }

    private void setupTimelineIcon(ImageView icon, Installment installment) {
        int color;
        if (Boolean.TRUE.equals(installment.getIsPaid())) {
            color = ContextCompat.getColor(requireContext(), R.color.testpress_green);
        } else {
            color = Color.parseColor("#9ca3af");
        }

        ((View) icon.getParent()).setBackgroundResource(R.drawable.circle_gray_background);
        ((View) icon.getParent()).getBackground().setTint(color);
        icon.setImageResource(R.drawable.ic_tick_white);
    }

    private void setupFooter(View view, Installment currentInstallment) {
        TextView totalAmountView = view.findViewById(R.id.total_amount);
        totalAmountView.setText(getString(R.string.testpress_amount_with_symbol, plan.getPrice()));

        TextView payButton = view.findViewById(R.id.btn_pay_installment);
        if (currentInstallment != null) {
            String ordinal = StringUtils.getOrdinal(currentInstallment.getOrder() + 1);
            payButton.setText(getString(R.string.installment_pay_button_label, ordinal));
        } else {
            payButton.setText("Pay Installment");
        }

        payButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPayInstallment(plan.getId());
            }
            dismiss();
        });
    }


    public interface PayInstallmentListener {
        void onPayInstallment(int planId);
    }
}

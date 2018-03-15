package in.testpress.store.payu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import in.testpress.store.R;

import java.util.ArrayList;
import java.util.List;

public class PaymentOptionsAdapter
        extends RecyclerView.Adapter<PaymentOptionsAdapter.PaymentOptionViewHolder> {

    private List<PaymentOptionModel> paymentOptionModels;
    private LayoutInflater layoutInflater;
    private OnRecyclerItemClickListener recyclerItemClickListener;

    PaymentOptionsAdapter(Context context, List<PaymentOptionModel> optionModels) {
        this.paymentOptionModels = new ArrayList<>();
        this.paymentOptionModels.addAll(optionModels);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public PaymentOptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.testpress_payment_option_card, parent, false);
        return new PaymentOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PaymentOptionViewHolder holder, int position) {
        PaymentOptionModel optionModel = paymentOptionModels.get(position);
        holder.name.setText(optionModel.getName());
        holder.icon.setImageResource(optionModel.getIcon());
    }

    @Override
    public int getItemCount() {
        return paymentOptionModels.size();
    }

    class PaymentOptionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        PaymentOptionViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.image_view);
            name = (TextView) itemView.findViewById(R.id.bank_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerItemClickListener.onRecyclerItemClicked(view, getAdapterPosition());
                }
            });
        }
    }

    public interface OnRecyclerItemClickListener {
        void onRecyclerItemClicked(View view, int position);
    }

    void setOnRecyclerItemClickListener(OnRecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}

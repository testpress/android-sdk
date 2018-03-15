package in.testpress.store.payu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.ArrayList;

import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.store.payu.PaymentModeActivity.ACTION_BACK_PRESSED;

public class NetBankingActivity extends BaseToolBarActivity
        implements NetBankingListAdapter.OnRecyclerItemClickListener, TextWatcher {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private Intent intent;
    private PaymentParams paymentParams;
    private PayuConfig payuConfig;
    private AlertDialog.Builder builder;
    private NetBankingListAdapter netBankingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.testpress_activity_net_banking);
        recyclerView = (RecyclerView) findViewById(R.id.net_banking_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        EditText editTextSearch = (EditText) findViewById(R.id.action_search);
        editTextSearch.addTextChangedListener(this);
        builder = new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel)
                .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_BACK_PRESSED);
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.testpress_no, null);

        intent = getIntent();
        paymentParams = intent.getParcelableExtra(PayuConstants.PAYMENT_PARAMS);
        payuConfig = intent.getParcelableExtra(PayuConstants.PAYU_CONFIG);
        init();
    }

    private synchronized void init() {
        ArrayList<PaymentDetails> netBankingDetails =
                intent.getParcelableArrayListExtra(PayuConstants.NETBANKING);

        netBankingListAdapter = new NetBankingListAdapter(this, netBankingDetails);

        netBankingListAdapter.setOnRecyclerItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(netBankingListAdapter);

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecyclerItemClicked(View view, int position) {
        PaymentDetails details = netBankingListAdapter.getItem(position);
        paymentParams.setBankCode(details.getBankCode());

        try {
            PostData postData =
                    new PaymentPostParams(paymentParams, PayuConstants.NB).getPaymentPostParams();

            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, MakePaymentActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            } else {
                // Something went wrong
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        netBankingListAdapter.getFilter().filter(charSequence);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}

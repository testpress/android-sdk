package in.testpress.store.payu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import java.util.ArrayList;

import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

public class PaymentModeActivity extends BaseToolBarActivity implements
        PaymentOptionsAdapter.OnRecyclerItemClickListener, PaymentRelatedDetailsListener {

    public static final String ACTION_BACK_PRESSED = "backPressed";

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<PaymentOptionModel> paymentOptionModels = new ArrayList<>();
    private PaymentParams paymentParams;
    private PayuConfig payuConfig;
    private PayuHashes payuHashes;
    private PayuResponse mPayuResponse = null;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_payment_mode);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.payment_options);
        builder = new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_are_you_sure)
                .setMessage(R.string.testpress_want_to_cancel_order)
                .setPositiveButton(R.string.testpress_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton(R.string.testpress_no, null);

        Intent intent = getIntent();
        paymentParams = intent.getParcelableExtra(PayuConstants.PAYMENT_PARAMS);
        payuConfig = intent.getParcelableExtra(PayuConstants.PAYU_CONFIG);
        payuHashes = intent.getParcelableExtra(PayuConstants.PAYU_HASHES);

        TextView amountPayable = (TextView) findViewById(R.id.amount_payable);
        amountPayable.append(paymentParams.getAmount());

        init();
    }

    private synchronized void init() {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(paymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1(paymentParams.getUserCredentials() == null ? "default" :
                paymentParams.getUserCredentials());

        merchantWebService.setHash(payuHashes.getPaymentRelatedDetailsForMobileSdkHash());
        PostData postData = new MerchantWebServicePostParams(merchantWebService)
                .getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask =
                    new GetPaymentRelatedDetailsTask(this);

            paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void createPaymentOptions() {

        PaymentOptionsAdapter paymentOptionsAdapter =
                new PaymentOptionsAdapter(this, paymentOptionModels);

        paymentOptionsAdapter.setOnRecyclerItemClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(paymentOptionsAdapter);

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecyclerItemClicked(View view, int position) {
        PaymentOptionModel paymentOption = paymentOptionModels.get(position);
        switch (paymentOption.getName()) {
            case Pay_U_Constants.DEBIT_CARD:
                Intent dcIntent = new Intent(this, CreditCardActivity.class);
                dcIntent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
                dcIntent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                dcIntent.putExtra(PayuConstants.CARD_TYPE, PayuConstants.DEBITCARD);
                dcIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(dcIntent);
                finish();
                break;
            case Pay_U_Constants.CREDIT_CARD:
                Intent ccIntent = new Intent(this, CreditCardActivity.class);
                ccIntent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
                ccIntent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                ccIntent.putExtra(PayuConstants.CARD_TYPE, PayuConstants.CREDITCARD);
                ccIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(ccIntent);
                finish();
                break;
            case Pay_U_Constants.NET_BANKING:
                Intent nbIntent = new Intent(this, NetBankingActivity.class);
                nbIntent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
                nbIntent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                nbIntent.putExtra(PayuConstants.NETBANKING, mPayuResponse.getNetBanks());
                nbIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(nbIntent);
                finish();
                break;
        }
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;
        if (payuResponse.isResponseAvailable() &&
                payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {

            paymentOptionModels.add(
                    new PaymentOptionModel(R.drawable.ic_debit_card, Pay_U_Constants.DEBIT_CARD));

            paymentOptionModels.add(
                    new PaymentOptionModel(R.drawable.ic_credit_card, Pay_U_Constants.CREDIT_CARD));

            paymentOptionModels.add(
                    new PaymentOptionModel(R.drawable.ic_net_banking, Pay_U_Constants.NET_BANKING));

            createPaymentOptions();
        } else {
            Toast.makeText(this, "SOME THING WENT WRONG : " + payuResponse.getResponseStatus(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data.getAction() == null || !data.getAction().equals(ACTION_BACK_PRESSED)) {
                setResult(resultCode, data);
                finish();
            }
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
}

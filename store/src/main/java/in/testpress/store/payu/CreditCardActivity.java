package in.testpress.store.payu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.Calendar;
import java.util.Locale;

import in.testpress.store.R;
import in.testpress.ui.BaseToolBarActivity;

import static in.testpress.store.payu.PaymentModeActivity.ACTION_BACK_PRESSED;

public class CreditCardActivity extends BaseToolBarActivity
        implements View.OnClickListener, TextWatcher {

    private EditText cardNumber, month, year, cvv, name;
    private Button makePayment;
    private PaymentParams paymentParams;
    private PayuConfig payuConfig;
    private PayuUtils payuUtils;
    private String issuer;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.testpress_activity_credit_card);
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

        Intent intent = getIntent();

        paymentParams = intent.getParcelableExtra(PayuConstants.PAYMENT_PARAMS);
        payuConfig = intent.getParcelableExtra(PayuConstants.PAYU_CONFIG);
        payuUtils = new PayuUtils();

        cardNumber = (EditText) findViewById(R.id.card_number);
        cardNumber.addTextChangedListener(this);
        month = (EditText) findViewById(R.id.month);
        year = (EditText) findViewById(R.id.year);
        cvv = (EditText) findViewById(R.id.cvv);
        name = (EditText) findViewById(R.id.name);
        makePayment = (Button) findViewById(R.id.make_payment_button);
        assert makePayment != null;
        makePayment.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String _cardNumber = cardNumber.getText().toString().trim();
        String _month = month.getText().toString().trim();
        String _year = year.getText().toString().trim();
        String _cvv = cvv.getText().toString().trim();
        String _name = name.getText().toString().trim();

        int monthNum;
        if (_cardNumber.length() == 0) {
            cardNumber.setError(getString(R.string.testpress_this_field_is_required));
            return;
        }

        if (_month.length() == 0) {
            month.setError(getString(R.string.testpress_this_field_is_required));
            return;
        }
        monthNum = Integer.parseInt(_month);
        if (monthNum > 12) {
            month.setError(getString(R.string.testpress_invalid_month));
            return;
        }

        if (_year.length() == 0) {
            year.setError(getString(R.string.testpress_this_field_is_required));
            return;
        } else {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int currentYear = calendar.get(Calendar.YEAR);
            int __year = Integer.parseInt(_year);
            if (__year < currentYear) {
                year.setError(getString(R.string.testpress_invalid_year));
                return;
            }
        }

        if (_cvv.length() == 0 ) {
            cvv.setError(getString(R.string.testpress_this_field_is_required));
            return;
        }
        if (_cvv.length() < 3) {
            cvv.setError(getString(R.string.testpress_invalid_cvv));
            return;
        }

        if (_name.length() == 0) {
            name.setError(getString(R.string.testpress_this_field_is_required));
            return;
        }

        paymentParams.setCardName(issuer);
        paymentParams.setCardNumber(_cardNumber);
        paymentParams.setExpiryMonth(String.format(Locale.getDefault(), "%02d", monthNum));
        paymentParams.setExpiryYear(_year);
        paymentParams.setCvv(_cvv);
        paymentParams.setNameOnCard(_name);

        try {
            Payu.setInstance(getApplicationContext());
            PostData postData =
                    new PaymentPostParams(paymentParams, PayuConstants.CC).getPaymentPostParams();

            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, MakePaymentActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            } else {
                // something went wrong
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        if (charSequence.length() == 6) { // To confirm card we need min 6 digit.
            issuer = payuUtils.getIssuer(charSequence.toString());
            if (issuer != null && issuer.length() > 1) {
                Drawable issuerDrawable;
                switch (issuer) {
                    case PayuConstants.VISA:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_visa);
                        break;
                    case PayuConstants.LASER:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_laser);
                        break;
                    case PayuConstants.DISCOVER:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_discover);
                        break;
                    case PayuConstants.MAES:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_maestro);
                        break;
                    case PayuConstants.MAST:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_mastercard);
                        break;
                    case PayuConstants.AMEX:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_amex);
                        break;
                    case PayuConstants.DINR:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_diners);
                        break;
                    case PayuConstants.JCB:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_jcb);
                        break;
                    case PayuConstants.SMAE:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_maestro);
                        break;
                    default:
                        issuerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_brand_credit);
                }
                if (issuer.contentEquals(PayuConstants.SMAE)) { // Hide cvv and expiry
                    month.setVisibility(View.GONE);
                    year.setVisibility(View.GONE);
                    cvv.setVisibility(View.GONE);
                } else { // Show cvv and expiry
                    month.setVisibility(View.VISIBLE);
                    year.setVisibility(View.VISIBLE);
                    cvv.setVisibility(View.VISIBLE);
                }
                cardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
            }
        }

        if (charSequence.length() > 11 && !makePayment.isEnabled()) {
            makePayment.setEnabled(true);
        } else if (charSequence.length() < 12 && makePayment.isEnabled()) {
            makePayment.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
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
}

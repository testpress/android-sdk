package `in`.testpress.store.ui

import `in`.testpress.store.R
import `in`.testpress.store.TestpressStore
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class PaymentFailureActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_failure_activity_layout)
        displayFailureMessage()
        setOnClickListeners()
    }

    private fun displayFailureMessage() {
        val failureMessage = findViewById<TextView>(R.id.failure_message)
        failureMessage.text = "Your payment could not be processed. Please try again."
    }

    private fun setOnClickListeners() {
        val continueButton = findViewById<View>(R.id.back_button) as Button
        continueButton.setOnClickListener {
            backToProductDetail()
        }
    }

    private fun backToProductDetail() {
        val intent = Intent()
        intent.putExtra(TestpressStore.PAYMENT_FAILURE, true)
        setResult(RESULT_OK, intent)
        finish()
    }
}
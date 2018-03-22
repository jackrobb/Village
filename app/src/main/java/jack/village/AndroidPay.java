package jack.village;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardInfo;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stripe.android.model.Token;

import java.util.Arrays;

public class AndroidPay extends AppCompatActivity {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;

    private View payButton;
    private com.google.android.gms.wallet.PaymentsClient PaymentsClient;
    private NumberPicker numberPicker;
    private int amount;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private TextView donateContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_pay);

        //Set values for the number picker
        numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        donateContent = findViewById(R.id.donateContent);

        //Allow admin to remotely set text
        firebaseRemoteConfig.fetch()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            firebaseRemoteConfig.activateFetched();
                            Log.d("ConfigDonate", "Successful");
                        }else{
                            Log.d("ConfigDonate", "Unsuccessful");
                        }
                        fetchContent();
                    }
                });

        //Create new payments client and set it to test environment
        PaymentsClient =
                Wallet.getPaymentsClient(this,
                        new Wallet.WalletOptions.Builder()
                                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                                .build());

        //Set on click listener for payment button
        payButton = findViewById(R.id.buy_button);
        payButton.setEnabled(false);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payWithGooglePay();
            }
        });

        //Set the device up for payment
        isReadyToPay();

    }
    public void fetchContent(){
        //Update Text Fields
        donateContent.setText(firebaseRemoteConfig.getString("Donation"));
    }

    private void payWithGooglePay() {
        PaymentDataRequest request = createPaymentDataRequest();
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    PaymentsClient.loadPaymentData(request),
                    this,
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    private void isReadyToPay() {
        //Select allowed payment methods from users
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        Task<Boolean> task = PaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(@NonNull Task<Boolean> task) {
                        try {
                            boolean result =
                                    task.getResult(ApiException.class);
                            if(result) {
                                //If the device is ready to accept payment set pay button to enabled
                                payButton.setEnabled(true);
                            } else {
                                //Else disable the button
                                payButton.setEnabled(false);
                            }
                        } catch (ApiException exception) {
                            Toast.makeText(getApplicationContext(),
                                    "Exception: " + exception.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        //If the result returns ok get the token from Stripe
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        String rawToken = paymentData.getPaymentMethodToken().getToken();

                        Token stripeToken = Token.fromString(rawToken);

                        if (stripeToken != null) {

                            //Send token to Server.
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        //Toast Cancelled
                        Toast.makeText(this,
                                "Canceled", Toast.LENGTH_LONG).show();

                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        //Toast Error
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        Toast.makeText(this,
                                "Got error " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                        break;
                    default:
                }
                break;
            default:
        }
    }


    //Get payment parameters - Set up account unique test key
    private PaymentMethodTokenizationParameters createTokenizationParameters() {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:publishableKey", "pk_test_RRggAlQdtWBJBMYlhvjAQnFZ")
                .addParameter("stripe:version", "5.1.1")
                .build();
    }

    //Build the request selecting allowed inputs and price
    private PaymentDataRequest createPaymentDataRequest() {
        amount = numberPicker.getValue();
        PaymentDataRequest.Builder request =
                PaymentDataRequest.newBuilder()
                        .setTransactionInfo(
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                        //Price set to amount from number picker
                                        .setTotalPrice(String.valueOf(amount) + ".00")
                                        .setCurrencyCode("GBP")
                                        .build())
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(Arrays.asList(
                                                WalletConstants.CARD_NETWORK_VISA,
                                                WalletConstants.CARD_NETWORK_MASTERCARD))
                                        .build());



        Toast.makeText(this,
                "Thank-you for donating " + String.valueOf(amount)+".00", Toast.LENGTH_LONG).show();

        request.setPaymentMethodTokenizationParameters(createTokenizationParameters());
        return request.build();
    }


}

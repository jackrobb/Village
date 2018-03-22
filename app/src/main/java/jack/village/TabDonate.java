package jack.village;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stripe.android.model.Token;

import org.w3c.dom.Text;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabDonate extends Fragment {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;

    private View payButton;
    private PaymentsClient PaymentsClient;
    private NumberPicker numberPicker;
    private int amount;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private TextView donateContent;

    public TabDonate() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_donate, container, false);

        numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        donateContent = view.findViewById(R.id.donateContent);

        firebaseRemoteConfig.fetch()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
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

        PaymentsClient =
                Wallet.getPaymentsClient(getContext(),
                        new Wallet.WalletOptions.Builder()
                                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                                .build());

        payButton = view.findViewById(R.id.buy_button);
        payButton.setEnabled(false);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payWithGooglePay();
            }
        });

        isReadyToPay();

        return view;
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
                    getActivity(),
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    private void isReadyToPay() {
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
                                payButton.setEnabled(true);
                            } else {
                                payButton.setEnabled(false);
                            }
                        } catch (ApiException exception) {
                            Toast.makeText(getActivity(),
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

                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        CardInfo info = paymentData.getCardInfo();
                        UserAddress address = paymentData.getShippingAddress();
                        String rawToken = paymentData.getPaymentMethodToken().getToken();

                        Token stripeToken = Token.fromString(rawToken);

                        if (stripeToken != null) {
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getContext(),
                                "Canceled", Toast.LENGTH_LONG).show();

                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        Toast.makeText(getContext(),
                                "Got error " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                        break;
                    default:
                }
                break;
            default:
        }
    }

    private PaymentMethodTokenizationParameters createTokenizationParameters() {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:publishableKey", "pk_test_RRggAlQdtWBJBMYlhvjAQnFZ")
                .addParameter("stripe:version", "5.1.1")
                .build();
    }

    private PaymentDataRequest createPaymentDataRequest() {
        amount = numberPicker.getValue();
        PaymentDataRequest.Builder request =
                PaymentDataRequest.newBuilder()
                        .setTransactionInfo(
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
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



        Toast.makeText(getActivity(),
                "Thank-you for donating " + String.valueOf(amount)+".00", Toast.LENGTH_LONG).show();

        request.setPaymentMethodTokenizationParameters(createTokenizationParameters());
        return request.build();
    }


}

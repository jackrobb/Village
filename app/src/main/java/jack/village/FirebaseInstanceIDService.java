package jack.village;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by jack on 02/02/2018.
 */

public class FirebaseInstanceIDService extends com.google.firebase.iid.FirebaseInstanceIdService {

        private static final String TAG = "MyFirebaseIIDService";

        @Override
        public void onTokenRefresh() {

            //Getting registration token
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            //Displaying token in logcat
            Log.e(TAG, "Refreshed token: " + refreshedToken);

        }

        private void sendRegistrationToServer(String token) {

        }
}

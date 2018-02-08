package jack.village;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by jack on 08/02/2018.
 */

public class Village extends Application{

    @Override
    public void onCreate(){
        super.onCreate();

        //Allows notes to be accessed offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

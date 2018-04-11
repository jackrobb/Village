package jack.village;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabHome extends Fragment {

    TextView title, description;
    FirebaseRemoteConfig firebaseRemoteConfig;

    public TabHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_home, container, false);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        title = view.findViewById(R.id.home_title);
        description = view.findViewById(R.id.home_description);

        //Fetch the data from remote configuration
        firebaseRemoteConfig.fetch()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            firebaseRemoteConfig.activateFetched();
                            Log.d("Config", "Successful");
                        }else{
                            Log.d("Config", "Unsuccessful");
                        }
                        fetchContent();
                    }
                });

        return view;
    }

    public void fetchContent(){
        //Update Text Fields
        title.setText(firebaseRemoteConfig.getString("Title"));
        description.setText(firebaseRemoteConfig.getString("Description"));
    }

}

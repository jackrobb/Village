package jack.village;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabContact extends Fragment implements View.OnClickListener{

    private Button connectionCard;


    public TabContact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_contact, container, false);

        connectionCard = view.findViewById(R.id.connectionCard);
        connectionCard.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectionCard:
                startActivity(new Intent(getActivity(), ConnectionCardActivity.class));
                break;
        }
    }

}

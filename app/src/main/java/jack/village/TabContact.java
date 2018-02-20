package jack.village;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.Manifest;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabContact extends Fragment implements View.OnClickListener{

    MapView mMapView;
    private GoogleMap googleMap;


    public TabContact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_contact, container, false);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                //Customise map appearance
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));


                // For dropping a marker at a point on the Map
                LatLng village = new LatLng(54.597144, -5.885955);
                googleMap.addMarker(new MarkerOptions().position(village).title("Village").snippet("Village Church Belfast: East"));


                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(village).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        Button connectionCard = view.findViewById(R.id.connectionCard);
        connectionCard.setOnClickListener(this);

        Button directions = view.findViewById(R.id.directions);
        directions.setOnClickListener(this);

        Button phone = view.findViewById(R.id.phone);
        phone.setOnClickListener(this);

        ImageButton facebook = view.findViewById(R.id.facebook);
        facebook.setOnClickListener(this);

        ImageButton twitter = view.findViewById(R.id.twitter);
        twitter.setOnClickListener(this);

        ImageButton instagram = view.findViewById(R.id.instagram);
        instagram.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    //Ensure permission has been granted, if so call number
    public void onCall() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    Integer.parseInt("1"));
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:07491010066")));
        }
    }

    //Check to see if the user has given application permission to call
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onCall();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;

            default:
                break;
        }
    }

    public static String FACEBOOK_URL = "https://www.facebook.com/villagechurchbelfast";

    //Link to facebook application
    public String facebook(Context context) {
        try {
            getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    public static String TWITTER_URL = "https://www.twitter.com/Village_Belfast";
    public static String TWITTER_PAGE_ID = "442681264";

    //Link to Twitter application
    public String twitter() {
        try {
            getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
            return "twitter://user?user_id=" + TWITTER_PAGE_ID;
        } catch (PackageManager.NameNotFoundException e) {
            return TWITTER_URL; //normal web url
        }
    }

    public static String INSTAGRAM_URL = "https://www.instagram.com/villagechurchbelfast";
    public static String INSTAGRAM_PAGE_ID = "villagechurchbelfast";

    //Link to Instagram application
    public String instagram() {
        try {
            getActivity().getPackageManager().getPackageInfo("com.instagram.android", 0);
            return "http://instagram.com/_u/" + INSTAGRAM_PAGE_ID;
        } catch (PackageManager.NameNotFoundException e) {
            return INSTAGRAM_URL; //normal web url
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //Ensure the button was not pressed by accident
            case R.id.phone:
                new AlertDialog.Builder(getActivity())
                        .setMessage("Call Village Church?")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TabContact.this.onCall();
                            }
                        })
                        .show();
                break;
            case R.id.facebook:
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = facebook(getActivity());
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
                break;
            case R.id.twitter:
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
                String twitterUrl = twitter();
                twitterIntent.setData(Uri.parse(twitterUrl));
                startActivity(twitterIntent);
                break;
            case R.id.instagram:
                Intent instagramIntent = new Intent(Intent.ACTION_VIEW);
                String instagramUrl = instagram();
                instagramIntent.setData(Uri.parse(instagramUrl));
                startActivity(instagramIntent);
                break;
            case R.id.directions:
                Intent direction = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=54.597144, -5.885955&travelmode=driving"));
                startActivity(direction);
                break;
            case R.id.connectionCard:
                startActivity(new Intent(getActivity(), ConnectionCardActivity.class));
                break;
        }
    }

}

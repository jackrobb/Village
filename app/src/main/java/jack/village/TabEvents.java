package jack.village;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TabEvents extends Fragment {

    WebView webView;
    ImageButton refresh;
    TextView reload;
    String url = "https://www.villagebelfast.com/events/";

    public TabEvents() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_events, container, false);

        webView = view.findViewById(R.id.webView);
        refresh = view.findViewById(R.id.refresh);
        reload = view.findViewById(R.id.reload);

        setRetainInstance(true);

        if (internet_connection()) {
            webView.setVisibility(View.VISIBLE);
            load();
        }else{
            webView.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), "Internet Connection Required", Toast.LENGTH_SHORT).show();
        }

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internet_connection()) {
                    webView.setVisibility(View.VISIBLE);
                    load();
                }else{
                    webView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Internet Connection Required", Toast.LENGTH_SHORT).show();
                }
            }
        });


        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //This is the filter
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        (getActivity()).onBackPressed();
                    }
                    return true;
                }
                return false;
            }
        });


        return view;

}

    public void load(){
        refresh.setVisibility(View.INVISIBLE);
        reload.setVisibility(View.INVISIBLE);
        WiseWeWebClient myWebClient = new WiseWeWebClient();
        webView.setWebViewClient(myWebClient);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }



    public class WiseWeWebClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view, String url) {

            view.loadUrl("javascript:" +
                    "var footer = document.getElementById(\"footer\"); footer.parentNode.removeChild(footer); " +
                    "var header = document.getElementById(\"header\"); header.parentNode.removeChild(header); " +
                    "var navbar = document.getElementsByClassName('mobile-nav clear')[0].style.display = 'none';" +
                    "var navbar2 = document.getElementsByClassName('cco-nav')[0].style.display = 'none';");
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().contains("village")) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }

    //Method to check if the device has an internet connection
    boolean internet_connection(){
        ConnectivityManager connectionManager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

}
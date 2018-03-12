package jack.village;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class PodcastTab extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;

    private List<PodcastRSSModel> feedList;
    List<PodcastRSSModel> items = new ArrayList<>();


    public PodcastTab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_podcast, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //If there is an internet connection Call fetchFeed on creation
        if (internet_connection()) {
            new FetchFeed().execute((Void) null);
        }else
        {
            Toast.makeText(getContext(), "Internet Connection Required", Toast.LENGTH_SHORT).show();
        }

        //If there is an internet connection call fetchFeed when user refreshes page
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (internet_connection()) {
                new FetchFeed().execute((Void) null);
                }else
                {
                    Toast.makeText(getContext(), "Internet Connection Required", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                }
            }
        });



        return view;
    }

//    AsyncTask allows the application to perform background tasks and publish results
    private class FetchFeed extends AsyncTask<Void, Void, Boolean> {

        //String to contain RSS url
        private String urlLink;

        @Override
        protected void onPreExecute() {
            //On pre execute display the refreshing wheel
            swipeLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                //Set string to RSS url
                urlLink = "https://www.villagebelfast.com/new-blog?format=rss";

                //URL item equal to the RSS string
                URL url = new URL(urlLink);

                //Used to read data from a source
                InputStream inputStream = url.openConnection().getInputStream();

                //feed list array equal to the results of the method parse feed
                feedList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //Set the refreshing icon to false as page has loaded
            swipeLayout.setRefreshing(false);
                // Fill RecyclerView
                recyclerView.setAdapter(new PodcastRssFeedAdapter(feedList));
        }
    }

    public List<PodcastRSSModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean inItem = false;
        int amount = 0;


            try {
                //Create instance of XmlPullParser and set the input to the input stream
                XmlPullParser pullParser = Xml.newPullParser();
                pullParser.setInput(inputStream, null);

                    //Ensure the document has not ended and no more than 12 objects have been pulled (Performance)
                    while (pullParser.next() != XmlPullParser.END_DOCUMENT  && amount <12) {
                        int eventType = pullParser.getEventType();

                        //Returns the name of the current element
                        //If it is null skip
                        String name = pullParser.getName();
                        if (name == null)
                            continue;

                        //If it is the End Tag set in item to false
                        if (eventType == XmlPullParser.END_TAG) {
                            if (name.equalsIgnoreCase("item")) {
                                inItem = false;
                            }
                            continue;
                        }

                        //If it is the Start Tag set in item to true
                        if (eventType == XmlPullParser.START_TAG) {
                            if (name.equalsIgnoreCase("item")) {
                                inItem = true;
                                continue;
                            }
                        }

                        //Set empty string result
                        String result = "";

                        //If the next item is text set result to text and move to next tag
                        if (pullParser.next() == XmlPullParser.TEXT) {
                            result = pullParser.getText();
                            pullParser.nextTag();
                        }

                        //Set the title, link and description to the string pulled from the rss feed
                            if (name.equalsIgnoreCase("title")) {
                                title = result;
                            } else if (name.equalsIgnoreCase("enclosure")) {
                                String podcastUrl = pullParser.getAttributeValue(null, "url");
                                link = podcastUrl;
                            } else if (name.equalsIgnoreCase("description")) {
                                description = Html.fromHtml(result).toString();
                            }

                            //If all items have a value, create a new item model and add the items
                            if (title != null && link != null && description != null) {
                                if (inItem) {
                                    PodcastRSSModel item = new PodcastRSSModel(title, link, description);
                                    items.add(item);

                                    //Amount +1
                                    amount++;
                                }

                                //Reset strings to null
                                title = null;
                                link = null;
                                description = null;
                                inItem = false;
                            }
                        }

                return items;
            }
        finally {
            inputStream.close();
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

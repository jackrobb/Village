package jack.village;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabPodcast extends Fragment {

    ListView rss_feed;
    ArrayList<String> title;
    ArrayList<String> link;
    ArrayList<String> description;


    public TabPodcast() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_podcast, container, false);

        rss_feed = view.findViewById(R.id.rss_feed);

        title = new ArrayList<>();
        link = new ArrayList<>();
        description = new ArrayList<>();


        rss_feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final Uri podcastUri = Uri.parse(link.get(position));

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaPlayer podcast = new MediaPlayer();
                        podcast.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        String podcast1 = podcastUri.toString();

                        try {
                            podcast.setDataSource(podcast1);
                            podcast.prepare();
                            podcast.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                     }
                });
                mainThread.start();

            }
        });

        new ProcessInBackground().execute();

        return view;
    }

    public InputStream getInputStream(URL url){
        try{
            return url.openConnection().getInputStream();
        }catch(IOException e){
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception>
    {
        ProgressBar progressBar = new ProgressBar(getActivity());
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try{
                //Defines RSS URL
                URL url = new URL("https://www.villagebelfast.com/new-blog?format=rss");

                //Helps to retrieve data from xml document
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                //Doesn't support XML namespaces
                factory.setNamespaceAware(true);

                //New instance of xml pull parser using currently configured factory features
                XmlPullParser xml = factory.newPullParser();

                //Set pull parser to the url
                xml.setInput(getInputStream(url), "UTF_8");

                boolean insideItem=false;

                int type = xml.getEventType();

                while(type != XmlPullParser.END_DOCUMENT){
                    if(type == XmlPullParser.START_TAG){
                        if(xml.getName().equalsIgnoreCase("item")){
                            insideItem = true;
                        }else if (xml.getName().equalsIgnoreCase("title")){
                            if(insideItem){
                                title.add(xml.nextText());
                            }
                        }else if (xml.getName().equalsIgnoreCase("description")){
                            if(insideItem){
                                description.add(xml.nextText());
                            }
                        }else if(xml.getName().equalsIgnoreCase("enclosure")){
                            if(insideItem){
                                String podcastUrl = xml.getAttributeValue(null, "url");
                                link.add(podcastUrl);
                            }
                        }
                    }else if(type == XmlPullParser.END_TAG && xml.getName().equalsIgnoreCase("title")){
                        insideItem = false;
                    }

                    type = xml.next();
                }

            }catch(XmlPullParserException | IOException e){
                exception = e;
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            ArrayAdapter<String> titleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, title);

            rss_feed.setAdapter(titleAdapter);

            progressBar.setVisibility(View.GONE);
        }
    }


}

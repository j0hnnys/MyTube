package com.projects.johnny.se137.mytube;

import android.app.Fragment;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johnny on 12/8/15.
 */
public class SearchFragment extends Fragment {

    private EditText searchBarEditText;
    private RecyclerView mSearchResultRecyclerView;
    private YouTubeSearchResultAdapter mYouTubeSearchResultAdapter;
    private List<SearchResult> mSearchResultList;

    // API Key obtained from Google
    private final String API_KEY = Config.YOUTUBE_API_KEY;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    private static final long NUMBER_OF_VIDEOS_RETURNED = 30;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        searchBarEditText = (EditText) v.findViewById(R.id.search_bar_edit_text);
        mSearchResultList = new ArrayList<SearchResult>();

        try {
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {

                }
            }).setApplicationName("MyTube").build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set listener for search bar EditText view
        // will search videos and obtain a list of SearchResults
        searchBarEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new SearchInBackground().doInBackground();
                new SearchNow().run();

                updateRecyclerView();
            }
        });

        mSearchResultRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_search);
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateRecyclerView();

        return v;
    }

    private void updateRecyclerView() {
        mYouTubeSearchResultAdapter = new YouTubeSearchResultAdapter(mSearchResultList);
        mSearchResultRecyclerView.setAdapter(mYouTubeSearchResultAdapter);
    }

    private class SearchNow implements Runnable {

        @Override
        public void run() {
            try {
                YouTube.Search.List searchRequest = youtube.search().list("id,snippet");
                // Set search query (input in search bar EditText)
                searchRequest.setQ(searchBarEditText.getText().toString());
                // Filter search to videos only
                searchRequest.setType("video");
                // Set max results to 10
                searchRequest.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                // Set API key
                searchRequest.setKey(API_KEY);

                // Get response for search
                SearchListResponse searchResponse = searchRequest.execute();

                // Get list of results
                mSearchResultList = searchResponse.getItems();

            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class YouTubeSearchResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private YouTubeThumbnailView thumbnailView;
        private TextView titleTextView;
        private TextView numberOfViewsTextView;
        private TextView publishedDateTextView;
        private Button favoriteButton;
        private Video v;

        private String videoIDSelected;

        public YouTubeSearchResultHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            numberOfViewsTextView = (TextView) itemView.findViewById(R.id.num_views_text_view);
            publishedDateTextView = (TextView) itemView.findViewById(R.id.publish_date_text_view);
            thumbnailView = (YouTubeThumbnailView) itemView.findViewById(R.id.thumbnail);
            favoriteButton = (Button) itemView.findViewById(R.id.favorite_button);
        }

        private void bindResult(SearchResult result) {
            // Get video id for listener action
            videoIDSelected = result.getId().getVideoId();

            // Get title and published date
            SearchResultSnippet snippet = result.getSnippet();
            String title = snippet.getTitle();
            String publishedDate = "Uploaded: " + snippet.getPublishedAt().toString();

            // Get view count
            String numberOfViews = "View count: ";
            try {
                YouTube.Videos.List list = youtube.videos().list("statistics");
                // Sets ID of video in each search result
                list.setId(result.getId().getVideoId());
                list.setKey(API_KEY);

                // Create video object to get view count
                v = list.execute().getItems().get(0);
                // Get view count
                numberOfViews += v.getStatistics().getViewCount();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get thumbnail images
            final String videoID = result.getId().getVideoId();
            thumbnailView.initialize(API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    youTubeThumbnailLoader.setVideo(videoID);
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });

            titleTextView.setText(title);
            numberOfViewsTextView.setText(numberOfViews);
            publishedDateTextView.setText(publishedDate);

            // Set button listener to add video to favorites
            final SearchResult searchResult = result;
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FavoritesFragment.favoritesList.add(searchResult);
                }
            });
        }

        @Override
        public void onClick(View v) {
            // Save video id to the intent that will be passed to VideoActivity
            Intent intent = new Intent(getActivity(), VideoActivity.class);
            intent.putExtra(VideoActivity.VIDEO_ID_KEY, videoIDSelected);
            startActivity(intent);
        }
    }

    private class YouTubeSearchResultAdapter extends RecyclerView.Adapter<YouTubeSearchResultHolder> {
        List<SearchResult> searchResults;

        public YouTubeSearchResultAdapter(List<SearchResult> results) {
            searchResults = results;
        }

        @Override
        public YouTubeSearchResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create the LayoutInflater that is used to inflate the CrimeHolder (ViewHolder)
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            // Inflates each individual ViewHolder using a our
            // layout defined in R.layout.frag_list_item
            View view = layoutInflater.inflate(R.layout.fragment_list_item, parent, false);

            return new YouTubeSearchResultHolder(view);
        }

        @Override
        public void onBindViewHolder(YouTubeSearchResultHolder holder, int position) {
            holder.bindResult(mSearchResultList.get(position));
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }
}

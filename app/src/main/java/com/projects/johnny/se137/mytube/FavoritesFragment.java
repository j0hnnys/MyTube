package com.projects.johnny.se137.mytube;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    // Static class used as singleton
    public static List<SearchResult> favoritesList;

    private RecyclerView mSearchResultRecyclerView;
    private YouTubeSearchResultAdapter mYouTubeSearchResultAdapter;
    private static final String API_KEY = Config.YOUTUBE_API_KEY;
    private YouTube youtube;

    public FavoritesFragment() {
        super();
        if (favoritesList == null) {
            favoritesList = new ArrayList<SearchResult>();
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize YouTube object
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {

            }
        }).setApplicationName("MyTube").build();

        mSearchResultRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_favorites);
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchResultRecyclerView.setAdapter(new YouTubeSearchResultAdapter(favoritesList));

        return v;
    }

    // made public so that it is called every time favorite tab is clicked
    // in order to refresh favorites page
    public void updateRecyclerView() {
        mYouTubeSearchResultAdapter = new YouTubeSearchResultAdapter(favoritesList);
        mSearchResultRecyclerView.setAdapter(mYouTubeSearchResultAdapter);
    }

    private class YouTubeSearchResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private YouTubeThumbnailView thumbnailView;
        private TextView titleTextView;
        private TextView numberOfViewsTextView;
        private TextView publishedDateTextView;
        private Video v;

        private String videoIDSelected;

        public YouTubeSearchResultHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // Initialize view objects
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            numberOfViewsTextView = (TextView) itemView.findViewById(R.id.num_views_text_view);
            publishedDateTextView = (TextView) itemView.findViewById(R.id.publish_date_text_view);
            thumbnailView = (YouTubeThumbnailView) itemView.findViewById(R.id.thumbnail);
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
            View view = layoutInflater.inflate(R.layout.fragment_list_item_favorites, parent, false);

            return new YouTubeSearchResultHolder(view);
        }

        @Override
        public void onBindViewHolder(YouTubeSearchResultHolder holder, int position) {
            holder.bindResult(searchResults.get(position));
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }
}

package com.projects.johnny.se137.mytube;

import android.os.Bundle;
import android.os.PersistableBundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;

/**
 * Created by Johnny on 12/9/15.
 */
public class VideoActivity extends YouTubeBaseActivity {

    private YouTubePlayerView mYouTubePlayerView;
    private YouTubePlayer mYouTubePlayer;
    private static final String API_KEY = Config.YOUTUBE_API_KEY;

    public static final String VIDEO_ID_KEY = "VIDEO_ID_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final String videoIDPassed = getIntent().getStringExtra(VIDEO_ID_KEY);

        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubeplayerview);

        // This process runs in background. So mYouTubePlayer may take some time to initialize.
        mYouTubePlayerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                mYouTubePlayer = youTubePlayer;
                mYouTubePlayer.loadVideo(videoIDPassed);
                mYouTubePlayer.play();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                System.out.println("\nYouTube Player Initialize FAILED\n");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

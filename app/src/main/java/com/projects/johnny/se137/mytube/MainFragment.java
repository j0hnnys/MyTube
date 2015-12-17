package com.projects.johnny.se137.mytube;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;



/**
 * Created by Johnny on 12/7/15.
 */
public class MainFragment extends Fragment {

    Fragment searchFragment;
    Fragment favoritesFragment;

    Button searchButton;
    Button favoritesButton;
    FrameLayout frameLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        searchFragment = new SearchFragment();
        favoritesFragment = new FavoritesFragment();

        searchButton = (Button) v.findViewById(R.id.tab_search_button);
        favoritesButton = (Button) v.findViewById(R.id.tab_favorites_button);
        frameLayout = (FrameLayout) v.findViewById(android.R.id.tabcontent);

        searchButton.setOnClickListener(searchButtonListener());
        favoritesButton.setOnClickListener(favoritesButtonListener());

        return v;
    }

    private View.OnClickListener searchButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButton.setBackgroundColor(getResources().getColor(R.color.colorHoloRedDark));
                favoritesButton.setBackgroundColor(getResources().getColor(R.color.colorHoloRedLight));


                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(android.R.id.tabcontent, searchFragment).commit();
            }
        };
    };

    private View.OnClickListener favoritesButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesButton.setBackgroundColor(getResources().getColor(R.color.colorHoloRedDark));
                searchButton.setBackgroundColor(getResources().getColor(R.color.colorHoloRedLight));

                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(android.R.id.tabcontent, favoritesFragment).commit();
            }
        };
    };
}

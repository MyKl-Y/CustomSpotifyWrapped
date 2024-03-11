package com.example.spotifywrapped;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StoryPagerAdapter extends FragmentStateAdapter {
    private SpotifyDataModel storyData = new SpotifyDataModel();

    public StoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setData(SpotifyDataModel data) {
        this.storyData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return SummaryStartFragment.newInstance(storyData);
            case 1:
                return TopArtistsFragment.newInstance(storyData);
            case 2:
                return RecommendationsFragment.newInstance(storyData);
            case 3:
                return TopSongsFragment.newInstance(storyData);
            case 4:
                return TopGenresFragment.newInstance(storyData);
            case 5:
                return LargeLanguageModelFragment.newInstance(storyData);
            case 6:
                return SummaryEndFragment.newInstance(storyData);
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}

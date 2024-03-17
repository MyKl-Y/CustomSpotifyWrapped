package com.example.spotifywrapped;

import android.util.Log;

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
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
                    return HolidayRecommendationsFragment.newInstance(storyData, 0);
                } else {
                    return TopArtistsFragment.newInstance(storyData);
                }
            case 2:
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
                    return HolidayRecommendationsFragment.newInstance(storyData, 1);
                } else {
                    return RecommendationsFragment.newInstance(storyData);
                }
            case 3:
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
                    return HolidayRecommendationsFragment.newInstance(storyData, 2);
                } else {
                    return TopSongsFragment.newInstance(storyData);
                }
            case 4:
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
                    return HolidayRecommendationsFragment.newInstance(storyData, 3);
                } else {
                    return TopGenresFragment.newInstance(storyData);
                }
            case 5:
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
                    return HolidayRecommendationsFragment.newInstance(storyData, 4);
                } else {
                    return LargeLanguageModelFragment.newInstance(storyData);
                }
            case 6:
                if (storyData.timeRange.equalsIgnoreCase("Holiday")) {

                } else {
                    return SummaryEndFragment.newInstance(storyData);
                }
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        if (storyData.timeRange.equalsIgnoreCase("Holiday")) {
            return 6;
        } else {
            return 7;
        }
    }
}

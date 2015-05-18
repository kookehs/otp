package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mrcornman.otp.R;
import com.mrcornman.otp.adapters.MakerMatchAdapter;
import com.mrcornman.otp.models.MatchItem;
import com.mrcornman.otp.utils.DatabaseHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MakerListFragment extends Fragment {

    private MakerMatchAdapter makerMatchAdapter;
    private List<MatchItem> matchItems;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MakerListFragment newInstance() {
        MakerListFragment fragment = new MakerListFragment();
        return fragment;
    }

    public MakerListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maker_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.matchmaker_list);

        // set up list view input
        // set up on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String matchId = view.getTag().toString();
                DatabaseHelper.getMatchById(matchId, new GetCallback<MatchItem>() {
                    @Override
                    public void done(MatchItem matchItem, ParseException e) {
                        if(matchItem != null) {
                            String numLikesStr = matchItem.getNumLikes() + "";
                            Log.i("MatchMakerListFragment", "Checking out - " + numLikesStr);
                        }
                    }
                });
            }
        });

        matchItems = new ArrayList<>();
        makerMatchAdapter = new MakerMatchAdapter(getActivity().getApplicationContext());
        listView.setAdapter(makerMatchAdapter);

        // fill list up
        DatabaseHelper.findTopMatches(20, new FindCallback<MatchItem>() {
            @Override
            public void done(List<MatchItem> list, ParseException e) {
                for(MatchItem match : list) {
                    makerMatchAdapter.addMatch(match);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
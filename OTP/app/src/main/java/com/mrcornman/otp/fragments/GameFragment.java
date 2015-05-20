package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrcornman.otp.R;
import com.mrcornman.otp.adapters.UserCardAdapter;
import com.mrcornman.otp.models.MatchItem;
import com.mrcornman.otp.utils.DatabaseHelper;
import com.mrcornman.otp.views.CardStackLayout;
import com.mrcornman.otp.views.CardView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class GameFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private CardStackLayout mCardStackLayoutFirst;
    private CardStackLayout mCardStackLayoutSecond;
    private UserCardAdapter mUserCardAdapterFirst;
    private UserCardAdapter mUserCardAdapterSecond;

    private SharedPreferences sharedPreferences;

    private String potentialFirstId = "";
    private String potentialSecondId = "";

    public static GameFragment newInstance() {
        GameFragment fragment = new GameFragment();
        return fragment;
    }

    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // init views
        mCardStackLayoutFirst = (CardStackLayout) view.findViewById(R.id.cardstack_first);
        mCardStackLayoutSecond = (CardStackLayout) view.findViewById(R.id.cardstack_second);

        mCardStackLayoutFirst.setCardStackListener(new CardStackLayout.CardStackListener() {
            @Override
            public void onBeginProgress() {
                buildPotentialMatch(getCurrentFirstId(), getCurrentSecondId());
            }

            @Override
            public void onUpdateProgress(boolean positif, float percent) {
            }

            @Override
            public void onCancelled() {
                clearPotentialMatch();
            }

            @Override
            public void onChoiceMade(boolean choice) {
                /*
                SingleUserView item = (SingleUserView) beingDragged;
                item.onChoiceMade(choice, beingDragged);
                //todo: handle what to do after the choice is made.
                if (choice) {
                    db.updateNumLikes(item.userItem, db.VALUE_LIKED);
                } else {
                    db.updateNumLikes(item.userItem, db.VALUE_DISLIKED);
                }
                Log.d("game fragment", "updated the choice made " + String.valueOf(choice) + " " + item.userItem.getName());
                */
                onCreateMatch();

                if(!mCardStackLayoutFirst.hasMoreItems()) {
                    refreshFirst();
                }
            }
        });

        mCardStackLayoutSecond.setCardStackListener(new CardStackLayout.CardStackListener() {
            @Override
            public void onBeginProgress() {
                buildPotentialMatch(getCurrentFirstId(), getCurrentSecondId());
            }

            @Override
            public void onUpdateProgress(boolean positif, float percent) {
            }

            @Override
            public void onCancelled() {
                clearPotentialMatch();
            }

            @Override
            public void onChoiceMade(boolean choice) {
                onCreateMatch();

                if(!mCardStackLayoutSecond.hasMoreItems()) {
                    refreshSecond();
                }
            }
        });

        // init data
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        mUserCardAdapterFirst = new UserCardAdapter(getActivity().getApplicationContext());
        mCardStackLayoutFirst.setAdapter(mUserCardAdapterFirst);

        mUserCardAdapterSecond = new UserCardAdapter(getActivity().getApplicationContext());
        mCardStackLayoutSecond.setAdapter(mUserCardAdapterSecond);

        refreshFirst();
        refreshSecond();

        return view;
    }


    private void buildPotentialMatch(String firstId, String secondId) {
        potentialFirstId = firstId != null ? firstId : "";
        potentialSecondId = secondId != null ? secondId : "";
    }

    private void clearPotentialMatch() {
        potentialFirstId = "";
        potentialSecondId = "";
    }

    public void onCreateMatch() {
        if(potentialFirstId.equals("") || potentialSecondId.equals("")) return;

        final String cachedFirstId = potentialFirstId;
        final String cachedSecondId = potentialSecondId;

        clearPotentialMatch();

        if(cachedFirstId == cachedSecondId) {
            Log.e("Game Fragment", "User tried to match up same people.");
            return;
        }

        // TODO: Possibly make it update on insert match instead of doing a costly check beforehand
        DatabaseHelper.getMatchByPair(cachedFirstId, cachedSecondId, new GetCallback<MatchItem>() {
            @Override
            public void done(MatchItem matchItem, ParseException e) {
                if (matchItem == null) {
                    DatabaseHelper.insertNewMatchByPair(cachedFirstId, cachedSecondId);
                } else {
                    DatabaseHelper.updateMatchNumLikes(matchItem.getObjectId(), matchItem.getNumLikes() + 1);
                }
            }
        });
    }

    private void refreshFirst() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // exclude self
        //query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    mUserCardAdapterFirst.fillUsers(list);

                    mCardStackLayoutFirst.refreshStack();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sorry, there was a problem loading users",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void refreshSecond() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // exclude self
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    mUserCardAdapterSecond.fillUsers(list);

                    mCardStackLayoutSecond.refreshStack();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sorry, there was a problem loading users",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public String getCurrentFirstId() {
        CardView view = mCardStackLayoutFirst.getDraggedCard() != null ? (CardView)mCardStackLayoutFirst.getDraggedCard() : (CardView)mCardStackLayoutFirst.getTopCard();
        return view != null ? view.boundUserId : null;
    }

    public String getCurrentSecondId() {
        CardView view = mCardStackLayoutSecond.getDraggedCard() != null ? (CardView)mCardStackLayoutSecond.getDraggedCard() : (CardView)mCardStackLayoutSecond.getTopCard();
        return view != null ? view.boundUserId : null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public interface GameInteractionListener {
        //void onRequestOpenClientMatch(String recipientId);
    }
}

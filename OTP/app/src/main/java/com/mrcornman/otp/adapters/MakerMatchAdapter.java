package com.mrcornman.otp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrcornman.otp.R;
import com.mrcornman.otp.models.models.MatchItem;
import com.mrcornman.otp.models.gson.PhotoFile;
import com.mrcornman.otp.models.models.PhotoItem;
import com.mrcornman.otp.utils.DatabaseHelper;
import com.mrcornman.otp.utils.PrettyNumbers;
import com.mrcornman.otp.utils.ProfileBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MakerMatchAdapter extends BaseAdapter {

    private Context mContext;
    private List<MatchItem> mItems;

    public MakerMatchAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    public void addMatch(MatchItem match) {
        mItems.add(match);
        notifyDataSetChanged();
    }

    public void clearMatches() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MatchItem getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_match_maker, parent, false);
        }

        // init views first
        final TextView countText = (TextView) convertView.findViewById(R.id.count_text);

        final ImageView thumbImageFirst = (ImageView) convertView.findViewById(R.id.thumb_image_first);
        final TextView nameTextFirst = (TextView) convertView.findViewById(R.id.name_text_first);

        // init views second
        final ImageView thumbImageSecond = (ImageView) convertView.findViewById(R.id.thumb_image_second);
        final TextView nameTextSecond = (TextView) convertView.findViewById(R.id.name_text_second);

        // use match item to fill views
        MatchItem match = getItem(position);

        convertView.setTag(match.getObjectId());
        countText.setText(PrettyNumbers.formatInteger(match.getNumLikes()));

        // fill first user
        DatabaseHelper.getUserById(match.getFirstId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(parseUser != null) {
                    nameTextFirst.setText(parseUser.getString(ProfileBuilder.PROFILE_KEY_NAME));

                    List<PhotoItem> photoItems = parseUser.getList(ProfileBuilder.PROFILE_KEY_PHOTOS);
                    if(photoItems != null && photoItems.size() > 0 && photoItems.get(0) != null && photoItems.get(0) != JSONObject.NULL) {
                        PhotoItem mainPhoto = photoItems.get(0);
                        mainPhoto.fetchIfNeededInBackground(new GetCallback<PhotoItem>() {
                            @Override
                            public void done(PhotoItem photoItem, ParseException e) {
                                if(photoItem != null && e == null) {
                                    PhotoFile mainFile = photoItem.getPhotoFiles().get(0);
                                    Picasso.with(mContext).load(mainFile.url).fit().centerCrop().into(thumbImageFirst);
                                }
                            }
                        });
                    } else {
                        Picasso.with(mContext).load(R.drawable.com_facebook_profile_picture_blank_portrait).fit().centerCrop().into(thumbImageFirst);
                    }
                }
            }
        });

        // fill second user
        DatabaseHelper.getUserById(match.getSecondId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(parseUser != null) {
                    nameTextSecond.setText(parseUser.getString(ProfileBuilder.PROFILE_KEY_NAME));

                    List<PhotoItem> photoItems = parseUser.getList(ProfileBuilder.PROFILE_KEY_PHOTOS);
                    if(photoItems != null && photoItems.size() > 0 && photoItems.get(0) != null && photoItems.get(0) != JSONObject.NULL) {
                        PhotoItem mainPhoto = photoItems.get(0);
                        mainPhoto.fetchIfNeededInBackground(new GetCallback<PhotoItem>() {
                            @Override
                            public void done(PhotoItem photoItem, ParseException e) {
                                if(photoItem != null && e == null) {
                                    PhotoFile mainFile = photoItem.getPhotoFiles().get(0);
                                    Picasso.with(mContext).load(mainFile.url).fit().centerCrop().into(thumbImageSecond);
                                }
                            }
                        });
                    } else {
                        Picasso.with(mContext).load(R.drawable.com_facebook_profile_picture_blank_portrait).fit().centerCrop().into(thumbImageSecond);
                    }
                }
            }
        });

        return convertView;
    }
}

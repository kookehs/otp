package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mrcornman.otp.R;
import com.mrcornman.otp.activities.PhotoSelectorActivity;
import com.mrcornman.otp.models.gson.PhotoFile;
import com.mrcornman.otp.models.models.PhotoItem;
import com.mrcornman.otp.utils.PrettyTime;
import com.mrcornman.otp.utils.ProfileBuilder;
import com.mrcornman.otp.views.EditTextBorder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditProfileFragment extends Fragment {

    public final static int REQUEST_PHOTO_SELECTOR = 1;

    private final static String KEY_INDEX = "index";
    private final static String KEY_URL = "url";

    private EditTextBorder aboutEditText;
    private EditTextBorder wantEditText;

    private ImageView[] pictureImages;
    private ProgressBar[] pictureProgressBars;

    private OnEditProfileInteractionListener onEditProfileInteractionListener;

    private ParseUser user;

    public static EditProfileFragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        return fragment;
    }

    public EditProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        user = ParseUser.getCurrentUser();

        pictureProgressBars = new ProgressBar[] {
                (ProgressBar) rootView.findViewById(R.id.progress_0),
                (ProgressBar) rootView.findViewById(R.id.progress_1),
                (ProgressBar) rootView.findViewById(R.id.progress_2),
                (ProgressBar) rootView.findViewById(R.id.progress_3)
        };

        pictureImages = new ImageView[] {
                (ImageView) rootView.findViewById(R.id.picture_image_0),
                (ImageView) rootView.findViewById(R.id.picture_image_1),
                (ImageView) rootView.findViewById(R.id.picture_image_2),
                (ImageView) rootView.findViewById(R.id.picture_image_3)
        };


        List<PhotoItem> photoItems = user.getList(ProfileBuilder.PROFILE_KEY_PHOTOS);
        if (photoItems != null && photoItems.size() == ProfileBuilder.MAX_NUM_PHOTOS) {
            for(int i = 0; i < ProfileBuilder.MAX_NUM_PHOTOS; i++) {
                final int index = i;

                // first initialize the click listener for the image slot
                pictureImages[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onEditProfileInteractionListener.onRequestPhotoSelect(index);
                        Intent intent = new Intent(getActivity(), PhotoSelectorActivity.class);
                        intent.putExtra("slot_index", index);
                        startActivityForResult(intent, REQUEST_PHOTO_SELECTOR);
                    }
                });

                // now fill the image
                if(photoItems.get(i) != null && photoItems.get(i) != JSONObject.NULL) {
                    PhotoItem photo = photoItems.get(i);
                    photo.fetchIfNeededInBackground(new GetCallback<PhotoItem>() {
                        @Override
                        public void done(PhotoItem photoItem, com.parse.ParseException e) {
                            if(photoItem != null && e == null) {
                                PhotoFile photoFile = photoItem.getPhotoFiles().get(0);
                                Picasso.with(getActivity().getApplicationContext()).load(photoFile.url).fit().centerCrop().into(pictureImages[index]);
                            }
                        }
                    });
                } else {
                    if(index == 0) {
                        Picasso.with(getActivity().getApplicationContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).fit().centerCrop().into(pictureImages[index]);
                    } else {
                        Picasso.with(getActivity().getApplicationContext()).load(R.mipmap.placeholder_add).fit().centerCrop().into(pictureImages[index]);
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), "Your account has become corrupted. Please contact us for assistance.", Toast.LENGTH_LONG).show();
            return rootView;
        }

        TextView nameText = (TextView) rootView.findViewById(R.id.name_text);
        TextView ageText = (TextView) rootView.findViewById(R.id.age_text);

        nameText.setText(user.getString(ProfileBuilder.PROFILE_KEY_NAME) + ", ");
        ageText.setText(PrettyTime.getAgeFromBirthDate(user.getDate(ProfileBuilder.PROFILE_KEY_BIRTHDATE)) + "");

        //set the text the user wants
        aboutEditText = (EditTextBorder) rootView.findViewById(R.id.about_edit_text);
        aboutEditText.setHorizontallyScrolling(false);

        if(user.getString(ProfileBuilder.PROFILE_KEY_ABOUT) != null) aboutEditText.setText(user.getString(ProfileBuilder.PROFILE_KEY_ABOUT));
        aboutEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //hide keyboard
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(aboutEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        //EditText for the interested in section of the profile page
        wantEditText = (EditTextBorder) rootView.findViewById(R.id.want_edit_text);
        wantEditText.setHorizontallyScrolling(false);

        if(user.getString(ProfileBuilder.PROFILE_KEY_WANT) != null) {
            wantEditText.setText(user.getString(ProfileBuilder.PROFILE_KEY_WANT));
        }
        wantEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //hide keyboard
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(wantEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(wantEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        boolean isDirty = false;

        String aboutTextStr = aboutEditText.getText().toString();
        if (!aboutTextStr.equals(user.getString(ProfileBuilder.PROFILE_KEY_ABOUT))) {
            user.put(ProfileBuilder.PROFILE_KEY_ABOUT, aboutTextStr);
            isDirty = true;
        }

        String wantTextStr = wantEditText.getText().toString();
        if (!wantTextStr.equals(user.getString(ProfileBuilder.PROFILE_KEY_WANT))) {
            user.put(ProfileBuilder.PROFILE_KEY_WANT, wantTextStr);
            isDirty = true;
        }

        if(isDirty) {
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getActivity().getApplicationContext(), "There was a problem saving your profile. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (getActivity() != null)
                        Toast.makeText(getActivity().getApplicationContext(), "Profile saved!", Toast.LENGTH_LONG).show();
                }
            });
        }

        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onEditProfileInteractionListener = (OnEditProfileInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEditProfileInteractionListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PHOTO_SELECTOR:
                if(resultCode == Activity.RESULT_OK) {
                    String url = data.getStringExtra("photo_url");
                    int index = data.getIntExtra("slot_index", -1);
                    changePicture(index, url);
                }
                break;
        }
    }

    public void changePicture(final int index, String url) {
        if(index < 0 || url == null) {
            Log.e("EditProfileFragment", "There was a problem getting the picture from the PhotoSelector.");
            return;
        }


        final ProgressBar pBar = pictureProgressBars[index];
        pBar.setVisibility(View.VISIBLE);

        final int mIndex = index;
        final String mUrl = url;

        final List<PhotoFile> photoFiles = new ArrayList<PhotoFile>();

        final Target target = new Target(){

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                final Bitmap mBitmap = bitmap;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                final byte[] imageBytes = stream.toByteArray();

                final ParseFile imageFile = new ParseFile("prof_" + mIndex + ".jpg", imageBytes);
                imageFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            return;
                        }

                        PhotoFile photoFile = new PhotoFile();
                        photoFile.width = mBitmap.getWidth();
                        photoFile.height = mBitmap.getHeight();
                        photoFile.url = imageFile.getUrl();
                        photoFiles.add(photoFile);

                        final PhotoItem photo = new PhotoItem();
                        photo.setPhotoFiles(photoFiles);

                        List<PhotoItem> photoItems = user.getList(ProfileBuilder.PROFILE_KEY_PHOTOS);
                        if (photoItems != null && photoItems.size() == ProfileBuilder.MAX_NUM_PHOTOS) {
                            photoItems.set(mIndex, photo);
                        }

                        user.put(ProfileBuilder.PROFILE_KEY_PHOTOS, photoItems);

                        // finally we have all our images and we are done building our profile
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                PhotoFile photoFile = photo.getPhotoFiles().get(0);
                                if (getActivity() != null) {
                                    Picasso.with(getActivity()).load(photoFile.url).fit().centerCrop().into(pictureImages[mIndex]);
                                    pBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(getActivity(), "There was a problem loading your photo. Please try again.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        Picasso.with(getActivity()).load(mUrl).into(target);
    }

    public interface OnEditProfileInteractionListener {
        void onRequestPhotoSelect(int slotIndex);
    }
}
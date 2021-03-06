package com.mrcornman.otp.models.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("Match")
public class MatchItem extends ParseObject {

    // key identifiers
    public static final String MATCH_KEY_FIRST_ID = "first_id";
    public static final String MATCH_KEY_SECOND_ID = "second_id";
    public static final String MATCH_KEY_FOLLOWERS = "followers";
    public static final String MATCH_KEY_LAST_ACTIVITY_DATE = "last_activity_date";
    public static final String MATCH_KEY_NUM_LIKES = "num_likes";
    public static final String MATCH_KEY_NUM_MESSAGES = "num_messages";

    public String getFirstId() {
        return getString(MATCH_KEY_FIRST_ID);
    }
    public void setFirstId(String firstId) {
        put(MATCH_KEY_FIRST_ID, firstId);
    }

    public String getSecondId() {
        return getString(MATCH_KEY_SECOND_ID);
    }
    public void setSecondId(String secondId) {
        put(MATCH_KEY_SECOND_ID, secondId);
    }

    public Date getLastActivityDate() { return getDate(MATCH_KEY_LAST_ACTIVITY_DATE); }
    public void setLastActivityDate(Date lastActivityDate) { put(MATCH_KEY_LAST_ACTIVITY_DATE, lastActivityDate); }

    public int getNumLikes() {
        return getInt(MATCH_KEY_NUM_LIKES);
    }
    public void setNumLikes(int numLikes) {
        put(MATCH_KEY_NUM_LIKES, numLikes);
    }
    public void incrNumLikes() { increment(MATCH_KEY_NUM_LIKES); }

    public int getNumMessages() { return getInt(MATCH_KEY_NUM_MESSAGES); }
    public void setNumMessages(int numMessages) { put(MATCH_KEY_NUM_MESSAGES, numMessages); }
}
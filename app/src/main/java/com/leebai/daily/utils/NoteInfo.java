package com.leebai.daily.utils;

/**
 * Created by swd1 on 17-10-11.
 */

public class NoteInfo {

    private String mTitle;
    private String mContent;
    private String mOriginalText; //text to save, when there is an image, use tag <img img/>
    private String mDisplayText;  //text to show, when there is an image, show [image]
    private long mModifiedTime;

    public NoteInfo() {
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public void setModifiedTime(long mModifiedTime) {
        this.mModifiedTime = mModifiedTime;
    }

    public void setOriginalText(String mOriginalText) {
        this.mOriginalText = mOriginalText;
    }

    public void setDisplayText(String mDisplayText) {
        this.mDisplayText = mDisplayText;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public long getModifiedTime() {
        return mModifiedTime;
    }

    public String getOriginalText() {
        return mOriginalText;
    }

    public String getDisplayText() {
        return mDisplayText;
    }
}
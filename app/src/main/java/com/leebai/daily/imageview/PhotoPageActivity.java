package com.leebai.daily.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.leebai.daily.R;

import java.util.ArrayList;

/**
 * Created by swd1 on 17-10-26.
 */

public class PhotoPageActivity extends FragmentActivity {

    private ViewPager mPhotoPager;
    private ImagePagerAdapter mPagerAdapter;
    private ArrayList<String> mImagePaths;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_pager);


        Intent intent = getIntent();
        mImagePaths = intent.getStringArrayListExtra("paths");
        mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mImagePaths.size());
        mPhotoPager = findViewById(R.id.image_pager);
        mPhotoPager.setAdapter(mPagerAdapter);
        mPhotoPager.setOffscreenPageLimit(2);

    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoFragment.newInstance(mImagePaths.get(position));
        }
    }
}

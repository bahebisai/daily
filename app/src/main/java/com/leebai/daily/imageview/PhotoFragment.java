package com.leebai.daily.imageview;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.leebai.daily.R;

/**
 * Created by swd1 on 17-10-26.
 */

public class PhotoFragment extends Fragment{

    private static String IMAGE_DATA_EXTRA = "image_data";
    private String mPhotoPath;
    private ImageView mImageView;


    public PhotoFragment() {
    }

    public static PhotoFragment newInstance(String imageUrl) {
        final PhotoFragment f = new PhotoFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoPath = getArguments() == null ? null : getArguments().getString(IMAGE_DATA_EXTRA);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.photo_fragment, container, false);
        mImageView = view.findViewById(R.id.photo);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Glide.with(getContext()).load(mPhotoPath).into(mImageView);
    }
}

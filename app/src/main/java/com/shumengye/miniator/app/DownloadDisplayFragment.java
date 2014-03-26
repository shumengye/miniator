package com.shumengye.miniator.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by shye on 25/03/14.
 */
public class DownloadDisplayFragment extends FlipCardFragment {

    private OnDownloadStartListener mCallbackActivity;
    private ImageView mainImage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Ensure the container activity has implemented callback interface
        try {
            mCallbackActivity = (OnDownloadStartListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDownloadStartListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment state
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_back, container, false);
        mainImage = (ImageView) view.findViewById(R.id.main_image);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Download / fetch from cache image
        OnDownloadStartListener downloadListener = (OnDownloadStartListener) getActivity();
        if (downloadListener != null) {
            downloadListener.startDownload();
        }
    }

    public void showBitmap(Bitmap bitmap) {
        if (mainImage != null) {
            mainImage.setImageBitmap(bitmap);
        }
    }

}

package com.shumengye.miniator.app;

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

    private View mView;
    OnDownloadStartListener mCallbackActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment state
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_card_back, container, false);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnDownloadStartListener downloadListener = (OnDownloadStartListener) getActivity();
        downloadListener.startDownload();
    }

    public void showBitmap(Bitmap bitmap) {
        final ImageView imageView = (ImageView) mView.findViewById(R.id.main_image);
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

}

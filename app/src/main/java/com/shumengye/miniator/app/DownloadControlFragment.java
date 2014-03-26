package com.shumengye.miniator.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by shye on 25/03/14.
 */
public class DownloadControlFragment extends FlipCardFragment {

    private TextView mProgressStatus;
    private Button mStartDownloadButton;
    OnDownloadStartListener mCallbackActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Ensure the container activity has implemented callback interface
        try {
            mCallbackActivity = (OnDownloadStartListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImageLoadListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_card_front, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressStatus = (TextView) view.findViewById(R.id.progress_status);

        mStartDownloadButton = (Button) view.findViewById(R.id.download_button);
        mStartDownloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                OnDownloadStartListener downloadListener = (OnDownloadStartListener) getActivity();
                downloadListener.startDownload();
            }
        });
    }

    public void updateProgress(Integer progress) {
        mProgressStatus.setText("" + progress);
    }
}

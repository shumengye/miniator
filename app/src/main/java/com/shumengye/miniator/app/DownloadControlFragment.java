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

    private OnDownloadStartListener mCallbackActivity;
    private Button mStartDownloadButton;
    private TextView mPreProgressStatus;
    private TextView mProgressStatus;
    private Boolean mHideDownloadButton;

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

        mHideDownloadButton = Boolean.FALSE;

        // Get saved visibility for download button from Bundle
        if (savedInstanceState != null) {
            Integer hideButton = (Integer) savedInstanceState.get("progressSpinning");
            if (hideButton != null) {
                if (hideButton == 1)
                    mHideDownloadButton = Boolean.TRUE;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_front, container, false);

        mStartDownloadButton = (Button) view.findViewById(R.id.download_button);
        mStartDownloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                OnDownloadStartListener downloadListener = (OnDownloadStartListener) getActivity();
                downloadListener.startDownload();
            }
        });

        mPreProgressStatus = (TextView) view.findViewById(R.id.predownload_status);

        mProgressStatus = (TextView) view.findViewById(R.id.progress_status);

        // Hide download button if progress is being displayed
        if (mHideDownloadButton) {
            mStartDownloadButton.setVisibility(View.GONE);
        }

        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mStartDownloadButton.getVisibility() == View.GONE) {
            outState.putInt("progressSpinning", 1);
        }
        else {
            outState.putInt("progressSpinning", 0);
        }

        super.onSaveInstanceState(outState);

    }

    public void onPreDownload() {
        mStartDownloadButton.setVisibility(View.GONE);

        mPreProgressStatus.setVisibility(View.VISIBLE);
    }

    public void updateProgress(Integer progress) {
        mPreProgressStatus.setVisibility(View.GONE);

        mProgressStatus.setVisibility(View.VISIBLE);
        mProgressStatus.setText(progress + "%");
    }

}

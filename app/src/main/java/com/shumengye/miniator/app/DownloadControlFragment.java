package com.shumengye.miniator.app;

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

    private View rootView;
    private TextView mProgressStatus;
    private Button mStartDownloadButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

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

        final Button flipButton = (Button) view.findViewById(R.id.flip_button);
        flipButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mCallback.onFlipCard();
                }
        });

        mStartDownloadButton = (Button) view.findViewById(R.id.download_button);
        mStartDownloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity ac = (MainActivity) mCallback;
                ac.startImageDownload();
            }
        });
    }

    public void updateProgress(Integer progress) {
        mProgressStatus.setText("" + progress);
    }
}

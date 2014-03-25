package com.shumengye.miniator.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by shye on 25/03/14.
 */
public class DownloadDisplayFragment extends FlipCardFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_card_back, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button flipButton = (Button) view.findViewById(R.id.flipbutton);
        if (flipButton != null) {
            flipButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mCallback.onFlipCard();
                }
            });
        }
    }

}

package com.shumengye.miniator.app;

import android.app.Activity;
import android.app.Fragment;

/**
 * Created by shye on 25/03/14.
 */
public class FlipCardFragment extends Fragment {
    OnFlipCardListener mCallback;

    public interface OnFlipCardListener {
        public void onFlipCard();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Ensure the container activity has implemented callback interface
        try {
            mCallback = (OnFlipCardListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFlipCardListener");
        }
    }
}

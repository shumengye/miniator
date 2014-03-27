package com.shumengye.miniator.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;

public class MainActivity extends ActionBarActivity
        implements DownloadControlFragment.OnFlipCardListener,
        ImageLoaderFragment.OnImageLoadListener,
        OnDownloadStartListener {

    private static final String IMAGE_URL = "https://dl.dropboxusercontent.com/u/1638040/minion1.jpg";

    /** True if back card (DownloadDisplayFragment) is visible */
    private Boolean mShowingBackCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find ImageLoaderFragment, create if it doesn't exist
        ImageLoaderFragment loaderFragment = (ImageLoaderFragment)
                getFragmentManager().findFragmentByTag(ImageLoaderFragment.TAG);

        if (loaderFragment == null) {
            loaderFragment = new ImageLoaderFragment();
            getFragmentManager()
                    .beginTransaction().add(loaderFragment, ImageLoaderFragment.TAG).commit();
        }

        // Add download control
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DownloadControlFragment())
                    .commit();
        }

        // TODO: Do not base fragment visibility on back stack size
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            mShowingBackCard = true;
        }
        else {
            mShowingBackCard = false;
        }
    }

    /**
     * @category OnDownloadStartListener.startDownload
     * Initiates image download with ImageLoaderFragment
     */
    public void startDownload() {
        ImageLoaderFragment fragment = (ImageLoaderFragment)
                getFragmentManager().findFragmentByTag(ImageLoaderFragment.TAG);

        // Base target size of image on screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        fragment.loadBitmap(IMAGE_URL, size.x, size.y);
    }

    /**
     * @category ImageLoaderFragment.OnImageLoadListener
     * Actions when download has been initiated but not started yet
     */
    public void onPreImageLoad() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        if (fragment instanceof DownloadControlFragment) {
            DownloadControlFragment controlFragment = (DownloadControlFragment) fragment;
            controlFragment.onPreDownload();
        }
    }

    /**
     * @category ImageLoaderFragment.OnImageLoadListener
     * Displays download progress
     */
    public void onUpdateImageLoad(Integer progress) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        if (fragment instanceof DownloadControlFragment) {
            DownloadControlFragment controlFragment = (DownloadControlFragment) fragment;
            controlFragment.updateProgress(progress);
        }
    }

    /**
     * @category ImageLoaderFragment.OnImageLoadListener
     * Displays downloaded bitmap in image display fragment. If needed, animates fragment transaction with flip effect
     */
    public void showBitmap(final Bitmap bitmap) {
        // Check currently visible fragment
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        // DownloadControlFragment is currently visible
        if (fragment instanceof DownloadControlFragment) {

            // Show bitmap after flip animation
            getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                public void onBackStackChanged() {
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
                    if (fragment instanceof DownloadDisplayFragment) {
                        DownloadDisplayFragment displayFragment = (DownloadDisplayFragment) fragment;
                        displayFragment.showBitmap(bitmap);
                    }
                }
            });

            // Show image display fragment with flip animation
            onFlipCard();
        }
        // DownloadDisplayFragment is currently visible, just show bitmap
        else if (fragment instanceof DownloadDisplayFragment) {
            DownloadDisplayFragment displayFragment = (DownloadDisplayFragment) fragment;
            displayFragment.showBitmap(bitmap);
        }
    }

    /**
     * @category DownloadControlFragment.OnFlipCardListener
     * Card flip animation between front and back card (DownloadControlFragment and DownloadDisplayFragment)
     */
    public void onFlipCard() {
        // Back card is visible, pop back fragment from back stack
        if (mShowingBackCard) {
            mShowingBackCard = false;
            getFragmentManager().popBackStack();
            return;
        }

        // Front card is visible, add fragment for the back of the card
        getFragmentManager()
                .beginTransaction()
                // Flip animations
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, new DownloadDisplayFragment())
                .addToBackStack(null)
                .commit();

        mShowingBackCard = true;
    }
}

package com.shumengye.miniator.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity
        implements DownloadControlFragment.OnFlipCardListener,
        ImageLoaderFragment.OnImageLoadListener {

    /** URL of image download */
    private static final String sImageUrl = "https://dl.dropboxusercontent.com/u/986362/minion1.jpg";
    //private static final String sImageUrl = "192.168.0.2:8000/minion1.jpg";
    /** True if back card fragment (DownloadDisplayFragment) is visible */
    private Boolean mShowingBackCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find image loader fragment, create if it doesn't exist
        ImageLoaderFragment fragment = (ImageLoaderFragment)
                getFragmentManager().findFragmentByTag(ImageLoaderFragment.TAG);

        if (fragment == null) {
            fragment = new ImageLoaderFragment();
            getFragmentManager()
                    .beginTransaction().add(fragment, ImageLoaderFragment.TAG).commit();
        }

        if (savedInstanceState == null) {
            Log.d("","CREATING NEW DOWNLOAD CONTROL");
            getFragmentManager()
                    .beginTransaction()
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startImageDownload() {
        Log.d("DEBUG", "Download image");
        ImageLoaderFragment fragment = (ImageLoaderFragment)
                getFragmentManager().findFragmentByTag(ImageLoaderFragment.TAG);

        fragment.loadImage(sImageUrl, 500, 500);
    }

    /**
     * @category ImageLoaderFragment.OnImageLoadListener
     * Actions when download has been initiated but not started yet
     */
    public void onPreImageLoad(Integer progress) {

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
     * Displays downloaded bitmap
     */
    public void showBitmap(final Bitmap bitmap) {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

        if (fragment instanceof DownloadControlFragment) {

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
            {
            public void onBackStackChanged() {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.container);

                if (fragment instanceof DownloadDisplayFragment) {
                    DownloadDisplayFragment displayFragment = (DownloadDisplayFragment) fragment;
                    displayFragment.showBitmap(bitmap);
                }
            }
        });

            onFlipCard();
        }
        else {
            DownloadDisplayFragment displayFragment = (DownloadDisplayFragment) fragment;
            displayFragment.showBitmap(bitmap);
        }
    }

    /**
     * @category DownloadControlFragment.OnFlipCardListener
     * Card flip animation between a front and back card fragment (DownloadControlFragment and DownloadDisplayFragment)
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

                // Custom animations for transaction
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace current fragment in container view with fragment for back card
                .replace(R.id.container, new DownloadDisplayFragment())

                 // Add this transaction to the back stack
                .addToBackStack(null)
                .commit();

        mShowingBackCard = true;
    }
}

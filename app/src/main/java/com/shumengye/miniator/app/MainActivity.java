package com.shumengye.miniator.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity
        implements DownloadControlFragment.OnFlipCardListener{

    /** True if back card fragment (DownloadDisplayFragment) is visible */
    private Boolean mShowingBackCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new DownloadControlFragment())
                    .commit();
        }


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

    /**
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

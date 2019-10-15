package com.ryan3r.bustimes;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

/**
 * The base class for all activities
 */

class BaseActivity extends AppCompatActivity {
    // the amount to darken the status bar by
    private static final int STATUS_BAR_DARKEN_LEVEL = 35;

    /**
     * Darken a color to put in the status bar
     * @param orig The original color
     * @return The darker color
     */
    static int darken(int orig) {
        int red = Color.red(orig);
        int green = Color.green(orig);
        int blue = Color.blue(orig);

        red -= STATUS_BAR_DARKEN_LEVEL;
        green -= STATUS_BAR_DARKEN_LEVEL;
        blue -= STATUS_BAR_DARKEN_LEVEL;

        if(red < 0) red = 0;
        if(green < 0) green = 0;
        if(blue < 0) blue = 0;

        return Color.argb(255, red, green, blue);
    }

    /**
     * Set the color for this activity
     * @param colorInt The color int to use
     */
    protected void setColor(int colorInt) {
        // set the action bar color
        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colorInt));
        }

        Window window = getWindow();

        // set the status bar color
        window.setStatusBarColor(/*darken(*/colorInt/*)*/);
    }

    /**
     * Show the back button in the status bar
     */
    protected void enableBackButton() {
        ActionBar actionBar = getSupportActionBar();

        if(actionBar == null) return;

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Handle back button presses
     * @param item The menu item pressed
     * @return Whether the event was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return false;
    }
}

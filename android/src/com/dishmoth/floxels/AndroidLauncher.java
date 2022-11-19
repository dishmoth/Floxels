/*
 *  AndroidLauncher.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

// This launcher is consistent with the standard libGDX set up.
// But the copy in com.dishmoth.floxels.android is the one that's used.
public class AndroidLauncher extends AndroidApplication {
	
  @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lockScreenOrientation();
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useWakelock = true;
		initialize(new FloxelsGame(), config);
	}
  
  private void lockScreenOrientation() {
    int orientation = getResources().getConfiguration().orientation;
    if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
  }
  
}

/*
 *  TimingStats.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// collect timing statistics as the game is running
class TimingStats {

  // show frame rate on screen (for development)
  private static final boolean kOnscreenReport = false;
  
  // time between reports
  private static final float kReportSeconds    = 3.0f,
                             kFpsReportSeconds = 1.0f;

  // display the frame rate if it is consistently below this value
  private static final int kFpsDisplayCutoff = 95;
  
  // assorted measurements
  private int   mNumUpdates;
  private float mTotalSeconds,
                mMinSeconds,
                mMaxSeconds;

  // value (average frames-per-second as percentage) to show on screen
  private int   mFpsUpdates,
                mFpsValue,
                mFpsValueOld1,
                mFpsValueOld2;
  private float mFpsSeconds;
  
  // font for on-screen frame rate
  private BitmapFont mFont = null;
  
  // constructor
  public TimingStats() { 

    clear();
    clearFps();

    mFpsValue = mFpsValueOld1 = mFpsValueOld2 = 0;
    
    if ( kOnscreenReport ) {
      mFont = new BitmapFont();
    }
  
  } // constructor
  
  // reset counters
  public void clear() {

    mNumUpdates = 0;
    mTotalSeconds = mMinSeconds = mMaxSeconds = 0.0f;
    
  } // clear()
  
  // reset fps counters
  private void clearFps() {
    
    mFpsUpdates = 0;
    mFpsSeconds = 0.0f;
    
  } // clearFps()
  
  // update timing statistics after each tick 
  public void update(float seconds) {
    
    mTotalSeconds += seconds;
    mFpsSeconds += seconds;
    
    if ( mNumUpdates == 0 ) {
      mMinSeconds = mMaxSeconds = seconds;
    } else {
      if ( mMinSeconds > seconds ) mMinSeconds = seconds;
      if ( mMaxSeconds < seconds ) mMaxSeconds = seconds;
    }
    
    mNumUpdates++;
    mFpsUpdates++;
    
    if ( mTotalSeconds > kReportSeconds ) {
      Env.debug( Env.decimalPlaces(mNumUpdates/mTotalSeconds, 1)
               + " fps (mean="
               + Env.decimalPlaces(1000*mTotalSeconds/mNumUpdates, 1)
               + "ms, min="
               + Env.decimalPlaces(1000*mMinSeconds, 1)
               + "ms, max="
               + Env.decimalPlaces(1000*mMaxSeconds, 1)
               + "ms)" );
      clear();
    }

    if ( mFpsSeconds >= kFpsReportSeconds ) {
      mFpsValueOld2 = mFpsValueOld1;
      mFpsValueOld1 = mFpsValue;
      
      mFpsValue = Math.round( 100 * (mFpsUpdates/mFpsSeconds) 
                              / Env.TICKS_PER_SEC );

      if ( mFpsValue     > 0 && mFpsValue     <= kFpsDisplayCutoff &&
           mFpsValueOld1 > 0 && mFpsValueOld1 <= kFpsDisplayCutoff &&
           mFpsValueOld2 > 0 && mFpsValueOld2 <= kFpsDisplayCutoff ) {
        Env.setFrameRate(mFpsValue);
      } else {
        Env.setFrameRate(100);
      }
      
      clearFps();
    }
    
  } // update()

  // show the frame rate on screen
  public void display(SpriteBatch batch) {
    
    if ( kOnscreenReport ) {
      mFont.draw(batch, 
                 "fps: " + mFpsValue + "%", 
                 Env.gameOffsetX()+5, Env.gameOffsetY()+20);
    }

  } // display
  
} // class TimingStats

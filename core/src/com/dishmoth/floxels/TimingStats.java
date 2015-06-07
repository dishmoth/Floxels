/*
 *  TimingStats.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// collect timing statistics as the game is running
class TimingStats {

  // show frame rate on screen
  private static final boolean kOnscreenReport = true;
  
  // time between reports
  private static final float kReportSeconds = 3.0f;
  
  // assorted measurements
  private int   mNumUpdates;
  private float mTotalSeconds,
                mMinSeconds,
                mMaxSeconds;

  // value (average frames-per-second) to show on screen
  private float mOnscreenValue;
  
  // font for on-screen frame rate
  private BitmapFont mFont = null;
  
  // constructor
  public TimingStats() { 

    clear(); 
    
    if ( kOnscreenReport ) {
      mFont = new BitmapFont();
      mOnscreenValue = 0.0f;
    }
  
  } // constructor
  
  // reset counters
  public void clear() {

    mNumUpdates = 0;
    mTotalSeconds = mMinSeconds = mMaxSeconds = 0.0f;
    
  } // clear()
  
  // update timing statistics after each tick 
  public void update(float seconds) {
    
    mTotalSeconds += seconds;
    
    if ( mNumUpdates == 0 ) {
      mMinSeconds = mMaxSeconds = seconds;
    } else {
      if ( mMinSeconds > seconds ) mMinSeconds = seconds;
      if ( mMaxSeconds < seconds ) mMaxSeconds = seconds;
    }
    
    mNumUpdates++;
    
    if ( mTotalSeconds > kReportSeconds ) {
      Env.debug( String.format("%.1f", mNumUpdates/mTotalSeconds)
               + " fps (mean="
               + String.format("%.1f", 1000*mTotalSeconds/mNumUpdates)
               + "ms, min="
               + String.format("%.1f", 1000*mMinSeconds)
               + "ms, max="
               + String.format("%.1f", 1000*mMaxSeconds)
               + "ms)" );
      mOnscreenValue = mNumUpdates/mTotalSeconds;
      clear();
    }
    
  } // update()

  // show the frame rate on screen
  public void display(SpriteBatch batch) {
    
    if ( !kOnscreenReport ) return;
    
    mFont.draw(batch, 
               "fps: " + String.format("%.1f", mOnscreenValue), 
               Env.gameOffsetX()+5, Env.gameOffsetY()+20);

  } // display
  
} // class TimingStats

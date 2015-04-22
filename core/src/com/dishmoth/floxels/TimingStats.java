/*
 *  TimingStats.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// collect timing statistics as the game is running
class TimingStats {

  // time between reports
  private static final float kReportSeconds = 3.0f;
  
  // assorted measurements
  private int   mNumUpdates;
  private float mTotalSeconds,
                mMinSeconds,
                mMaxSeconds;
  
  // constructor
  public TimingStats() { 

    clear(); 
  
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
      clear();
    }
    
  } // update()
  
} // class TimingStats

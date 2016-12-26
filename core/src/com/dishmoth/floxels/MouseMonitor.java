/*
 *  MouseMonitor.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;

// monitor mouse behaviour within game component
public class MouseMonitor {

  // record of the state of the mouse pointer
  public static class State {
    public int x, y; // (in pixels, or -1)
    public boolean b; // (button 1)
    public State(int xx, int yy, boolean bb) { x=xx; y=yy; b=bb; }
  } // class MouseMonitor.State
  
  // current state of the pointer (position is in pixels)
  private int     mPointerX,
                  mPointerY;
  private boolean mButton;
  
  // number of updates until the mouse is enabled again
  private int mDisableTimer;
  
  // constructor
  public MouseMonitor() {
    
    mPointerX = mPointerY = -1;
    mButton = false;
    
    mDisableTimer = 0;
    
  } // constructor
  
  // retrieve the current state of the pointer
  public State getState() {

    return new State(mPointerX, mPointerY, mButton);
    
  } // getState()
  
  // update current state of the pointer and the main button
  public void updateState() {

    if ( mDisableTimer > 0 ) {
      mDisableTimer -= 1;
      mPointerX = mPointerY = -1;
      mButton = false;
      return;
    }
    
    mButton = false;
    
    final int numPointers = 2;
    for ( int ptrInd = 0 ; ptrInd < numPointers ; ptrInd++ ) {
      if ( Gdx.input.isTouched(ptrInd) ) {
        float x = Gdx.input.getX(ptrInd),
              y = Gdx.input.getY(ptrInd);
        mPointerX = (int)x;
        mPointerY = (int)(Gdx.graphics.getHeight() - y);
        mButton = true;
      }
    }
    
  } // updateState()
  
  // disable the mouse for a number of frames
  public void disableMouse(int disableTime) {
 
    assert( disableTime > 0 );
    mDisableTimer = Math.max(mDisableTimer, disableTime);
    mPointerX = mPointerY = -1;
    mButton = false;
    
  } // disableMouse()
  
} // class MouseMonitor

/*
 *  MouseMonitor.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;

// monitor mouse behaviour within game component
public class MouseMonitor {

  // record of the state of the mouse pointer
  public static class State {
    public int x, y; // (in pixels, or -1)
    public boolean b1; // (button 1)
    public boolean b2; // (button 2)
    public State(int xx, int yy, boolean ba, boolean bb) 
      { x=xx; y=yy; b1=ba; b2=bb; }
  } // class MouseMonitor.State
  
  // current state of the pointer (position is in pixels)
  int     mPointerX,
          mPointerY;
  boolean mButton1,
          mButton2;
  
  // constructor
  public MouseMonitor() {
    
    mPointerX = mPointerY = -1;
    mButton1 = mButton2 = false;
    
  } // constructor
  
  // retrieve the current state of the pointer
  public State getState() {

    return new State(mPointerX, mPointerY, mButton1, mButton2);
    
  } // getState()
  
  // update current state of the pointer and the main button
  public void updateState() {

    mButton1 = mButton2 = false;
    
    final int numPointers = 2;
    for ( int ptrInd = 0 ; ptrInd < numPointers ; ptrInd++ ) {
      if ( Gdx.input.isTouched(ptrInd) ) {
        float x = Gdx.input.getX(ptrInd),
              y = Gdx.input.getY(ptrInd);
        mPointerX = (int)x;
        mPointerY = (int)(Gdx.graphics.getHeight() - y);
        mButton1 = true;
      }
    }
    
  } // updateState()
  
} // class MouseMonitor

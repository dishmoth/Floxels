/*
 *  MouseMonitor.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.*;
import java.awt.event.*;

// monitor mouse behaviour within game component
public class MouseMonitor implements MouseListener, MouseMotionListener {

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
  
  // monitor the mouse on this component
  public void monitor(Component target) {
    
    if ( target == null ) return;
    target.addMouseListener(this);
    target.addMouseMotionListener(this);
    
  } // monitor()
  
  // retrieve the current state of the pointer
  public synchronized State getState() {

    return new State(mPointerX, mPointerY, mButton1, mButton2);
    
  } // getState()
  
  // update current state of the pointer and the main button
  // (button: -1 => released, +1 => pressed, 0 => no change)
  private synchronized void updateState(int x, int y, int button1, int button2) {
    
    if ( x >= 0 && x < Env.screenWidth() &&
         y >= 0 && y < Env.screenHeight() ) {
      mPointerX = x;
      mPointerY = y;
      if ( button1 != 0 ) mButton1 = (button1 == +1);
      if ( button2 != 0 ) mButton2 = (button2 == +1);
    } else {
      mPointerX = mPointerY = -1;
      mButton1 = mButton2 = false;
    }
    
  } // updateState()
  
  // functions from MouseListener interface
  public void mouseClicked(MouseEvent e) {
    updateState(e.getX(), e.getY(), 0, 0);
  } // MouseListener.mouseClicked()
  public void mouseEntered(MouseEvent e) {
    updateState(e.getX(), e.getY(), -1, -1);
  } // MouseListener.mouseEntered()
  public void mouseExited(MouseEvent e) {
    updateState(-1, -1, -1, -1);
  } // MouseListener.mouseExited()
  public void mousePressed(MouseEvent e) {
    int button1 = ( (e.getButton()==MouseEvent.BUTTON1) ? +1 : 0 );
    int button2 = ( (e.getButton()!=MouseEvent.BUTTON1) ? +1 : 0 );
    updateState(e.getX(), e.getY(), button1, button2);
  } // MouseListener.mousePressed()
  public void mouseReleased(MouseEvent e) {
    int button1 = ( (e.getButton()==MouseEvent.BUTTON1) ? -1 : 0 );
    int button2 = ( (e.getButton()!=MouseEvent.BUTTON1) ? -1 : 0 );
    updateState(e.getX(), e.getY(), button1, button2);
  } // MouseListener.mouseReleased()
  
  // functions from MouseMotionListener interface
  public void mouseDragged(MouseEvent e) {
    updateState(e.getX(), e.getY(), 0, 0);
  } // MouseMotionListener.mouseDragged()
  public void mouseMoved(MouseEvent e) {
    updateState(e.getX(), e.getY(), 0, 0);
  } // MouseMotionListener.mouseMoved()
  
} // class MouseMonitor

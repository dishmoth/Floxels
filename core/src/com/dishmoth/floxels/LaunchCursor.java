/*
 *  LaunchCursor.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// initial cursor for launching big batch of floxels 
public class LaunchCursor extends Sprite {

  // story event: launch complete
  public static class EventComplete extends StoryEvent {
  } // LaunchCursor.EventComplete

  // how sprite is displayed relative to others
  private static final int kScreenLayer = 70; 

  // number of floxels launched per frame
  private static final int kLaunchRate = 40;
  
  // size of launch area
  private static final float kLaunchRadius = 0.05f;
  
  // how long the cursor exists for after launch
  static private final float kTime = 0.4f;
  
  // how circle radius grows
  static private final float kRadiusStart = 0.05f,
                             kRadiusEnd   = 2.0f;
  
  // opacity of the circle
  static private final float kAlphaStart = 1.0f,
                             kAlphaEnd   = 0.0f;
  
  // reference to the floxels
  private Floxels mFloxels;
  
  // which population the cursor launches
  private final int mFloxelType;
  
  // last valid position of the cursor (grid units)
  private float mXPos,
                mYPos;

  // first operation of the cursor is to summon floxels
  private int mNumToLaunch;

  // time (seconds) since the launch was started 
  private float mTimer;

  // wait for a clear touch
  private boolean mReady;
  
  // constructor
  public LaunchCursor(int numToLaunch, int floxelType, Floxels floxels) {
    
    super(kScreenLayer);

    mFloxels = floxels;
    mFloxelType = floxelType;
    mNumToLaunch = numToLaunch;
    
    mXPos = mYPos = -1;
    
    mTimer = 0.0f;
    mReady = false;
    
  } // constructor
  
  // release floxels and animate the cursor
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = Env.TICK_TIME;

    if ( mTimer == 0.0f ) {

      Env.mouse().updateState();
      MouseMonitor.State state = Env.mouse().getState();
      final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                  y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
      final boolean button = state.b;

      if ( !mReady ) {
        if ( !button ) mReady = true;
        return;
      }
    
      if ( button &&
           x >= 0 && x < Env.numTilesX() && 
           y >= 0 && y < Env.numTilesY() ) {
        mXPos = x;
        mYPos = y;
        mTimer += dt;
        Env.sounds().play(Sounds.UNLEASH_BIG);
      }
      
    } else {

      if ( mNumToLaunch > 0 ) {
        int n = Math.min(mNumToLaunch, kLaunchRate);
        mFloxels.releaseFloxels(mFloxelType, n, mXPos, mYPos, kLaunchRadius);
        mNumToLaunch -= n;
      }
      mTimer += dt;
      if ( mTimer > kTime && mNumToLaunch == 0 ) {
        killTheseSprites.add(this);
        newStoryEvents.add(new EventComplete());
      }

    }
    
  } // Sprite.advance()

  // display the cursor
  @Override
  public void draw(SpriteBatch batch) {
    
    float h = mTimer/kTime;
    if ( h < 0.0f || h > 1.0f ) return;
    
    float radius = (1-h)*kRadiusStart + h*kRadiusEnd;
    float alpha  = (1-h)*kAlphaStart + h*kAlphaEnd;
    
    Env.painter().hoopPainter().drawHoop(batch, mXPos, mYPos, radius, alpha);
    
  } // Sprite.draw()

} // class LaunchCursor

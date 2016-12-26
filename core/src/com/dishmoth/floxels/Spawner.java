/*
 *  Spawner.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// an animation when releasing extra floxels
public class Spawner extends Sprite {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 60; 

  // times at which things happen
  static private final float kTimeTotal   = 0.4f,
                             kTimeRelease = 0.2f;
  
  // how circle radius grows
  static private final float kRadiusStart = 0.05f,
                             kRadiusEnd   = 0.6f;
  
  // opacity of the circle
  static private final float kAlphaStart = 0.8f,
                             kAlphaEnd   = 0.0f;
  
  // reference to the floxels object
  final private Floxels mFloxels;
  
  // which type of floxels to release
  final private int mFloxelType;

  // number of floxels to release
  final private int mNumToRelease;
  
  // position of the release point 
  private float mXPos,
                mYPos;

  // time since creation
  private float mTimer;
  
  // constructor
  public Spawner(Floxels floxels, 
                 float x, float y, int type, int num,
                 float delay) {
    
    super(kScreenLayer);
    
    mFloxels = floxels;
    
    mXPos = x;
    mYPos = y;
    
    assert( num > 0 );
    mNumToRelease = num;
    mFloxelType   = type;
    
    assert( delay >= 0.0f );
    mTimer = -delay;
    
  } // constructor
  
  // animate the release of floxels
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    float oldTimer = mTimer;
    mTimer += Env.TICK_TIME;
    
    if ( oldTimer < kTimeRelease && mTimer >= kTimeRelease ) {
      mFloxels.releaseFloxels(mFloxelType, mNumToRelease, 
                              mXPos, mYPos, kRadiusStart);
    }
    
    if ( mTimer >= kTimeTotal ) {
      killTheseSprites.add(this);
    }
    
  } // Sprite.advance()

  // display the circle and floxels
  @Override
  public void draw(SpriteBatch batch) {

    float h = mTimer/kTimeTotal;
    if ( h < 0.0f || h > 1.0f ) return;
    
    float radius = (1-h)*kRadiusStart + h*kRadiusEnd;
    float alpha  = (1-h)*kAlphaStart + h*kAlphaEnd;
    
    Env.painter().hoopPainter().drawHoop(batch, mXPos, mYPos, radius, alpha);
    
  } // Sprite.draw()

} // class Spawner

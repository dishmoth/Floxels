/*
 *  LaunchCursor.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

//import java.awt.BasicStroke;
//import java.awt.Graphics2D;
//import java.awt.Color;
//import java.awt.RenderingHints;
//import java.awt.Stroke;
//import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// object for launching floxels at the position of a mouse click
public class LaunchCursor extends Sprite implements SourceTerm {

  // story event: the floxels have all been released
  public static class EventLaunchComplete extends StoryEvent {}

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70; 

  // time until cursor is ready for launch
  private static final float kSecondsTillStart = 0.2f;
  
  // floxels launched per second
  private static final float kLaunchRate = 300.0f;
  
  // spread of launched floxels
  private static final float kLaunchRadius = 0.2f;

  // gap to leave at the edge of the game area (tile units)
  private static final float kMargin = 0.15f;
  
  // how many hovering floxels to draw
  private static final int kHoverNumber = 5;
  
  // how spread out the hovering floxels are drawn (tile units)
  private static final float kHoverRadius = 0.1f;

  // size of the bounding hoop (pixels)
  private static final float kHoopRadiusMin = 2.0f,
                             kHoopRadiusMax = 10.3f;
  
  // appearance of the bounding hoop
//  private static final Stroke kHoopLineStrokes[] = { new BasicStroke(6.8f),
//                                                     new BasicStroke(4.5f),
//                                                     new BasicStroke(1.8f) };
//  private static final Color kHoopLineColours[] = { new Color(0,0,0, 100),
//                                                    new Color(0,0,0),
//                                                    new Color(200,200,200) };
  
  // how strongly other floxels avoid the launch position
  private static final float kRepulsionStrength = 1000.0f;
  
  // seconds until the cursor is active
  private float mStartTime;
  
  // ready for a button press
  private boolean mTrigger;

  // record of mouse's current position (or -1)
  private float mMouseXPos,
                mMouseYPos;

  // number of floxels remaining to launch
  private int mLaunchNum;

  // position of launch (or -1)
  private float mLaunchXPos,
                mLaunchYPos;
  
  // the type of floxel to launch
  private int mFloxelType;
  
  // reference to the floxels
  private Floxels mFloxels;
  
  // constructor
  public LaunchCursor(int num, int type, Floxels floxels) {
    
    super(kScreenLayer);

    assert( num > 0 );
    
    mStartTime = kSecondsTillStart;
    
    mTrigger = false;
    mMouseXPos = mMouseYPos = -1;
    
    mLaunchNum = num;
    mLaunchXPos = mLaunchYPos = -1;

    mFloxelType = type;
    mFloxels = floxels;
    
  } // constructor
  
  // capture mouse state
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    Env.mouse().updateState();
    MouseMonitor.State state = Env.mouse().getState();
    
    mMouseXPos = (state.x - Env.gameOffsetX())/(float)Env.tileWidth();
    mMouseYPos = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
    if ( mMouseXPos <= 0 || mMouseXPos >= Env.numTilesX() || 
         mMouseYPos <= 0 || mMouseYPos >= Env.numTilesY() ) {
      mMouseXPos = mMouseYPos = -1;
    } else {
      mMouseXPos = Math.max(kMargin, 
                            Math.min(Env.numTilesX()-kMargin, mMouseXPos));
      mMouseYPos = Math.max(kMargin, 
                            Math.min(Env.numTilesY()-kMargin, mMouseYPos));
    }

    if ( mStartTime == kSecondsTillStart && 
         mMouseXPos >= 0 && mMouseYPos >= 0 ) {
      Env.sounds().playSpawnSound();
    }
    if ( mStartTime > 0.0f ) {
      mStartTime = Math.max(0.0f, mStartTime-Env.TICK_TIME);
    }
    
    if ( mLaunchXPos < 0 && mLaunchYPos < 0 ) {
      if ( state.b1 ) {
        if ( mTrigger ) {
          if ( mMouseXPos > 0 && mMouseYPos > 0 && mStartTime == 0 ) { 
            mLaunchXPos = mMouseXPos;
            mLaunchYPos = mMouseYPos;
            Env.sounds().playUnleashSound();
          }
          mTrigger = false;
        }
      } else {
        mTrigger = true;
      }
    }

    if ( mLaunchXPos >= 0 && mLaunchYPos >= 0 ) {
      final int num = Math.min( Math.round(kLaunchRate/Env.TICKS_PER_SEC), 
                                mLaunchNum );
      mFloxels.releaseFloxels(mFloxelType, num, 
                              mLaunchXPos, mLaunchYPos, kLaunchRadius);
      mLaunchNum -= num;
    }
    
    if ( mLaunchNum == 0 ) {
      newStoryEvents.add( new EventLaunchComplete() );
      killTheseSprites.add(this);
    }
    
  } // Sprite.advance()

  // repel floxels at the position of the cursor
  public void addToSource(int floxelType, float source[][], int refineFactor) {
    
    if ( floxelType != 0 ) return;
    
    float x, y;
    if ( mLaunchXPos >= 0 && mLaunchYPos >= 0 ) {
      x = mLaunchXPos;
      y = mLaunchYPos;
    } else if ( mMouseXPos >= 0 && mMouseYPos >= 0 ) {
      x = mMouseXPos;
      y = mMouseYPos;
    } else {
      return;
    }
    
    final int ix = (int)Math.floor(x*refineFactor),
              iy = (int)Math.floor(y*refineFactor);
    source[iy][ix] += kRepulsionStrength;
    
  } // SourceTerm.addToSource()
  
  // draw some floxels around the mouse pointer
  @Override
  public void draw(SpriteBatch batch) {
    
    if ( mLaunchXPos >= 0 || mLaunchYPos >= 0 ) return;    
    if ( mMouseXPos < 0 || mMouseYPos < 0 ) return;

    // draw the bounding hoop
    
    final float xHoop = mMouseXPos*Env.tileWidth() + Env.gameOffsetX() + 0.5f,
                yHoop = mMouseYPos*Env.tileWidth() + Env.gameOffsetY() + 0.5f;
    final float t = (1.0f - mStartTime/(float)kSecondsTillStart),
                r = kHoopRadiusMax*t + kHoopRadiusMin*(1-t);
    //RoundRectangle2D hoop = new RoundRectangle2D.Float(xHoop-r, yHoop-r, 
    //                                                   2*r, 2*r, 2*r, 2*r);
    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
    //                    RenderingHints.VALUE_ANTIALIAS_ON);
    //for ( int k = 0 ; k < kHoopLineColours.length ; k++ ) {
    //  g2.setColor(kHoopLineColours[k]);
    //  g2.setStroke(kHoopLineStrokes[k]);
    //  g2.draw(hoop);
    //}
    
    if ( t < 0.5f ) return;
    
    // draw some floxels
    
    Floxel floxel = new Floxel();
    floxel.mState = Floxel.State.NORMAL;
    floxel.mType = (byte)mFloxelType;

    final float hoverRadius = kHoverRadius*t; 
    for ( int k = 0 ; k < kHoverNumber ; k++ ) {
      float dx, dy;
      do {
        dx = Env.randomFloat(-1.0f, +1.0f);
        dy = Env.randomFloat(-1.0f, +1.0f);
      } while ( dx*dx + dy*dy > 1.0 );

      floxel.mX = mMouseXPos + hoverRadius*dx; 
      floxel.mY = mMouseYPos + hoverRadius*dy;
      floxel.mShade = (byte)Env.randomInt( Floxel.NUM_SHADES/4 );
      floxel.mFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
      
      FloxelPainter.draw(batch, floxel);
    }
    
  } // Sprite.draw()

} // class LaunchCursor

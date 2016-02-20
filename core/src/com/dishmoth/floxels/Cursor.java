/*
 *  Cursor.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// the player's cursor, for catching and releasing floxels 
public class Cursor extends Sprite implements SourceTerm {

  // story event: floxels summoned
  public static class EventFloxelsSummoned extends StoryEvent {
  } // LaunchCursor.EventFloxelsSummoned

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70; 

  // how strongly floxels are pulled to the cursor position
  private static final float kAttractStrength = 50.0f;
  private static final float kAttractRange    = 0.4f;
  
  // how strongly other floxels avoid the cursor position
  private static final float kRepulseStrength    = 2.0f,
                             kRepulseStrengthMax = 30.0f;
  private static final int   kLaunchRepulseNum   = 100;

  // how the strength of the cursor's repulsion weakens over time
  private static final int   kFatigueRefine  = 2;
  private static final float kFatigueRate    = 0.5f,
                             kFatigueRadius  = 1.0f;
  private static final float kTimeWasteStart = 3.0f,
                             kTimeWasteEnd   = 20.0f,
                             kTimeWasteDrop  = 0.5f;
  
  // drag floxels into the cursor and capture them
  private static final float kPullRadius    = 0.5f,
                             kCaptureRadius = 0.1f;

  // delay before the cursor can be used again after launch
  private static final float kRelaunchDelay = 1.5f;
  
  // slight delay before the initial summons kicks in
  private static final float kSummonDelay = 0.4f;
  
  // how long the cursor has to be held before summoning again
  private static final float kResummonDelay       = 2.0f,
                             kResummonRepeatDelay = 0.5f;
  private static final int   kResummonNum         = 10;
  
  // how the floxels fill-up the cursor
  private static final int   kFloxelCrowdNumMax    = 200,
                             kFloxelCrowdNumDrawn  = 100;
  private static final float kFloxelCrowdRadiusMax = 0.3f,
                             kFloxelCrowdRadiusMin = 0.03f;
  
  // animate the final face displayed
  private static final float kFinalFaceMinTime = 0.3f,
                             kFinalFaceMaxTime = 1.0f;
  
  // size of the displayed cursor
  private static final float kCursorRadius = 0.44f;

  // how the cursor animates in and out of focus
  private static final float kFocusRate    = 6.0f;
  private static final float kUnfocusScale = 3.0f,
                             kUnfocusAlpha = 0.1f;
  
  // don't play the 'bubble off' sound effect too soon after 'bubble on'
  private static final float kBubbleSoundDelay = 0.25f;
  
  // reference to the floxels
  private Floxels mFloxels;
  
  // which population the cursor acts on
  private final int mFloxelType;
  
  // local helper floxel to simplify painting
  private Floxel mPaintFloxel = null;

  // give special treatment when displaying the last captured floxel  
  private float mFinalFaceTimer;
  private byte  mFinalFace,
                mFinalShade;
  
  // cursor states (unpressed, released, held)
  private enum State { NOTHING, LAUNCHING, CAPTURING };
  
  // current cursor state
  private State mState;

  // last valid position of the cursor (grid units)
  private float mXPos,
                mYPos;

  // first operation of the cursor is to summon floxels
  private final int mNumToSummon;
  private boolean   mSummoning,
                    mInitialSummons;
  private float     mSummonTimer;
  
  // number of captured floxels
  private int mNumCaptured;
  
  // transition state (0.0 => vanished, 1.0 => active, -ve => post-launch delay)
  private float mFocus;

  // how repulsive the launch is
  private float mLaunchRepulseStrength;
  
  // current cursor repulsion strength across the game area (0.0 to 1.0)
  private float mRepulseFatigue[][];
  
  // how long since the floxels were all captured
  private float mTimeWasteTimer;
  
  // how long since we played the 'bubble on' sound effect
  private float mBubbleSoundTimer;
  
  // constructor
  public Cursor(int numToSummon, int floxelType, Floxels floxels) {
    
    super(kScreenLayer);

    mFloxels = floxels;
    mFloxelType = floxelType;
    
    mXPos = mYPos = -1;
    
    mState = State.NOTHING;

    assert( numToSummon >= 0 );
    mNumToSummon = numToSummon;
    mInitialSummons = mSummoning = ( mNumToSummon > 0 );
    mSummonTimer = 0.0f;
    
    mNumCaptured = 0;
    
    mFocus = 0.0f;
    
    mLaunchRepulseStrength = 0;
    mTimeWasteTimer = 0.0f;
    
    final int nx = kFatigueRefine*Env.numTilesX(),
              ny = kFatigueRefine*Env.numTilesY();
    mRepulseFatigue = new float[ny][nx];
    for ( int iy = 0 ; iy < ny ; iy++ ) {
      for ( int ix = 0 ; ix < nx ; ix++ ) {
        mRepulseFatigue[iy][ix] = 1.0f;
      }
    }
    
    mBubbleSoundTimer = 0.0f;
    
    mPaintFloxel = new Floxel();
    mPaintFloxel.mState = Floxel.State.NORMAL;
    mPaintFloxel.mType = (byte)mFloxelType;

    mFinalFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
    mFinalShade = (byte)Env.randomInt( Floxel.NUM_SHADES );
    
  } // constructor
  
  // release all captured floxels immediately
  public void cancel() { 
  
    if ( mNumCaptured > 0 ) {
      mFloxels.releaseFloxels(mFloxelType, mNumCaptured, 
                              mXPos, mYPos, floxelRadius());
      mNumCaptured = 0;
    }
    mState = State.NOTHING;
    if ( !mInitialSummons ) mSummoning = false;
    mSummonTimer = 0.0f;
    
  } // cancel()

  // radius of the captured floxel crowd
  private float floxelRadius() {
    
    if ( mNumCaptured <= 1 ) return 0.0f;
    
    float frac = Math.min(1.0f, mNumCaptured/(float)kFloxelCrowdNumMax);
    float f = (float)Math.sqrt(frac);
    return ( f*kFloxelCrowdRadiusMax + (1-f)*kFloxelCrowdRadiusMin );
    
  } // floxelRadius()
  
  // the number of captured floxels
  public int numCaptured() { return mNumCaptured; }
  
  // whether the cursor is is summoning mode
  public boolean summoning() { return mSummoning; }
  
  // update the cursor state and animate any captured floxels
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = Env.TICK_TIME;

    updateRepulseFatigue(dt);
    
    Env.mouse().updateState();
    MouseMonitor.State state = Env.mouse().getState();
    final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
    boolean button = state.b;

    int numActiveFloxels = mFloxels.numFloxels(mFloxelType);
    
    if ( mState == State.CAPTURING && numActiveFloxels == 0 ) {
      mTimeWasteTimer += dt;
    } else {
      mTimeWasteTimer = 0.0f;
    }
    mBubbleSoundTimer += dt;
    
    if ( mState == State.LAUNCHING ) {
      // launching (animate the cursor)
      mFocus -= dt*kFocusRate;
      if ( mFocus <= -kRelaunchDelay ) {
        mState = State.NOTHING;
        mFocus = 0.0f;
      }
      return;
    }
    
    if ( x >= 0 && x < Env.numTilesX() && y >= 0 && y < Env.numTilesY() ) {
      // update cursor position
      mXPos = x;
      mYPos = y;
    } else if ( mSummoning && mInitialSummons && numActiveFloxels > 0 ) {
      // keep the last position
    } else {
      // cursor has gone off-screen
      button = false;
    }
    
    if ( mSummoning && mState == State.CAPTURING ) {
      // summoning (lock down the button for the initial summons)
      assert( mNumCaptured <= mNumToSummon );
      if ( mNumCaptured == mNumToSummon ) {
        assert( numActiveFloxels == 0 );
        mSummoning = false;
        mInitialSummons = false;
        mSummonTimer = 0.0f;
      } else if ( mInitialSummons && numActiveFloxels > 0 ) {
        button = true;
      }
    }
    
    if ( button ) {
      // button pressed (animate cursor, capture, summon, etc.)
      if ( mState == State.NOTHING ) {
        if ( mSummoning ) {
          assert( mInitialSummons );
          mSummonTimer = kSummonDelay;
        }
      }
      mState = State.CAPTURING;
      if ( numActiveFloxels == 0 && !mSummoning &&
           mNumCaptured < mNumToSummon ) {
        assert( !mInitialSummons );
        mSummoning = true;
        mSummonTimer = kResummonDelay;
      }
      if ( mFocus == 1.0f ) {
        int numCatch = mFloxels.captureFloxels(mXPos, mYPos, 
                                               kPullRadius, kCaptureRadius,
                                               mFloxelType);
        if ( numCatch > 0 ) {
          mNumCaptured += numCatch;
          numActiveFloxels -= numCatch;
          Env.sounds().playCaptureSound(numCatch);
        }
        if ( mSummoning && mSummonTimer > 0.0f ) {
          mSummonTimer = Math.max(mSummonTimer-dt, 0.0f);
          if ( mSummonTimer == 0.0f ) {
            if ( mInitialSummons ) {
              assert( numActiveFloxels == 0 );
              assert( mNumCaptured == 0 );
              mFloxels.summonFloxels(mNumToSummon, mFloxelType);
              Env.sounds().play(Sounds.SUMMON_A);
              Env.sounds().play(Sounds.SUMMON_B, 6);
              newStoryEvents.add(new EventFloxelsSummoned());
            } else {
              int numLeft = mNumToSummon - mNumCaptured - numActiveFloxels;
              int n = (numLeft <= kResummonNum)  ? numLeft
                    : (numLeft < 2*kResummonNum) ? (numLeft+1)/2
                                                 : kResummonNum;
              if ( n > 0 ) {
                mFloxels.summonFloxels(n, mFloxelType);
                Env.sounds().play(Sounds.SUMMON_QUICK);
                mSummonTimer = kResummonRepeatDelay;
              }
            }
          }
        }
      } else {
        mFocus = Math.min(1.0f, mFocus+dt*kFocusRate);
        if ( mFocus == 1.0f ) {
          Env.sounds().play(Sounds.BUBBLE_ON);
          mBubbleSoundTimer = 0.0f;
        }
      }
    } else {
      // button released
      if ( mState == State.CAPTURING ) {
        mState = State.LAUNCHING;
        if ( mNumCaptured > 0 ) {
          float h = Math.min(1.0f, mNumCaptured/(float)kLaunchRepulseNum);
          mLaunchRepulseStrength = (1-h)*kRepulseStrength 
                                   + h*kRepulseStrengthMax; 
          mFloxels.releaseFloxels(mFloxelType, mNumCaptured, 
                                  mXPos, mYPos, floxelRadius());
          Env.sounds().playUnleashSound(mNumCaptured);
          mNumCaptured = 0;
        } else {
          mLaunchRepulseStrength = 0.0f;
        }
        if ( !mInitialSummons ) mSummoning = false;
        mSummonTimer = 0.0f;
        if ( mFocus == 1.0f ) {
          float early = Math.max(kBubbleSoundDelay - mBubbleSoundTimer, 0.0f);
          int delay = Math.round(early/Env.TICK_TIME);
          Env.sounds().play(Sounds.BUBBLE_OFF, delay);
        }
      }
    }

    if ( mState == State.CAPTURING ) {
      // animate the last-drawn (most visible) floxel face
      mFinalFaceTimer -= dt;
      if ( mFinalFaceTimer <= 0.0f ) {
        mFinalFaceTimer = Env.randomFloat(kFinalFaceMinTime, 
                                          kFinalFaceMaxTime);
        mFinalFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
      }
      if ( mFinalShade == 0 )                        mFinalShade += 1;
      else if ( mFinalShade == Floxel.NUM_SHADES-1 ) mFinalShade -= 1;
      else if ( Env.randomBoolean() )                mFinalShade += 1;
      else                                           mFinalShade -= 1;
    }
    
  } // Sprite.advance()

  // interpolate a fatigue value for grid coords (x,y)
  private float repulseFatigue(float x, float y) {

    x = kFatigueRefine*x - 0.5f;
    y = kFatigueRefine*y - 0.5f;
    
    int x0 = (int)Math.floor(x),
        y0 = (int)Math.floor(y);
    int x1 = x0 + 1,
        y1 = y0 + 1;
    float dx = x - x0,
          dy = y - y0;
    x0 = Math.max(x0, 0);
    y0 = Math.max(y0, 0);
    x1 = Math.min(x1, mRepulseFatigue[0].length-1);
    y1 = Math.min(y1, mRepulseFatigue.length-1);
    assert( x0 <= x1 && y0 <= y1 );
    
    float f0 = (1-dx)*mRepulseFatigue[y0][x0] + dx*mRepulseFatigue[y0][x1];
    float f1 = (1-dx)*mRepulseFatigue[y1][x0] + dx*mRepulseFatigue[y1][x1];
    float f = (1-dy)*f0 + dy*f1;
    
    return f;
    
  } // repulseFatigue()
  
  // weaken the repulsion strength where the cursor lingers
  private void updateRepulseFatigue(float dt) {

    float h = (mTimeWasteTimer <= kTimeWasteStart) ? 0.0f
            : (mTimeWasteTimer >= kTimeWasteEnd)   ? 1.0f
            : (mTimeWasteTimer-kTimeWasteStart)/(kTimeWasteEnd-kTimeWasteStart);
    float fbase = (1.0f-h) + h*kTimeWasteDrop; 
    
    final float df = dt*kFatigueRate;
    
    final boolean repulse = (mState == State.CAPTURING && mFocus == 1.0f); 
    
    for ( int iy = 0 ; iy < mRepulseFatigue.length ; iy++ ) {
      for ( int ix = 0 ; ix < mRepulseFatigue[iy].length ; ix++ ) {
        float f0 = fbase;
        if ( repulse ) {
          float dx = (ix+0.5f)/kFatigueRefine - mXPos,
                dy = (iy+0.5f)/kFatigueRefine - mYPos;
          float d2 = (dx*dx + dy*dy)/(kFatigueRadius*kFatigueRadius);
          if ( d2 < 1.0f ) f0 *= d2;
        }
        float f = mRepulseFatigue[iy][ix];
        if      ( f < f0 ) f = Math.min(f0, f+df);
        else if ( f > f0 ) f = Math.max(f0, f-df);
        mRepulseFatigue[iy][ix] = f;
      }
    }
    
  } // updateRepulseFatigue()
  
  // attract and repel floxels
  @Override
  public void addToSource(int floxelType, float[][] source, int refineFactor) {

    float strength = 0.0f;
    if ( floxelType == mFloxelType ) {
      if ( mState == State.CAPTURING && mFocus == 1.0f ) {
        strength = -kAttractStrength;
      }
    } else {
      if ( mState == State.CAPTURING && mFocus == 1.0f  ) {
        strength = +kRepulseStrength * repulseFatigue(mXPos, mYPos);
      } else if ( mState == State.LAUNCHING && mFocus > 0.0f ) {
        strength = mFocus*mLaunchRepulseStrength;
      }
    }
    if ( strength == 0.0f ) return;

    final int range = Math.round(kAttractRange*refineFactor);
    final int ix = (int)Math.floor(mXPos*refineFactor),
              iy = (int)Math.floor(mYPos*refineFactor);

    for ( int dy = -range ; dy <= +range ; dy++ ) {
      for ( int dx = -range ; dx <= +range ; dx++ ) {
        if ( ix+dx < 0 || ix+dx >= source[0].length ||
             iy+dy < 0 || iy+dy >= source.length ) continue;
        float scale = Math.max(Math.abs(dx), Math.abs(dy))/(range+1.0f);
        source[iy+dy][ix+dx] += scale*strength;
      }
    }
    
  } // SourceTerm.addToSource()
  
  // display the cursor and captured floxels
  @Override
  public void draw(SpriteBatch batch) {
    
    if ( mState == State.NOTHING ) return;

    FloxelPainter painter = Env.painter().floxelPainter();
    int colour = mFloxels.floxelColour(mFloxelType);
    
    // draw some floxels
    
    final float crowdRadius = floxelRadius();
    final int crowdNum = Math.min( mNumCaptured, kFloxelCrowdNumDrawn );
    for ( int k = 0 ; k < crowdNum ; k++ ) {
      float dx, dy;
      do {
        dx = Env.randomFloat(-1.0f, +1.0f);
        dy = Env.randomFloat(-1.0f, +1.0f);
      } while ( dx*dx + dy*dy > 1.0 );

      mPaintFloxel.mX = mXPos + crowdRadius*dx; 
      mPaintFloxel.mY = mYPos + crowdRadius*dy;
      
      if ( k == crowdNum-1 ) {
        mPaintFloxel.mShade = mFinalShade;
        mPaintFloxel.mFace = mFinalFace;
      } else {
        mPaintFloxel.mShade = (byte)Env.randomInt( Floxel.NUM_SHADES );
        mPaintFloxel.mFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
      }
      
      painter.draw(batch, mPaintFloxel, colour);
    }
    
    // draw the circle
    
    if ( mFocus > 0.0f ) {
      float scale = 1.0f + (kUnfocusScale-1.0f)*(1.0f-mFocus);
      float alpha = (1.0f-mFocus)*kUnfocusAlpha + mFocus;
      Env.painter().hoopPainter().drawHoop(batch, mXPos, mYPos, 
                                           scale*kCursorRadius, alpha);
    }
    
  } // Sprite.draw()

} // class Cursor

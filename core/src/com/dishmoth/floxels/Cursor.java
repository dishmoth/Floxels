/*
 *  Cursor.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// the player's cursor, for catching and releasing floxels 
public class Cursor extends Sprite implements SourceTerm {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70; 

  // how strongly floxels are pulled to the cursor position
  private static final float kAttractStrength = 50.0f;
  private static final float kAttractRange    = 0.4f;
  
  // how strongly other floxels avoid the cursor position
  private static final float kRepulseStrength    = 2.0f,
                             kRepulseStrengthMax = 30.0f;
  private static final float kLaunchRepulseTime  = 0.8f;
  private static final int   kLaunchRepulseNum   = 100;
  
  // drag floxels into the cursor and capture them
  private static final float kPullRadius    = 0.5f,
                             kCaptureRadius = 0.1f;

  // slight delay before the initial summons kicks in
  private static final float kSummonDelay = 0.4f;
  
  // how the floxels fill-up the cursor
  private static final int   kFloxelCrowdNumMax    = 200,
                             kFloxelCrowdNumDrawn  = 100;
  private static final float kFloxelCrowdRadiusMax = 0.3f,
                             kFloxelCrowdRadiusMin = 0.03f;
  
  // animate the final face displayed
  private static final float kFinalFaceMinTime = 0.3f,
                             kFinalFaceMaxTime = 1.0f;
  
  // size of the displayed cursor
  private static final float kCursorRadius = 0.5f;

  // how the cursor animates in and out of focus
  private static final float kFocusRate    = 6.0f;
  private static final float kUnfocusScale = 3.0f;
  
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
  private boolean   mSummoning;
  private float     mSummonTimer;
  
  // number of captured floxels
  private int mNumCaptured;
  
  // transition state (0.0 => vanished, 1.0 => active) 
  private float mFocus;

  // time during which launch repulsion is applied
  private float mLaunchTimer;
  
  // how repulsive the launch is
  private float mLaunchRepulseStrength;
  
  // ???
  private Texture mTexture = null;
  
  // constructor
  public Cursor(int numToSummon, int floxelType, Floxels floxels) {
    
    super(kScreenLayer);

    mFloxels = floxels;
    mFloxelType = floxelType;
    
    mXPos = mYPos = -1;
    
    mState = State.NOTHING;

    assert( numToSummon >= 0 );
    mNumToSummon = numToSummon;
    mSummoning = ( mNumToSummon > 0 );
    mSummonTimer = 0.0f;
    
    mNumCaptured = 0;
    
    mFocus = 0.0f;
    
    mLaunchTimer = 0.0f;
    mLaunchRepulseStrength = 0;
    
    mPaintFloxel = new Floxel();
    mPaintFloxel.mState = Floxel.State.NORMAL;
    mPaintFloxel.mType = (byte)mFloxelType;

    mFinalFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
    mFinalShade = (byte)Env.randomInt( Floxel.NUM_SHADES );
    
    mTexture = new Texture("Hoop.png");
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
  } // constructor
  
  // release all captured floxels immediately
  public void cancel() { 
  
    if ( mNumCaptured > 0 ) {
      mFloxels.releaseFloxels(mFloxelType, mNumCaptured, 
                              mXPos, mYPos, floxelRadius());
      mNumCaptured = 0;
    }
    mState = State.NOTHING;
    
  } // cancel()

  // radius of the captured floxel crowd
  public float floxelRadius() {
    
    if ( mNumCaptured <= 1 ) return 0.0f;
    
    float frac = Math.min(1.0f, mNumCaptured/(float)kFloxelCrowdNumMax);
    float f = (float)Math.sqrt(frac);
    return ( f*kFloxelCrowdRadiusMax + (1-f)*kFloxelCrowdRadiusMin );
    
  } // floxelRadius()
  
  // the number of captured floxels
  public int numCaptured() { return mNumCaptured; }
  
  // update the cursor state and animate any captured floxels
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = Env.TICK_TIME;

    Env.mouse().updateState();
    MouseMonitor.State state = Env.mouse().getState();
    final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
    boolean button = state.b;

    if ( mState == State.LAUNCHING ) {
      if ( mNumCaptured > 0 ) {
        mFloxels.releaseFloxels(mFloxelType, mNumCaptured, 
                                mXPos, mYPos, floxelRadius());
        mNumCaptured = 0;
      }
      mFocus = Math.max(0.0f, mFocus-dt*kFocusRate);
      mLaunchTimer = Math.max(0.0f, mLaunchTimer-dt);
      if ( mFocus == 0.0f && mLaunchTimer == 0.0f ) mState = State.NOTHING;
      return;
    }
    
    if ( x >= 0 && x < Env.numTilesX() && y >= 0 && y < Env.numTilesY() ) {
      mXPos = x;
      mYPos = y;
    } else if ( mSummoning && mFloxels.numFloxels(mFloxelType) > 0 ) {
      // keep the last position
    } else {
      mState = State.NOTHING;
      mFocus = 0.0f;
      mSummonTimer = 0.0f;
      return;
    }
    
    if ( mSummoning && mState == State.CAPTURING ) {
      if ( mNumCaptured == mNumToSummon ) {
        assert( mFloxels.numFloxels(mFloxelType) == 0 );
        mSummoning = false;
      } else if ( mFloxels.numFloxels(mFloxelType) > 0 ) {
        button = true;
      }
    }
    
    if ( button ) {
      if ( mState == State.NOTHING ) {
        if ( mSummoning ) {
          mSummonTimer = kSummonDelay;
        }
      }
      mState = State.CAPTURING;
      int num = mFloxels.captureFloxels(mXPos, mYPos, 
                                        kPullRadius, kCaptureRadius,
                                        mFloxelType);
      mNumCaptured += num;
      if ( num > 0 ) Env.sounds().playCaptureSound(num);
      if ( mSummoning && mSummonTimer > 0.0f ) {
        mSummonTimer = Math.max(mSummonTimer-dt, 0.0f);
        if ( mSummonTimer == 0.0f ) {
          mFloxels.summonFloxels(mNumToSummon, mFloxelType);
          Env.sounds().play(Sounds.SUMMON_A);
          Env.sounds().play(Sounds.SUMMON_B, 6);
        }
      }
      if ( mFocus < 1.0f ) mFocus = Math.min(1.0f, mFocus+dt*kFocusRate);
    } else {
      if ( mState == State.CAPTURING ) {
        mState = State.LAUNCHING;
        mLaunchTimer = 0.5f;
        float h = Math.min(1.0f, mNumCaptured/(float)kLaunchRepulseNum);
        mLaunchRepulseStrength = (1-h)*kRepulseStrength 
                                 + h*kRepulseStrengthMax; 
      }
    }

    if ( mState == State.CAPTURING ) {
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

  // attract and repel floxels
  @Override
  public void addToSource(int floxelType, float[][] source, int refineFactor) {

    float strength = 0.0f;
    if ( floxelType == mFloxelType ) {
      if ( mState == State.CAPTURING ) {
        strength = -kAttractStrength;
      }
    } else {
      if ( mState == State.CAPTURING ) {
        strength = +kRepulseStrength;
      } else if ( mState == State.LAUNCHING ) {
        strength = (mLaunchTimer/kLaunchRepulseTime)*mLaunchRepulseStrength;
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

    FloxelPainter painter = Env.painter();
    
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
      
      painter.draw(batch, mPaintFloxel);
    }
    
    // draw the circle
    
    float scale = 1.0f + (kUnfocusScale-1.0f)*(1.0f-mFocus);

    float x = Env.gameOffsetX() + mXPos*Env.tileWidth(),
          y = Env.gameOffsetY() + mYPos*Env.tileWidth();
    float size = scale*2.0f*kCursorRadius*Env.tileWidth();

    Color oldColor = batch.getColor();
    batch.setColor(1.0f, 1.0f, 1.0f, mFocus);
    
    batch.draw(mTexture, x-size/2, y-size/2, size, size);
    
    batch.setColor(oldColor);

    
  } // Sprite.draw()

} // class Cursor

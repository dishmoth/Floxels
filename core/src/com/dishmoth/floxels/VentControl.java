/*
 *  VentControl.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// class for adjusting the overall flow around the maze
public class VentControl {

  // a small inward flow from each wall to keep floxels away
  static private final float kInFlowDefault   = 0.2f,
                             kInFlowVariation = 0.05f;

  // details of random drift effect
  static private final float kNoiseTimeScale = 0.1f,
                             kNoiseCeiling   = 2.0f;
  
  // details of hunt effect
  static private final float kTargetRadius = 5.0f,
                             kTargetSpeed  = 2.0f;
  
  // details for tracking a cluster of enemies 
  static private final float kTrackingRadius    = 3.0f,
                             kTrackingResetTime = 5.0f;

  // different tracking modes
  static private final int kModeXOnly  = 0,
                           kModeYOnly  = 1,
                           kModeXYBoth = 2,
                           kNumModes   = 3;
 
  // amount of time before changing tracking mode (seconds)
  static private final float kModeTimeMin = 3.0f,
                             kModeTimeMax = 10.0f;
  
  // how long it takes the effect to change from noise to hunt
  static private final float kActivationTime = 5.0f;
  
  // reference to the flow object
  private Flow mFlow;

  // reference to the floxels, and the floxel type for this flow
  private Floxels mFloxels;
  private int mFloxelType;

  // whether hunt mode is fully active (0.0 to 1.0)
  private float mHuntActive;
  
  // proportion of each effect to apply
  private float mNoiseStrength,
                mHuntStrength;
  
  // current point to focus the flow towards
  private float mTargetX,
                mTargetY;

  // position of a cluster of enemy particles (-1 if not defined)
  private float mTrackingX,
                mTrackingY;
  
  // time (seconds) until the target is changed
  private float mTrackingReset;

  // the current tracking behaviour (enumerated above)
  private int mTrackingMode;

  // if true then only the direct tracking mode is used
  private boolean mNoMercy;
  
  // if true then no tracking is performed this turn
  private boolean mOverrideTracking;
  
  // time until tracking mode changes (seconds)
  private float mModeTimer;
  
  // seconds since the object was created
  private float mNoiseTime;

  // noise generator for random flow
  private Perlin3D mNoise;
  
  // default vent setting
  static public float inFlowDefault() { return kInFlowDefault; }
  
  // constructor
  public VentControl(Flow flow, Floxels floxels, int floxelType) {
    
    mFlow = flow;
    mFloxels = floxels;
    mFloxelType = floxelType;

    mHuntActive = 0.0f;

    mNoiseStrength = 1.0f;
    mHuntStrength = 1.0f;
    
    mTargetX = 0.5f*Env.numTilesX();
    mTargetY = 0.5f*Env.numTilesY();
    
    mTrackingX = mTrackingY = -1;
    mTrackingReset = 0.0f;

    mTrackingMode = kModeXYBoth;
    mNoMercy = false;
    mOverrideTracking = false;
    mModeTimer = kModeTimeMin;
    
    mNoiseTime = 0.0f;
    mNoise = new Perlin3D(Env.randomInt(10000));
    
  } // constructor

  // modify the hunting urge (0.0 => none, 1.0 => full)
  public void setHuntStrength(float strength) {
    
    assert( strength >= 0.0f );
    mHuntStrength = strength;
    
  } // setHuntStrength()

  // set the hunting mode to tireless tracking
  public void setNoMercy(boolean val) { mNoMercy = val; }

  // specify a target position explicitly (for next advance only)
  public void overrideTracking(float targetX, float targetY, float strength) {
    
    mOverrideTracking = true;
    mTargetX = targetX;
    mTargetY = targetY;
    mHuntStrength = strength;
    mHuntActive = 1.0f;
    mNoMercy = true;
    
  } // overrideTracking()
  
  // change over teams
  public void switchFloxelTypes() {
    
    mFloxelType = 1 - mFloxelType;
    
  } // switchFloxelTypes()
  
  // adjust the vents to control the flow
  public void advance() {

    mNoiseTime += Env.TICK_TIME;
    mNoiseTime = Env.fold(mNoiseTime, Perlin3D.period());

    trackEnemy();
    updateTarget();
    
    FlowBlock blocks[][] = mFlow.blocks();
    for ( int iy = 0 ; iy < blocks.length ; iy++ ) {
      for ( int ix = 0 ; ix < blocks[iy].length ; ix++ ) {
        final float x = ix + 0.5f,
                    y = iy + 0.5f;
        
        final float ampNoise = inFlowNoise(x, y);
        final float ampHunt  = inFlowHunt(x, y);
        
        assert( mHuntActive >= 0.0f && mHuntActive <= 1.0f );
        final float amp = (1-mHuntActive)*ampNoise + mHuntActive*ampHunt;
        
        final float inFlow = kInFlowDefault + amp*kInFlowVariation;
        
        FlowBlock block = blocks[iy][ix];
        for ( int side = 0 ; side < 4 ; side++ ) {
          if ( block.boundary(side) ) block.setBoundary(side, inFlow);
        }
      }
    }
    
  } // advance()

  // follow a cluster of enemy particles
  private void trackEnemy() {

    if ( mOverrideTracking ) {
      mOverrideTracking = false;
      return;
    }
    
    final int otherType = 1 - mFloxelType;    
    final int count[][] = mFloxels.countFloxels(otherType);
    int max  = 0,
        xMax = -1,
        yMax = -1;
    int maxTrack  = 0,
        xMaxTrack = -1,
        yMaxTrack = -1;
    for ( int iy = 0 ; iy < count.length ; iy++ ) {
      for ( int ix = 0 ; ix < count[iy].length ; ix++ ) {
        if ( count[iy][ix] > max ) {
          max = count[iy][ix];
          xMax = ix;
          yMax = iy;
        }
        if ( mTrackingX >= 0 && mTrackingY >= 0 ) {
          float dx = (ix+0.5f) - mTrackingX,
                dy = (iy+0.5f) - mTrackingY;
          float d  = (float)Math.sqrt(dx*dx + dy*dy) / kTrackingRadius;
          int   c  = Math.round( count[iy][ix] * Math.max(0.0f, 1-d) );
          if ( c > maxTrack ) {
            maxTrack = c;
            xMaxTrack = ix;
            yMaxTrack = iy;
          }
        }
      }
    }

    final float delta = Env.TICK_TIME / kActivationTime;
    if ( max == 0) {
      mTrackingX = mTrackingY = -1;
      mHuntActive = Math.max(0.0f, mHuntActive-delta);
      return;
    }
    mHuntActive = Math.min(1.0f, mHuntActive+delta);
    
    if ( maxTrack > 0 && maxTrack < max/2 ) {
      if ( mTrackingReset > 0.0f ) {
        mTrackingReset -= Env.TICK_TIME;
        if ( mTrackingReset <= 0.0f ) {
          mTrackingReset = 0.0f;
          maxTrack = 0;
        }
      } else {
        mTrackingReset = kTrackingResetTime;
      }
    } else {
      mTrackingReset = 0.0f;
    }

    if ( maxTrack > 0 ) {
      mTrackingX = xMaxTrack + 0.5f;
      mTrackingY = yMaxTrack + 0.5f;
    } else {
      mTrackingX = xMax + 0.5f;
      mTrackingY = yMax + 0.5f;
    }

  } // trackEnemy()
  
  // advance the target position for the hunter
  private void updateTarget() {

    if ( mTrackingX < 0 || mTrackingY < 0 ) return;
    
    final float dx = mTrackingX - mTargetX,
                dy = mTrackingY - mTargetY;
    final float boost = ( mNoMercy ? 2.0f : 1.0f );
    final float step = kTargetSpeed*boost/Env.TICKS_PER_SEC;

    switch ( mTrackingMode ) {
      case kModeXOnly: {
        if ( step > Math.abs(dx) ) {
          mTargetX = mTrackingX;
        } else {
          mTargetX += step*( dx > 0 ? +1 : -1 );
        }        
      } break;

      case kModeYOnly: {
        if ( step > Math.abs(dy) ) {
          mTargetY = mTrackingY;
        } else {
          mTargetY += step*( dy > 0 ? +1 : -1 );
        }        
      } break;
      
      case kModeXYBoth: {
        final float d = (float)Math.sqrt(dx*dx + dy*dy);
        if ( step > d ) {
          mTargetX = mTrackingX;
          mTargetY = mTrackingY;
        } else {
          mTargetX += step*dx/d;
          mTargetY += step*dy/d;
        }
      } break;
      
      default: {
        assert(false);
      } break;
    }

    if ( mNoMercy ) {
      mTrackingMode = kModeXYBoth;
    } else {
      mModeTimer -= Env.TICK_TIME;
      if ( mModeTimer <= 0.0f ) {
        mTrackingMode = (mTrackingMode+Env.randomInt(kNumModes-1)+1)%kNumModes;
        mModeTimer = Env.randomFloat(kModeTimeMin, kModeTimeMax);
      }
    }
    
  } // updateTarget()
  
  // target-hunting contribution to the in-flow (between -1 and +1)
  private float inFlowHunt(float xTile, float yTile) {

    if ( mHuntStrength == 0.0 ) return 0.0f;
    
    if ( mTargetX < 0 || mTargetY < 0 ) return 0.0f;
    
    final float dx = xTile - mTargetX,
                dy = yTile - mTargetY;
    final float r2 = dx*dx + dy*dy;
    
    final float h = Math.min(1.0f, r2/(kTargetRadius*kTargetRadius));
    return (2.0f*h - 1.0f) * mHuntStrength;
    
  } // inFlowHunt()
  
  // random noise contribution to the in-flow (between -1 and +1)
  private float inFlowNoise(float xTile, float yTile) {

    if ( mNoiseStrength == 0.0 ) return 0.0f;
    
    final float x = xTile/Env.numTilesX(),
                y = yTile/Env.numTilesY(),
                z = kNoiseTimeScale*mNoiseTime;
    
    final float noise = mNoise.value(x, y, z)
                      + 0.5f*mNoise.value(2*x, 2*y, 2*z)
                      + 0.25f*mNoise.value(4*x, 4*y, 4*z);
    
    final float h = Math.max(-1.0f, Math.min(+1.0f, kNoiseCeiling*noise));
    return (mNoiseStrength * h);
    
  } // inFlowNoise()
  
} // class VentControl

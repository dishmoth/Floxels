/*
 *  Floxels.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// collection of flocking, flowing particles
public class Floxels extends Sprite {

  // story event: one of the floxel populations has been wiped out
  public static class EventPopulationDestroyed extends StoryEvent {
    public int mType;
    EventPopulationDestroyed(int type) { mType = type; }
  } // Floxels.EventPopulationDestroyed

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 50; 

  // maximum number of particles supported
  static private final int kNumFloxels = 1000;
  
  // details of how particles are launched or deleted
  static private final int kReleaseStep = 3,
                           kReclaimStep = 4;

  // time range between marking a floxel as reclaimed and it vanishing
  static private final float kMinReclaimTime = 0.5f,
                             kMaxReclaimTime = 1.0f;
  
  // lengths of time when stunned (game ticks)
  static private final int kStunTimeMax  = (int)(2.0f*Env.TICKS_PER_SEC),
                           kStunTimeMin  = (int)(1.0f*Env.TICKS_PER_SEC),
                           kStunTimeHalt = (int)(0.17f*Env.TICKS_PER_SEC),
                           kStunTimeWake = (int)(0.5f*Env.TICKS_PER_SEC);
  
  // default velocity scale for floxels
  static private final float kDefaultVelocityFactor = 3.0f;
  
  // maximum floxel speed
  static private final float kMaxSpeed = 3.0f;
  
  // if two floxels are on top of one another then try to separate them
  static private final float kNudgeDistance = 0.02f;
  
  // the refinement of the kill grid relative to the base grid
  static private final int kKillGridSubdivide = 6;

  // source terms controlling flocking behaviour
  static private final float kFlockAttractionStrength = 0.3f,
                             kFlockRepulsionStrength  = 3.0f;

  // source term reaction to a team-mate getting splatted  
  static private final float kSplatRepulsionStrength = 50.0f;
  
  // tweak the hunting/fleeing strengths for small clusters of floxels
  static private final int   kSmallClusterScore     = 6;
  static private final float kSmallClusterHuntBoost = 5.0f;
  
  // how frequently floxels blink and change expression
  static private final float kFaceChangeSeconds = 1.1f,
                             kBlinkTimeSeconds  = 0.17f,
                             kBlinkFraction     = 0.3f,
                             kSplatTime         = 0.2f;
  static private final int   kFaceChangeStep    = 10;
  
  // how floxels are summoned to the cursor
  static private final float kSummonSpeed = 70.0f;
  
  // the flow field
  private Flow mFlows[];

  // maximum number of different populations supported
  private final int mNumFloxelTypes;

  // different velocity scales for different floxel types
  private float mVelocityFactors[];
  
  // source terms controlling hunting and fleeing
  private float mHuntingStrengths[];  
  
  // size of each population
  private int mNumActiveFloxels[];
  
  // colours of the populations
  private int mTypeColours[];
  
  // size of the base grid
  private final int mGridXSize,
                    mGridYSize;
  
  // the particles
  private Floxel mFloxels[];

  // total number of floxels in each cell of the base grid
  private int mFloxelCounts[][][];
  
  // utility for determining how large clusters of floxels are
  private Clusters mClusters;

  // workspace for detecting collisions between different types of floxels
  private int mKillGrid[][];
  
  // spare velocity object 
  private Flow.Vel mVelObj = new Flow.Vel();
    
  // random modifications to the floxel faces 
  private int mFaceChangeIndex,
              mFaceChangeTailIndex;

  // capture point to which floxels are pulled (unless type is -1)
  private int   mPullType;
  private float mPullXPos,
                mPullYPos,
                mPullRadius;
  
  // call all player floxels to the pull position
  private boolean mSummonFloxels;
  
  // constructor
  public Floxels(Flow flows[]) {
    
    super(kScreenLayer);
    
    mFlows = flows;

    mGridXSize = mFlows[0].baseXSize();
    mGridYSize = mFlows[0].baseYSize();
    
    mFloxels = new Floxel[kNumFloxels];
    for ( int k = 0 ; k < kNumFloxels ; k++ ) {
      if ( mFloxels[k] == null ) mFloxels[k] = new Floxel();
    }

    mNumFloxelTypes = mFlows.length;

    mVelocityFactors = new float[mNumFloxelTypes];
    Arrays.fill(mVelocityFactors, kDefaultVelocityFactor);
    
    mHuntingStrengths = new float[mNumFloxelTypes];
    Arrays.fill(mHuntingStrengths, 0.0f);
    
    mNumActiveFloxels = new int[mNumFloxelTypes];
    Arrays.fill(mNumActiveFloxels, 0);
    
    mTypeColours = new int[mNumFloxelTypes];
    Arrays.fill(mTypeColours, 0);
    
    mFloxelCounts = new int[mNumFloxelTypes][mGridYSize][mGridXSize];
    
    mClusters = new Clusters(mFlows[0], 2, kNumFloxels);
    
    mKillGrid = new int[mGridYSize*kKillGridSubdivide+1]
                       [mGridXSize*kKillGridSubdivide+1];

    mFaceChangeIndex = 0;
    mFaceChangeTailIndex = -Math.round( kNumFloxels * kBlinkTimeSeconds 
                                        / kFaceChangeSeconds );

    mPullType = -1;
    mPullXPos = mPullYPos = mPullRadius = 0.0f;
    mSummonFloxels = false;
    
  } // constructor

  // the total number of floxels supported
  public int maxFloxels() { return kNumFloxels; }

  // current number of floxels of a particular type
  public int numFloxels(int type) { 
    
    assert( type >= 0 && type < mNumFloxelTypes );
    return mNumActiveFloxels[type]; 
    
  } // numFloxels()

  // colours of the different floxel types 
  public int floxelColour(int type) {
    
    assert( type >= 0 && type < mTypeColours.length );
    return mTypeColours[type]; 
    
  } // floxelColour()
  
  // define the colour for a floxel type 
  public void setFloxelColour(int type, int colour) {
    
    assert( type >= 0 && type < mTypeColours.length );
    assert( colour >= 0 && colour < ColourScheme.num() );
    mTypeColours[type] = colour;
    
  } // setFloxelColour()
  
  // access (read-only) to the floxel count per grid square
  public int[][] countFloxels(int type) { 
    
    assert( type >= 0 && type < mNumFloxelTypes );
    return mFloxelCounts[type]; 
    
  } // countFloxels()

  // set the velocity factor for a population
  public void setVelocityFactor(int type, float vel) {
    
    assert( type >= 0 && type < mNumFloxelTypes );
    assert( vel >= 0.0f );
    mVelocityFactors[type] = vel;
    
  } // setVelocityFactor()
  
  // set the hunt/flee factor for a population
  public void setHuntingStrength(int type, float val) {
    
    assert( type >= 0 && type < mNumFloxelTypes );
    mHuntingStrengths[type] = val;
    
  } // setHuntingStrength()
  
  // choose a random position for inactive particles
  public void releaseFloxels(int type, int num, 
                             float x, float y, float radius) {

    assert( type >= 0 && type < mNumFloxelTypes );
    assert( num > 0 );
    assert( x >= 0 && x <= mGridXSize );
    assert( y >= 0 && y <= mGridYSize );
    assert( radius >= 0 );
    
    int numActive = 0;
    for ( int n : mNumActiveFloxels ) numActive += n;
    assert( numActive + num <= kNumFloxels );

    final float margin = 0.001f;
    radius = Math.max(radius, 2*margin); 
    
    final float dx0 = Math.max(-radius, margin-x),
                dx1 = Math.min(+radius, mGridXSize-margin-x),
                dy0 = Math.max(-radius, margin-y),
                dy1 = Math.min(+radius, mGridYSize-margin-y);
    assert( dx1 > dx0 && dy1 > dy0 );
    
    int index = Env.randomInt(kNumFloxels);
    while ( num > 0 ) {
      while ( mFloxels[index].mState != Floxel.State.UNUSED ) {
        index += kReleaseStep;
        if ( index >= kNumFloxels ) {
          index = (index+1) % kReleaseStep;
        }
      }
      
      float dx, dy;
      do {
        dx = radius*Env.randomFloat(-1.0f, +1.0f); 
        dy = radius*Env.randomFloat(-1.0f, +1.0f);
      } while ( dx*dx + dy*dy > radius*radius || 
                dx < dx0 || dx > dx1 || 
                dy < dy0 || dy > dy1 );
      
      Floxel floxel = mFloxels[index];
      floxel.mState = Floxel.State.NORMAL;
      floxel.mX = x + dx; 
      floxel.mY = y + dy;
      floxel.mTimer = 0;
      floxel.mCluster = (byte)Env.randomInt( Clusters.maxClusterScore()+1 );
      floxel.mNeedsNudge = false;
      floxel.mType = (byte)type;
      floxel.mShade = (byte)Env.randomInt( Floxel.NUM_SHADES );
      floxel.mFace = (byte)Env.randomInt( Floxel.NUM_EXPRESSIONS );
      
      mNumActiveFloxels[type]++;
      num--;
    }
    
  } // releaseFloxels()

  // gradually remove a number of floxels without attracting attention
  public void reclaimFloxels(int num, int type) {
    
    assert(false); // not currently called by anything
    
    assert( type >= 0 && type < mNumFloxelTypes );
    assert( mNumActiveFloxels[type] >= num );

    final int minTime = Math.round(kMinReclaimTime * Env.TICKS_PER_SEC),
              maxTime = Math.round(kMaxReclaimTime * Env.TICKS_PER_SEC);
    
    int minClusterScore = 10;
    int numSteps = 0;
    
    int index = Env.randomInt(kNumFloxels);
    while ( num > 0 ) {
      while ( ( mFloxels[index].mState != Floxel.State.NORMAL &&
                mFloxels[index].mState != Floxel.State.STUNNED ) ||
              mFloxels[index].mType != type ||
              mFloxels[index].mCluster < minClusterScore ) {
        index += kReclaimStep;
        if ( index >= kNumFloxels ) {
          index = (index+1) % kReclaimStep;
        }
        if ( ++numSteps == kNumFloxels ) minClusterScore = -100;
        assert( numSteps <= 2*kNumFloxels );
      }      

      mFloxels[index].mState = Floxel.State.RECLAIMED;
      mFloxels[index].mTimer = (byte)Env.randomInt(minTime, maxTime);
      num--;
    }
    
  } // reclaimFloxels()
  
  // temporarily disable all floxels in an annular region
  public void stunFloxels(float x, float y, 
                          float radiusMin, float radiusMax,
                          int type) {

    assert( radiusMin >= 0.0f && radiusMax > radiusMin );
    assert( type >= -1 && type < mNumFloxelTypes );
    
    final float r2A = radiusMin*radiusMin,
                r2B = radiusMax*radiusMax;

    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState != Floxel.State.NORMAL &&
           floxel.mState != Floxel.State.SPLATTED &&
           floxel.mState != Floxel.State.STUNNED ) continue;
      if ( type >= 0 && floxel.mType != type ) continue;
      final float dx = floxel.mX - x,
                  dy = floxel.mY - y;
      final float d2 = dx*dx + dy*dy;
      if ( d2 >= r2A && d2 < r2B ) {
        floxel.mState = Floxel.State.STUNNED;
        floxel.mTimer = (short)kStunTimeMax;
        floxel.mCluster = 0;
        floxel.mFace = (byte)Floxel.STUN_FACE;
      }
    }
    
  } // stunFloxels()
  
  // temporarily disable all floxels in a circular region
  public void stunFloxels(float x, float y, float radius) {

    stunFloxels(x, y, 0.0f, radius, -1);
    
  } // stunFloxels()

  // pull floxels to the cursor position and capture them
  public int captureFloxels(float x, float y, 
                            float pullRadius, float captureRadius,
                            int type) {
    
    assert( x >= 0.0f && x < mGridXSize );
    assert( y >= 0.0f && y < mGridYSize );
    assert( pullRadius >= captureRadius && captureRadius > 0.0f );

    mPullType = type;
    mPullXPos = x;
    mPullYPos = y;
    mPullRadius = pullRadius;
    
    int numCaptured = 0;
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState != Floxel.State.NORMAL &&
           floxel.mState != Floxel.State.STUNNED ) continue;
      if ( type >= 0 && floxel.mType != type ) continue;
      final float dx = floxel.mX - x,
                  dy = floxel.mY - y;
      if ( dx*dx + dy*dy < captureRadius*captureRadius ) {
        floxel.mState = Floxel.State.UNUSED;
        numCaptured += 1;
        mNumActiveFloxels[type] -= 1;
      }
    }
    return numCaptured;
    
  } // captureFloxels()
  
  // convert some of the majority population and call them to the cursor
  // (move them to the end of the list so they are drawn over everything else)
  public void summonFloxels(int num, int type) {
    
    final int otherType = 1-type;
    
    assert( num > 0 );
    assert( num <= mNumActiveFloxels[otherType] );
    
    final int splatTime = Math.round( Env.TICKS_PER_SEC*kSplatTime );

    final int step = Env.randomInt(1,100);
    int offset = 0;
    
    int index = offset;
    int endIndex = kNumFloxels;

    int numGot = 0;
    while ( numGot < num ) {
      Floxel floxel = mFloxels[index];
      if ( floxel.mState != Floxel.State.UNUSED && 
           floxel.mType == otherType ) {
        floxel.mState = Floxel.State.SPLATTED;
        floxel.mTimer = (byte)splatTime;
        floxel.mFace = (byte)Floxel.SPLAT_FACE;
        floxel.mCluster = (byte)Clusters.maxClusterScore();
        floxel.mNeedsNudge = false;
        floxel.mType = (byte)type;
        floxel.mShade = (byte)0;
        
        do {
          endIndex -= 1;
          assert( endIndex >= 0 );
        } while ( mFloxels[endIndex].mState != Floxel.State.UNUSED &&
                  mFloxels[endIndex].mType == (byte)type &&
                  endIndex > index );
        
        mFloxels[index] = mFloxels[endIndex];
        mFloxels[endIndex] = floxel;

        numGot += 1;
      } else {
        index += step;
      }
      
      if ( index >= endIndex ) {
        offset += 1;
        assert( offset < step );
        index = offset;
      }
    }
    
    mNumActiveFloxels[type] += num;
    mNumActiveFloxels[otherType] -= num;
    
    mSummonFloxels = true;
    
  } // summonFloxels()
  
  // advance by one frame
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    int oldNumFloxels[] = mNumActiveFloxels.clone();
    
    if ( mSummonFloxels ) {
      if ( mPullType == -1 ) {
        for ( Floxel floxel : mFloxels ) {
          if ( floxel.mState == Floxel.State.NORMAL ) floxel.mTimer = 0;
        }
        mSummonFloxels = false;
      }
    }
    
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ) continue;
      advanceFloxel(floxel);
    }

    fightFloxels();
    countFloxels();
    setDesiredSolutionLevels();
    makeClusters();
    updateFaces();

    for ( int type = 0 ; type < mNumFloxelTypes ; type++ ) {
      if ( mNumActiveFloxels[type] == 0 && 
           oldNumFloxels[type] > 0 ) {
        newStoryEvents.add( new EventPopulationDestroyed(type) );
      }
    }

    mPullType = -1;
    
  } // Sprite.advance()

  // update position for a floxel
  private void advanceFloxel(Floxel floxel) {

    final float dt = Env.TICK_TIME;
    
    final int type = floxel.mType;

    int flowType = type;
    if ( mSummonFloxels && type == mPullType ) flowType = 1-mPullType;
    Flow flow = mFlows[flowType];
    
    // special behaviour for certain floxel states

    float slowdown = 1.0f;
    switch ( floxel.mState ) {
      case UNUSED: {
        assert(false);
      } break;
            
      case SPLATTED: {
        assert( floxel.mTimer > 0 );
        if ( --floxel.mTimer == 0 ) {
          floxel.mState = Floxel.State.NORMAL;
          floxel.mFace = (byte)Env.randomInt(Floxel.NUM_EXPRESSIONS);
        }
      } break;
      
      case STUNNED: {
        assert( floxel.mTimer > 0 );
        floxel.mTimer--;
        if ( floxel.mTimer == 0 ) {
          floxel.mState = Floxel.State.NORMAL;
        } else if ( floxel.mTimer < kStunTimeWake ) {
          slowdown = 1.0f - floxel.mTimer/(float)kStunTimeWake;
        } else if ( floxel.mTimer == kStunTimeWake ) {
          floxel.mFace = (byte)Env.randomInt(Floxel.NUM_EXPRESSIONS);
          slowdown = 0.0f;
        } else if ( floxel.mTimer < kStunTimeMax-kStunTimeHalt ) {
          slowdown = 0.0f;
        } else if ( floxel.mTimer == kStunTimeMax-kStunTimeHalt ) {
          floxel.mTimer -= Env.randomInt(kStunTimeMax-kStunTimeMin);
          slowdown = 0.0f;
          assert( floxel.mTimer > kStunTimeWake );
        } else {
          final int t0 = kStunTimeMax - kStunTimeHalt;
          slowdown = (floxel.mTimer - t0)/(float)(kStunTimeMax - t0 - 1);
        }
        assert( slowdown >= 0.0f && slowdown <= 1.0f );
      } break;
      
      case RECLAIMED: {
        assert( floxel.mTimer > 0 );
        if ( --floxel.mTimer == 0 ) {
          floxel.mState = Floxel.State.UNUSED;
          mNumActiveFloxels[type]--;
          return;
        }
      } break;

      case NORMAL: {
        if ( !mSummonFloxels ) assert( floxel.mTimer == 0 );
      } break;
    }

    if ( slowdown == 0.0f ) return;
    
    // determine the floxel's velocity
    
    flow.getVelocity(floxel.mX, floxel.mY, mVelObj);

    float velocityFactor = mVelocityFactors[type] * slowdown;
    float vx = velocityFactor * mVelObj.x,
          vy = velocityFactor * mVelObj.y;
    vx = Math.max(-kMaxSpeed, Math.min(+kMaxSpeed, vx));
    vy = Math.max(-kMaxSpeed, Math.min(+kMaxSpeed, vy));

    float dx = vx*dt,
          dy = vy*dt;

    // don't let floxels pile up on each other
    
    if ( floxel.mNeedsNudge ) {
      dx += kNudgeDistance*( Env.randomBoolean() ? +1 : -1 );
      dy += kNudgeDistance*( Env.randomBoolean() ? +1 : -1 );
    }

    // check whether the floxel is being pulled to the cursor
    
    boolean ignoreWalls = false;
    
    if ( floxel.mType == mPullType ) {
      float px = mPullXPos - floxel.mX,
            py = mPullYPos - floxel.mY;
      float p2 = px*px + py*py;
      if ( mSummonFloxels || p2 <= mPullRadius*mPullRadius ) {
        float speed = kMaxSpeed;
        if ( mSummonFloxels ) {
          if ( floxel.mState == Floxel.State.SPLATTED ) {
            speed = 0.0f;
          } else {
            floxel.mTimer += 1;
            float summonDt = floxel.mTimer*Env.TICK_TIME; 
            speed = summonDt*summonDt*kSummonSpeed;
          }
        }
        float step = speed*dt;
        if ( p2 < step*step ) {
          dx = px;
          dy = py;
        } else {
          float p = (float)Math.sqrt(p2);
          dx = step*px/p;
          dy = step*py/p;
        }
        ignoreWalls = true;
      }
    }
    
    // update the floxel's position, avoiding maze walls
    
    float x = floxel.mX,
          y = floxel.mY;
    float walls[] = flow.walls()[(int)y][(int)x];

    if ( ignoreWalls || 
         ((int)(x+dx+1)-1) == (int)x ||
         ( dx < 0 && walls[Env.WEST] == Flow.OPEN ) ||
         ( dx > 0 && walls[Env.EAST] == Flow.OPEN ) ) {
      x += dx;
      walls = flow.walls()[(int)y][(int)x];
    }
    
    if ( ignoreWalls ||
         ((int)(y+dy+1)-1) == (int)y ||
         ( dy < 0 && walls[Env.NORTH] == Flow.OPEN ) ||
         ( dy > 0 && walls[Env.SOUTH] == Flow.OPEN ) ) {
      y += dy;
    }
    
    assert( x >= 0 && x < mGridXSize );
    assert( y >= 0 && y < mGridYSize );
    
    floxel.mX = x;
    floxel.mY = y;
    
  } // advanceFloxel()

  // convert floxels if they collide with stronger ones of the other type
  private void fightFloxels() {
    
    if ( mSummonFloxels ) return;
    
    assert( mNumFloxelTypes == 2 );
    
    for ( int ky = 0 ; ky < mKillGrid.length ; ky++ ) {
      Arrays.fill(mKillGrid[ky], 0);
    }

    final float offsetX = 0.99f*Env.randomFloat(),
                offsetY = 0.99f*Env.randomFloat();
    
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState != Floxel.State.NORMAL ) continue;
      final int kx = (int)(floxel.mX*kKillGridSubdivide + offsetX),
                ky = (int)(floxel.mY*kKillGridSubdivide + offsetY);
      final int strength = floxel.mCluster + 1;
      if ( strength > Math.abs(mKillGrid[ky][kx]) ) {
        mKillGrid[ky][kx] = ( floxel.mType == 0 ? +strength : -strength );
      }
    }
    
    final int splatTime = Math.round( Env.TICKS_PER_SEC*kSplatTime );
    
    int killCount[] = new int[mNumFloxelTypes];
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState != Floxel.State.NORMAL &&
           floxel.mState != Floxel.State.STUNNED ) continue;
      final int kx = (int)(floxel.mX*kKillGridSubdivide + offsetX),
                ky = (int)(floxel.mY*kKillGridSubdivide + offsetY);
      final int attackStrength = Math.abs(mKillGrid[ky][kx]) - 1,
                attackType     = ( (mKillGrid[ky][kx] > 0) ? 0 : 1 );
      if ( floxel.mType != attackType && floxel.mCluster <= attackStrength ) {
        floxel.mState = Floxel.State.SPLATTED;
        floxel.mTimer = (byte)splatTime;
        floxel.mCluster = 0;
        floxel.mType = (byte)attackType;
        floxel.mShade = 0;
        floxel.mFace = (byte)Floxel.SPLAT_FACE;
        killCount[1-attackType] += 1;
      }
    }
    Env.sounds().playDeathSounds(killCount);
  
  } // fightFloxels()
  
  // census of the floxel populations
  private void countFloxels() {
    
    for ( int type = 0 ; type < mNumFloxelTypes ; type++ ) {
      for ( int ky = 0 ; ky < mGridYSize ; ky++ ) {
        Arrays.fill(mFloxelCounts[type][ky], 0);
      }
      mNumActiveFloxels[type] = 0;
    }
    
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ) continue;
      final int kx = (int)floxel.mX,
                ky = (int)floxel.mY;
      mFloxelCounts[floxel.mType][ky][kx] += 1;
      mNumActiveFloxels[floxel.mType] += 1;
    }
    
  } // countFloxels()
  
  // we only need a high-quality solution in the blocks where the floxels are
  public void setDesiredSolutionLevels() {

    final int desiredLevel = mFlows[0].refineLevel();
    
    for ( int type = 0 ; type < mNumFloxelTypes ; type++ ) {
      mFlows[type].resetDesiredSolutionLevel(0);
      
      for ( int ky = 0 ; ky < mGridYSize ; ky++ ) {
        for ( int kx = 0 ; kx < mGridXSize ; kx++ ) {
          if ( mFloxelCounts[type][ky][kx] > 0 ) {
            mFlows[type].setDesiredSolutionLevel(kx, ky, desiredLevel);
          }
        }
      }
    }
    
  } // setDesiredSolutionLevels()
  
  // assign a cluster size rating to each floxel, and update its shade
  private void makeClusters() {
    
    for ( int type = 0 ; type < mNumFloxelTypes ; type++ ) {
    
      mClusters.reset();
  
      for ( Floxel floxel : mFloxels ) {
        if ( floxel.mState != Floxel.State.NORMAL || 
             floxel.mType != type ) continue;
        mClusters.addPoint(floxel.mX, floxel.mY);
      }
    
      mClusters.makeClusters();
    
      for ( Floxel floxel : mFloxels ) {
        if ( floxel.mState == Floxel.State.UNUSED ||
             floxel.mType != type ) continue;

        if ( floxel.mState == Floxel.State.NORMAL ) { 
          final int n = mClusters.getClusterScore(floxel.mX, floxel.mY);
          if      ( n > floxel.mCluster ) floxel.mCluster++;
          else if ( n < floxel.mCluster ) floxel.mCluster--;
        }
        
        floxel.mShade = (byte)( (floxel.mCluster * (Floxel.NUM_SHADES-1))
                                / Clusters.maxClusterScore() );
      }
      
    }

  } // makeClusters()
  
  // animate the faces of the floxels in a random-ish way
  private void updateFaces() {
    
    final int numFaces  = Floxel.NUM_EXPRESSIONS,
              blinkFace = Floxel.BLINK_FACE;
    
    int newFace = Env.randomInt(numFaces);
    float blink = Env.randomFloat() + kBlinkFraction;

    final int numChanges = Math.round( (kNumFloxels/kFaceChangeSeconds)
                                       / Env.TICKS_PER_SEC );

    // change the expressions of some faces, starting some blinking
    for ( int k = 0 ; k < numChanges ; k++ ) {
      Floxel floxel = mFloxels[mFaceChangeIndex];
      if ( floxel.mState == Floxel.State.NORMAL ) {
        assert( floxel.mFace != Floxel.SPLAT_FACE && 
                floxel.mFace != Floxel.STUN_FACE );
        if ( blink >= 1.0f ) {
          floxel.mFace = (byte)blinkFace;
          blink -= 1.0f;
        } else {
          floxel.mFace = (byte)newFace;
          newFace = (newFace+1) % numFaces;
        }
        blink += kBlinkFraction;
      }
      mFaceChangeIndex += kFaceChangeStep;
      if ( mFaceChangeIndex >= kNumFloxels ) {
        mFaceChangeIndex = (mFaceChangeIndex+1) % kFaceChangeStep;
      }
    }

    // convert blinks back to ordinary faces after a while
    for ( int k = 0 ; k < numChanges ; k++ ) {
      if ( mFaceChangeTailIndex < 0 ) {
        mFaceChangeTailIndex++;
      } else {
        Floxel floxel = mFloxels[mFaceChangeTailIndex];
        if ( floxel.mState == Floxel.State.NORMAL && 
             floxel.mFace == blinkFace ) {
          floxel.mFace = (byte)newFace;
          newFace = (newFace+1) % numFaces;
        }
        mFaceChangeTailIndex += kFaceChangeStep;
        if ( mFaceChangeTailIndex >= kNumFloxels ) {
          mFaceChangeTailIndex = (mFaceChangeTailIndex+1) % kFaceChangeStep;
        }
      }
    }

  } // updateFaces()

  // add source terms to make the floxels flock together
  public void defineFlockingSources() {
    
    final int refinement = mFlows[0].refineFactor();

    // set all source terms to zero initially
    for ( int type = 0 ; type < mFlows.length ; type++ ) {
      mFlows[type].clearSource();
    }

    // attractive terms are applied in the general vicinity of the floxels 
    final int subdivide = 2,
              subSize   = refinement/subdivide;
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ) continue;
      float source[][] = mFlows[floxel.mType].source();
    
      final int kx = subSize*(int)(floxel.mX*subdivide),
                ky = subSize*(int)(floxel.mY*subdivide);

      if ( source[ky][kx] == 0.0f ) {
        for ( int dy = 0 ; dy < subSize ; dy++ ) {
          for ( int dx = 0 ; dx < subSize ; dx++ ) {
            source[ky+dy][kx+dx] = -kFlockAttractionStrength;
          }
        }
      }
    }
    
    // repulsive terms are applied at the precise positions of the floxels
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ) continue;
      float source[][] = mFlows[floxel.mType].source();

      final int kx = (int)(floxel.mX*refinement),
                ky = (int)(floxel.mY*refinement);

      if ( floxel.mState == Floxel.State.RECLAIMED ) {
        source[ky][kx] -= kFlockRepulsionStrength;
      } else {
        floxel.mNeedsNudge = (source[ky][kx] > 0.0f);
        source[ky][kx] += kFlockRepulsionStrength;
      }
    }
    
    //int numCoincidences = 0;
    //for ( Floxel floxel : mFloxels ) {
    //  if ( floxel.mActive && floxel.mNeedsNudge ) numCoincidences+=1;
    //}
    //System.out.println(numCoincidences);
    
  } // defineFlockingSources()
  
  // contributions to the source terms to effect hunting and fleeing  
  public void addHuntingSources() {
    
    final int refinement = mFlows[0].refineFactor();

    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ) continue;

      final int kx = (int)(floxel.mX * refinement),
                ky = (int)(floxel.mY * refinement);

      float strength = mHuntingStrengths[floxel.mType];
      if ( floxel.mState == Floxel.State.SPLATTED ) {
        strength = Math.max(strength, kSplatRepulsionStrength);
      } else if ( floxel.mCluster < kSmallClusterScore ) {
        final float f = floxel.mCluster/(float)kSmallClusterScore; 
        if ( strength < 0.0f ) {
          strength *= 1.0f + kSmallClusterHuntBoost*(1.0f - f);
        } else {
          strength *= f;
        }
      }

      final int otherType = 1 - floxel.mType;
      mFlows[otherType].source()[ky][kx] += strength;
    }
    
  } // addHuntingSources()

  // interchange the types of the floxels
  public void switchFloxelTypes() {
    
    assert( mNumFloxelTypes == 2 );
    
    for ( Floxel floxel : mFloxels ) {
      floxel.mType = (byte)(1 - floxel.mType);
    }
    
    int numTemp = mNumActiveFloxels[0];
    mNumActiveFloxels[0] = mNumActiveFloxels[1];
    mNumActiveFloxels[1] = numTemp;
    
    int countTemp[][] = mFloxelCounts[0];
    mFloxelCounts[0] = mFloxelCounts[1];
    mFloxelCounts[1] = countTemp;

    float huntTemp = mHuntingStrengths[0];
    mHuntingStrengths[0] = mHuntingStrengths[1];
    mHuntingStrengths[1] = huntTemp;
    
    float velTemp = mVelocityFactors[0];
    mVelocityFactors[0] = mVelocityFactors[1];
    mVelocityFactors[1] = velTemp;
    
    Flow flowTemp = mFlows[0];
    mFlows[0] = mFlows[1];
    mFlows[1] = flowTemp;
    
  } // switchFloxelTypes()
  
  // display the floxels
  @Override
  public void draw(SpriteBatch batch) {
    
    FloxelPainter painter = Env.painter().floxelPainter();
    
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState == Floxel.State.UNUSED ||
           floxel.mState == Floxel.State.SPLATTED ) continue;
      painter.draw(batch, floxel, mTypeColours[floxel.mType]);
    }
    
    for ( Floxel floxel : mFloxels ) {
      if ( floxel.mState != Floxel.State.SPLATTED ) continue;
      painter.draw(batch, floxel, mTypeColours[floxel.mType]);
    }
    
  } // Sprite.draw()
  
} // class Floxels

/*
 *  FloxelsStory.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.*;

// controlling class for the game
public class FloxelsStory extends Story {
  
  // enumeration of population types
  static private final int kMajorityType = 0,
                           kMinorityType = 1;
  static private final int kNumTypes     = 2;

  // how many floxels of each type we're dealing with
  static private final int kMajorityPopulationMax = 900,
                           kMinorityPopulation    = 100;

  // factors controlling majority floxels' instincts for hunting
  static private final float kHuntStrength      = 10.0f,
                             kHuntStrengthBoost = 8.0f,
                             kHuntStrengthFail  = -0.3f,
                             kHuntFailureFrac   = 0.7f;
  
  // factors controlling minority floxels' instincts for fleeing
  static private final float kFleeStrength         = 10.0f,
                             kFleeStrengthReverse  = -5.0f,
                             kFleeReverseFrac      = 0.7f,
                             kFleeReverseSoundFrac = 0.77f,
                             kFleeStrengthResign   = 0.5f;
  static private final int   kFleeResignNum        = 10;

  // seconds until a new bouncer is added
  static private final float kBouncerDelayMin = 5.0f,
                             kBouncerDelayMax = 20.0f;

  // special behaviour when a BlastMega is triggered (seconds)
  static private final float kMegaBlastDuration = 1.5f,
                             kMegaBlastAttack   = 3.0f;
  
  // seconds until various events occur
  static private final float kIntroDelay       = 0.9f,
                             kRestartDelay     = 1.5f,
                             kRestartLongDelay = 2.0f,
                             kStartleDelay     = 1.0f,
                             kChangeDelay      = 0.015f,
                             kChangeFirstDelay = 2.0f;
  
  // references to some specific objects
  private Background  mBackground;
  private Flow        mFlows[];
  private Floxels     mFloxels;
  private Maze        mMaze;
  private VentControl mVentControls[];
  private Cursor      mCursor;
  private Score       mScore;
  
  // player's current level (0, 1, 2, ...)
  private int mLevel;
  
  // index of the current maze
  private int mMazeNum;

  // when changing the maze, these are the changes remaining to be made
  private LinkedList<Maze.Delta> mMazeDeltas = new LinkedList<Maze.Delta>();
  
  // number of floxels at the start of the level
  private int mMajorityPopulation;
  
  // tweak to the aggression of the majority floxels (1.0 for no change)
  private float mDifficultyFactor;

  // maximum number of bouncers active at one time
  private int mMaxNumBouncers;

  // seconds until the first floxels appear (or zero)
  private float mIntroTimer;

  // seconds until new floxels can be introduced (or zero)
  private float mRestartTimer;

  // seconds until surprised floxels start to attack
  private float mStartleTimer;

  // seconds until the next maze alteration
  private float mChangeTimer;
  
  // seconds until a new bouncer appears (or zero)
  private float mNewBouncerTimer;

  // seconds until the mega blast effect ends
  private float mMegaBlastTimer;

  // true if the minority is suddenly on the offensive
  private boolean mReversed;
  
  // true if the maze is about to change
  private boolean mChanging;
  
  // prepare resources
  static public void initialize() {

    FloxelPainter.initialize();
    
  } // initialize()

  // constructor
  public FloxelsStory() {

    initialize();

    mLevel = -1;
    mMazeNum = -1;

  } // constructor

  // advance the Story by one frame
  public Story advance(LinkedList<StoryEvent> storyEvents,
                       SpriteManager          spriteManager) {

    // process the story event list
    for ( Iterator<StoryEvent> it = storyEvents.iterator() ; it.hasNext() ; ) {
      StoryEvent event = it.next();

      if ( event instanceof Story.EventGameBegins ) {
        mLevel = 0;
        mMazeNum = 0;
        prepareNewStory(spriteManager);
        it.remove();
      } // Story.EventGameBegins

      //if ( event instanceof LaunchCursor.EventLaunchComplete ) {
      //  mStartleTimer = kStartleDelay;
      //  mCursor = new SummonCursor(mMaze, mFloxels);
      //  spriteManager.addSprite(mCursor);
      //  it.remove();
      //} // LaunchCursor.EventLaunchComplete

      //if ( event instanceof SummonCursor.EventCaptureComplete ) {
      //  mCursor = new LaunchCursor(mFloxels.numReserveFloxels(kMinorityType),
      //                             kMinorityType, mFloxels);
      //  spriteManager.addSprite(mCursor);
      //  it.remove();
      //} // LaunchCursor.EventLaunchComplete

      if ( event instanceof Floxels.EventPopulationDestroyed ) {
        final int type = ((Floxels.EventPopulationDestroyed)event).mType;
        if ( type == kMajorityType ) {
          newLevel(spriteManager);
        } else if ( type == kMinorityType && mCursor.numCaptured() == 0 ) {
          restartLevel(spriteManager);
        }
        it.remove();
      } // Floxels.EventPopulationDestroyed

      if ( event instanceof BlastMega.EventUnleashed ) {
        mMegaBlastTimer = kMegaBlastDuration;
        it.remove();
      } // BlastMega.EventUnleashed
      
    } // for each story event

    updateHuntFactors();
    checkSummoners(spriteManager);
    updateFlows(spriteManager);
    addBouncers(spriteManager);
    
    if ( mRestartTimer > 0.0f && mChangeTimer == 0.0f ) {
      mRestartTimer -= Env.TICK_TIME;
      if ( mRestartTimer <= 0.0f ) {
        assert( mFloxels.numFloxels(kMajorityType) == mMajorityPopulation );
        assert( mCursor == null );
        //mCursor = new LaunchCursor(kMinorityPopulation, 
        //                           kMinorityType, mFloxels);
        mCursor = new Cursor(kMinorityPopulation, kMinorityType, mFloxels);
        spriteManager.addSprite(mCursor);
        mRestartTimer = 0.0f;
      }
    }

    if ( mChangeTimer > 0.0f ) {
      mChangeTimer -= Env.TICK_TIME;
      boolean changed = false;
      while ( mChangeTimer <= 0.0f ) {
        mMaze.applyDifference(mMazeDeltas.pop());
        changed = true;
        if ( mMazeDeltas.isEmpty() ) {
          mChangeTimer = 0.0f;
          break;
        } else {
          mChangeTimer += kChangeDelay;
        }
      }
      if ( changed ) {
        if ( mChanging ) {
          Env.sounds().play(Sounds.MAZE_MORPH);
          mChanging = false;
        }
        //mBackground.updateImage();
        for ( Flow flow : mFlows ) prepareFlow(flow, mMaze);
      }
    }
    
    if ( mIntroTimer > 0.0f ) {
      mIntroTimer -= Env.TICK_TIME;
      if ( mIntroTimer <= 0.0f ) {
        mFloxels.releaseFloxels(0, mMajorityPopulation, 5.0f, 5.0f, 0.1f);
        mRestartTimer = kRestartDelay;
        mIntroTimer = 0.0f;
        Env.sounds().play(Sounds.UNLEASH_BIG);
      }
    }

    mScore.setCurrentValue(mFloxels.numFloxels(kMinorityType));
    
    // no change of story
    return null;

  } // advance()

  // balance the aggressive tendencies of the floxels based on their numbers
  private void updateHuntFactors() {
    
    final int majNum = mFloxels.numFloxels(kMajorityType),
              minNum = mFloxels.numFloxels(kMinorityType);
    
    final int total  = majNum + minNum;
    if ( total == 0 ) return;
    
    final float minFrac = minNum/(float)total;
    final float dt = Env.TICK_TIME;
    
    // how tasty the minority population looks
    float huntStrength = kHuntStrength * mDifficultyFactor;
    if ( minNum == 0 ) {
      huntStrength = 0.0f;
    } else if ( mStartleTimer > 0.0f ) {
      final float h = mStartleTimer/kStartleDelay;
      huntStrength = -10.0f*h + (1-h)*huntStrength;
      mStartleTimer = Math.max(0.0f, mStartleTimer-dt);
    } else if ( minNum > 0 && minNum < kMinorityPopulation ) {
      final float h = minNum/(float)kMinorityPopulation;
      assert( h >= 0.0f && h <= 1.0f );
      final float boost = (1-h)*(1-h);
      huntStrength += boost*kHuntStrengthBoost*kHuntStrength;
    } else if ( minFrac > kHuntFailureFrac ) {
      final float h = (minFrac - kHuntFailureFrac)/(1.0f - kHuntFailureFrac);
      assert( h >= 0.0f && h <= 1.0f );
      huntStrength *= (1-h) + h*kHuntStrengthFail;
    }
    mFloxels.setHuntingStrength(kMinorityType, -huntStrength);
    
    // how scary the majority population looks
    float fleeStrength = kFleeStrength; 
    if ( minFrac > kFleeReverseFrac ) {
      final float h = (minFrac - kFleeReverseFrac)/(1.0f - kFleeReverseFrac);
      assert( h >= 0.0f && h <= 1.0f );
      fleeStrength *= (1-h) + h*kFleeStrengthReverse;
    } else if ( minNum < kFleeResignNum ) {
      final float h = (minNum-1)/(float)(kFleeResignNum-1);
      fleeStrength *= h + (1-h)*kFleeStrengthResign;
    }
    if ( mMegaBlastTimer > 0.0f ) {
      final float h = Math.abs(2*mMegaBlastTimer/kMegaBlastDuration-1);
      assert( h >= 0.0f && h <= 1.0f );
      final float f = 1.0f - h*h;
      final float str = (1-f)*kFleeStrength + f*(-kMegaBlastAttack);
      if ( str < fleeStrength ) fleeStrength = str;
      mMegaBlastTimer = Math.max(0.0f, mMegaBlastTimer-dt);
    }
    mFloxels.setHuntingStrength(kMajorityType, +fleeStrength);

    if ( minFrac > kFleeReverseSoundFrac && !mReversed ) {
      Env.sounds().play(Sounds.REVERSAL);
      mReversed = true;
    } else if ( minFrac < 0.75f*kFleeReverseSoundFrac ) {
      mReversed = false;
    }
    
    // vents influence large-scale movement
    final float v = Math.max(0.0f, Math.min(1.0f, (minFrac - 0.6f)/0.1f));
    mVentControls[kMajorityType].setHuntStrength(1.0f - v);
    mVentControls[kMinorityType].setHuntStrength(v);

    mVentControls[kMajorityType].setNoMercy( minNum < kMinorityPopulation/3 );
    mVentControls[kMinorityType].setNoMercy( majNum < kMinorityPopulation/2 );
    
  } // updateHuntFactors()
  
  // if any summoners are active, modify the vent controls
  private void checkSummoners(SpriteManager spriteManager) {
    
    for ( Sprite s : spriteManager.list() ) {
      if ( s instanceof Summoner ) {
        Summoner summon = (Summoner)s;
        final int type = summon.floxelType();
        mVentControls[type].overrideTracking(summon.xPos(), 
                                             summon.yPos(), 
                                             summon.strength());
      }
    }
    
  } // checkSummoners()
  
  // bring the flow fields up-to-date 
  private void updateFlows(SpriteManager spriteManager) {

    mFloxels.defineFlockingSources();
    mFloxels.addHuntingSources();

    for ( Sprite s : spriteManager.list() ) {
      if ( s instanceof SourceTerm ) {
        SourceTerm st = (SourceTerm)s;
        for ( int type = 0 ; type < kNumTypes ; type++ ) {
          Flow flow = mFlows[type];
          st.addToSource(type, flow.source(), flow.refineFactor());
        }
      }
    }

    for ( VentControl v : mVentControls ) v.advance();
    
    for ( Flow flow : mFlows ) flow.solve();
    
  } // updateFlows()

  // increase the number of bouncers if necessary
  private void addBouncers(SpriteManager spriteManager) {
    
    if ( mFloxels.numFloxels(kMajorityType) < kMinorityPopulation ||
         mFloxels.numFloxels(kMinorityType) < kMinorityPopulation/2 ) {
      
      mNewBouncerTimer = 0.0f;
      
    } else if ( mNewBouncerTimer > 0.0f ) {
      
      mNewBouncerTimer -= Env.TICK_TIME;
      if ( mNewBouncerTimer <= 0.0f ) {
        spriteManager.addSprite(new Bouncer(mMaze, mFloxels));
//        Env.sounds().playSpawnSound();
        mNewBouncerTimer = 0.0f;
      }
      
    } else {

      int num = 0;
      for ( Sprite sp : spriteManager.list() ) {
        if ( sp instanceof Bouncer ) num++;
      }
      if ( num < mMaxNumBouncers ) {
        mNewBouncerTimer = Env.randomFloat(kBouncerDelayMin, kBouncerDelayMax);
      }
      
    }
    
  } // addBouncers()
  
  // tweak the difficulty for the level
  private void setLevelDifficulty() {

    mMajorityPopulation = 0;
    for ( int level = 0 ; level <= mLevel ; level++ ) {
      if      ( mMajorityPopulation ==  0 ) mMajorityPopulation =  400; //200;
      else if ( mMajorityPopulation < 300 ) mMajorityPopulation += 100;
      else if ( mMajorityPopulation < 400 ) mMajorityPopulation += 50;
      else if ( mMajorityPopulation < 600 ) mMajorityPopulation += 25;
      else if ( mMajorityPopulation < 800 ) mMajorityPopulation += 20;
      else                                  mMajorityPopulation += 10;
    }
    
    final float minFactor = 0.4f;
    if ( mMajorityPopulation <= kMajorityPopulationMax ) {
      final int majPopStart = 200;
      final float h = (mMajorityPopulation - majPopStart)
                      /(float)(kMajorityPopulationMax - majPopStart);
      assert( h >= 0.0f && h <= 1.0f );
      mDifficultyFactor = (1-h) + h*minFactor;
    } else {
      final float h = (mMajorityPopulation-kMajorityPopulationMax)/10.0f;
      assert( h > 0.0f );
      mDifficultyFactor = minFactor + 0.01f*h;
    }

    mMajorityPopulation = Math.min(mMajorityPopulation, 
                                   kMajorityPopulationMax);
    
    if      ( mLevel <=  5 ) mMaxNumBouncers = 0;
    else if ( mLevel <= 20 ) mMaxNumBouncers = 1;
    else if ( mLevel <= 40 ) mMaxNumBouncers = 2;
    else                     mMaxNumBouncers = 3;
    
    if ( Env.debugMode() ) {
      Env.debug("Level " + mLevel 
                + " (" + mMajorityPopulation
                + " / " + mDifficultyFactor
                + " / " + mMaxNumBouncers + ")");
    }
    
  } // setLevelDifficulty()
  
  // things to do on the first frame
  private void prepareNewStory(SpriteManager spriteManager) {

    prepareNewRoom(spriteManager);

    setLevelDifficulty();

    mCursor = null;
    
    mIntroTimer = kIntroDelay;
    mRestartTimer = 0.0f;
    mStartleTimer = 0.0f;
    mNewBouncerTimer = 0.0f;
    mMegaBlastTimer = 0.0f;
    
    mReversed = false;
    mChanging = false;
    
    //!!!
    //spriteManager.addSprite(new Seeker(mMaze, mFloxels, mFlows[1]));
    
  } // prepareNewStory()

  // add the sprites for a room
  private void prepareNewRoom(SpriteManager spriteManager) {

    mMaze = Mazes.get(mMazeNum);
    mBackground = new Background(mMaze);
    spriteManager.addSprite(mBackground);

    mScore = new Score();
    spriteManager.addSprite(mScore);
    
    mFlows = new Flow[kNumTypes];
    for ( int k = 0 ; k < mFlows.length ; k++ ) {
      mFlows[k] = new Flow(Env.numTilesX(), Env.numTilesY(), 4);
      prepareFlow(mFlows[k], mMaze);
      mFlows[k].reset();
      mFlows[k].solve();
    }
    
    mFloxels = new Floxels(mFlows);
    spriteManager.addSprite(mFloxels);

    for ( int type = 0 ; type < kNumTypes ; type++ ) {
      mFloxels.setHuntingStrength(type, 0.0f);
    }
    
    mVentControls = new VentControl[kNumTypes];
    for ( int type = 0 ; type < kNumTypes ; type++ ) {
      mVentControls[type] = new VentControl(mFlows[type], mFloxels, type);
    }
    mVentControls[kMajorityType].setHuntStrength(1.0f);
    mVentControls[kMinorityType].setHuntStrength(0.0f);

  } // prepareNewRoom()
  
  // build a flow consistent with the maze
  private void prepareFlow(Flow flow, Maze maze) {

    final int nx = Env.numTilesX(),
              ny = Env.numTilesY();
    
    final float inFlow = VentControl.inFlowDefault(); 

    float flowWalls[][][] = flow.walls();
    assert( flowWalls.length == ny && flowWalls[0].length == nx );

    for ( int iy = 0 ; iy < ny ; iy++ ) {
      for ( int ix = 0 ; ix < nx ; ix++ ) {
        float walls[] = flowWalls[iy][ix];
        walls[Env.NORTH] = ( maze.horizWall(ix, iy)   ? inFlow : Flow.OPEN );
        walls[Env.SOUTH] = ( maze.horizWall(ix, iy+1) ? inFlow : Flow.OPEN );
        walls[Env.WEST]  = ( maze.vertWall(ix, iy)    ? inFlow : Flow.OPEN );
        walls[Env.EAST]  = ( maze.vertWall(ix+1, iy)  ? inFlow : Flow.OPEN );
      }
    }
    
  } // prepareFlow()
  
  // reset the floxels to replay the level
  private void restartLevel(SpriteManager spriteManager) {
    
    mFloxels.reclaimFloxels(kMinorityPopulation, kMajorityType);
    
    spriteManager.removeSprite(mCursor);
    mCursor = null;
    mRestartTimer = kRestartDelay;

    mVentControls[kMajorityType].setHuntStrength(1.0f);
    mVentControls[kMinorityType].setHuntStrength(0.0f);

    Env.sounds().play(Sounds.FAIL, 15);
    
  } // restartLevel()

  // introduce a new population for the next level
  private void newLevel(SpriteManager spriteManager) {

    mLevel += 1;
    setLevelDifficulty();

    assert( mFloxels.numFloxels(kMajorityType) == 0 ); 
    mScore.setCurrentValue( mFloxels.numFloxels(kMinorityType) );
    mScore.fixBaseValue();

    mCursor.cancel();
    spriteManager.removeSprite(mCursor);
    mCursor = null;
    
    final int excess = mFloxels.numFloxels(kMinorityType) 
                       - mMajorityPopulation;
    if ( excess > 0 ) mFloxels.reclaimFloxels(excess, kMinorityType);

    switchPopulations(spriteManager);
    
    mRestartTimer = kRestartLongDelay;

    for ( Sprite sp : spriteManager.list() ) {
      if ( sp instanceof Bouncer) ((Bouncer)sp).disappear();
    }

    //if ( (mLevel % 3) == 0 ) {
    //  mMazeNum += 1;
    //  Maze nextMaze = Mazes.get(mMazeNum);
    //  mMaze.collectDifferences(nextMaze, mMazeDeltas);
    //  mChanging = true;
    //  mChangeTimer = kChangeFirstDelay;
    //}

    Env.sounds().play(Sounds.SUCCESS, 15);
    
  } // newLevel() 

  // majority and minority floxel populations switch places
  private void switchPopulations(SpriteManager spriteManager) {
    
    mFloxels.switchFloxelTypes();
    
    FloxelPainter.advanceColourIndex();
    
    for ( int type = 0 ; type < kNumTypes ; type++ ) {
      mVentControls[type].switchFloxelTypes();
    }

    VentControl temp = mVentControls[0];
    mVentControls[0] = mVentControls[1];
    mVentControls[1] = temp;

    for ( Sprite s : spriteManager.list() ) {
      if ( s instanceof Summoner ) ((Summoner)s).switchFloxelType();
    }
    
  } // switchPopulations()
  
} // class FloxelsStory

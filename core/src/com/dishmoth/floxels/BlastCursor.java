/*
 *  BlastCursor.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// object for converting mouse clicks into floxel-herding blasts
public class BlastCursor extends Sprite {

  // minimum time (seconds) before placing new blast
  private static final float kReloadDelay       = 0.25f,
                             kReloadDelaySwitch = 0.5f,
                             kReloadDelayMini   = 1.0f/30.0f,
                             kReloadDelayMega   = 2.0f;

  // don't place blasts too close to things
  private static final float kMarginEdge = 0.2f,
                             kMarginWall = 0.05f;

  // size of the stun effect
  private static final float kStunRadius = 0.5f;

  //
  private static final int kSummonedFloxelType = 1;
  
  // reference to the current maze
  private Maze mMaze;
  
  // reference to the floxels
  private Floxels mFloxels;
  
  // reload time until next blast can be fired
  private float mBlastDelay,
                mBlastDelayMini,
                mBlastDelayMega;
  
  // ready to fire a blast or whatever
  private boolean mTriggerLeft,
                  mTriggerRight;

  // reference to the summoner object we're controlling
  private Summoner mSummoner;
  
  // constructor
  public BlastCursor(Maze maze, Floxels floxels) {
    
    super(0);

    mMaze = maze;
    mFloxels = floxels;
    
    mBlastDelay = mBlastDelayMega = 0.0f;
    mBlastDelayMini = -1;
    mTriggerLeft = mTriggerRight = false;

    mSummoner = null;
    
  } // constructor
  
  // keep track of any bouncers
  @Override
  public void observeArrival(Sprite newSprite) { 
    
    if ( newSprite instanceof Bouncer ) mSpritesToWatch.add(newSprite);
    
  } // Sprite.observeArrival()
  
  // keep track of our summoner
  @Override
  public void observeDeparture(Sprite deadSprite) {
    
    if ( deadSprite instanceof Summoner ) {
      if ( deadSprite == mSummoner ) mSummoner = null;
    }
    
  } // Sprite.observeDeparture()
  
  // capture mouse state
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = Env.TICK_TIME;

    Env.mouse().updateState();
    MouseMonitor.State state = Env.mouse().getState();
    final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
    
    // left button: big blast, mini blast, or trigger a bouncer
    /*
    if ( state.b1 ) {
      if ( mTriggerLeft ) {
        if ( triggerBouncers(x,y) ) {
          mBlastDelay = kReloadDelay;
          mBlastDelayMini = kReloadDelaySwitch;
          mBlastDelayMega = kReloadDelayMega;
        } else if ( mBlastDelay == 0.0f ) {
          Blast blast = makeBlast(x, y);
          if ( blast != null ) {
            addTheseSprites.add(blast);
            Env.sounds().playBlastSound();
            mBlastDelay = kReloadDelay;
            mBlastDelayMini = kReloadDelaySwitch;
          }
        }
      } else {
        if ( mBlastDelayMini == 0.0f ) {
          BlastMini blast = makeBlastMini(x, y);
          if ( blast != null ) {
            addTheseSprites.add(blast);
            Env.sounds().playMiniBlastSound();
            mBlastDelayMini = kReloadDelayMini;
          } else {
            Env.sounds().stopMiniBlastSound();
          }
        }
      }
      mTriggerLeft = false;
    } else {
      mTriggerLeft = true;
      Env.sounds().stopMiniBlastSound();
    }
    */
    
    // right button: summon the floxels
    if ( state.b ) {
      boolean triggered = false;
      if ( mTriggerRight ) {
        mTriggerRight = false;
        triggered = true;
      }
      if ( x > 0 && x < Env.numTilesX() && y > 0 && y < Env.numTilesY() ) {
        boolean makeSound = triggered;
        if ( mSummoner == null ) {
//          mSummoner = new Summoner(kSummonedFloxelType);
          addTheseSprites.add(mSummoner);
          makeSound = true;
        }
        setSummonerPosition(x, y);
//        if ( triggered ) mSummoner.reset();
        if ( makeSound ) Env.sounds().playSummonsSound();
      }
    } else {
      mTriggerRight = true;
    }
    
    // decrement timers
    if ( mBlastDelay > 0.0f ) {
      mBlastDelay -= dt;
      if ( mBlastDelay < 1.0e-4 ) mBlastDelay = 0;
    }
    if ( mBlastDelayMega > 0.0f ) {
      mBlastDelayMega -= dt;
      if ( mBlastDelayMega < 1.0e-4 ) mBlastDelayMega = 0;
    }
    if ( mBlastDelayMini > 0.0f ) {
      mBlastDelayMini -= dt;
      if ( mBlastDelayMini < 1.0e-4 ) mBlastDelayMini = 0;
    }

  } // Sprite.advance()

  /*
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = 1.0f/Env.ticksPerSecond();
    
    MouseMonitor.State state = Env.mouse().getState();
    final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
    
    // left button: big blast (or trigger a bouncer)
    if ( state.b1 ) {
      if ( mTriggerLeft ) {
        if ( triggerBouncers(x,y) ) {
          mBlastDelay = mBlastDelayMini = kReloadDelay;
          mBlastDelayMega = kReloadDelayMega;
        } else if ( mBlastDelay == 0.0f ) {
          Blast blast = makeBlast(x, y);
          if ( blast != null ) {
            addTheseSprites.add(blast);
            Env.sounds().playBlastSound();
            mBlastDelay = mBlastDelayMini = kReloadDelay;
            mButtonSustain = 0.0f;
          }
        }
      }
      mTriggerLeft = false;
    } else {
      mTriggerLeft = true;
    }
    
    if ( mBlastDelay > 0.0f ) {
      mBlastDelay -= dt;
      if ( mBlastDelay < 1.0e-4 ) mBlastDelay = 0;
    }
    if ( mBlastDelayMega > 0.0f ) {
      mBlastDelayMega -= dt;
      if ( mBlastDelayMega < 1.0e-4 ) mBlastDelayMega = 0;
    }
    
    // right button: small blast
    if ( state.b2 ) {
      mButtonSustain = kButtonSustainTime;
    } else if ( mButtonSustain > 0.0f ) {
      mButtonSustain = Math.max(0.0f, mButtonSustain-dt);
    }
    if ( mButtonSustain > 0.0f && mBlastDelayMini == 0.0f ) {
      BlastMini blast = makeBlastMini(x, y);
      if ( blast != null ) {
        addTheseSprites.add(blast);
        Env.sounds().playMiniBlastSound();
        mBlastDelayMini = kReloadDelayMini;
      }
    }
    if ( mButtonSustain == 0.0f ) Env.sounds().stopMiniBlastSound();

    if ( mBlastDelayMini > 0.0f ) {
      mBlastDelayMini -= dt;
      if ( mBlastDelayMini < 1.0e-4 ) mBlastDelayMini = 0;
    }

  } // Sprite.advance()
  */
  
  // create a standard blast
  private Blast makeBlast(float x, float y) {

    if ( x <= 0.0f || x >= Env.numTilesX() ||
         y <= 0.0f || y >= Env.numTilesY() ) return null;
    
    x = Math.max(kMarginEdge, Math.min(Env.numTilesX()-kMarginEdge, x));
    y = Math.max(kMarginEdge, Math.min(Env.numTilesY()-kMarginEdge, y));

    final int ix = (int)Math.floor(x),
              iy = (int)Math.floor(y);
    float dx = x - ix,
          dy = y - iy;
    if ( dx < kMarginWall && mMaze.vertWall(ix, iy) ) {
      dx = kMarginWall;
    } else if ( dx > 1.0f-kMarginWall && mMaze.vertWall(ix+1, iy) ) {
      dx = 1.0f-kMarginWall;
    }
    if ( dy < kMarginWall && mMaze.horizWall(ix, iy) ) {
      dy = kMarginWall;
    } else if ( dy > 1.0f-kMarginWall && mMaze.horizWall(ix, iy+1) ) {
      dy = 1.0f-kMarginWall;
    }
    final float xBlast = ix + dx,
                yBlast = iy + dy;
    
    mFloxels.stunFloxels(xBlast, yBlast, kStunRadius);

    return new Blast(xBlast, yBlast);
    
  } // makeBlast()

  // create a small blast
  private BlastMini makeBlastMini(float x, float y) {
    
    if ( x <= 0.0f || x >= Env.numTilesX() ||
         y <= 0.0f || y >= Env.numTilesY() ) return null;
    
    final int ix = (int)Math.floor(x),
              iy = (int)Math.floor(y);
    float dx = x - ix,
          dy = y - iy;
    if ( dx < kMarginWall && mMaze.vertWall(ix, iy) ) {
      dx = kMarginWall;
    } else if ( dx > 1.0f-kMarginWall && mMaze.vertWall(ix+1, iy) ) {
      dx = 1.0f-kMarginWall;
    }
    if ( dy < kMarginWall && mMaze.horizWall(ix, iy) ) {
      dy = kMarginWall;
    } else if ( dy > 1.0f-kMarginWall && mMaze.horizWall(ix, iy+1) ) {
      dy = 1.0f-kMarginWall;
    }
    final float xBlast = ix + dx,
                yBlast = iy + dy;
    
    return new BlastMini(xBlast, yBlast);
    
  } // makeBlastMini()
  
  // check for mouse clicks on bouncers
  private boolean triggerBouncers(float x, float y) {
    
    if ( mBlastDelayMega != 0.0f ) return false;
    
    for ( Sprite sp : mSpritesToWatch ) {
      Bouncer bouncer = (Bouncer)sp;
      if ( bouncer.hit(x, y) ) {
        Env.sounds().playMegaBlastSound();
        return true;
      }
    }
    return false;
    
  } // triggerBouncers()

  // put the summoner at (or near) the specified position
  private void setSummonerPosition(float x, float y) {
    
    assert( mSummoner != null );

    x = Math.max(kMarginEdge, Math.min(Env.numTilesX()-kMarginEdge, x));
    y = Math.max(kMarginEdge, Math.min(Env.numTilesY()-kMarginEdge, y));
    
//    mSummoner.setPosition(x, y);
    
  } // setSummonerPosition()
  
  // nothing to see here
  @Override
  public void draw(SpriteBatch batch) {
  } // Sprite.draw()

} // class BlastCursor

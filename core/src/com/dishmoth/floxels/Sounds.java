/*
 *  Sounds.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.io.IOException;

// class for controlling audio
public class Sounds {
  
  // true if sounds have been loaded and all is operational
  private boolean mAvailable;
  
  // true if audio has been turned off by the user
  private boolean mMuted;

  // sounds for floxels dying
  private static final int kNumFloxelTypes   = 2,
                           kNumDeathSounds   = 10,
                           kDeathTicksMin    = 2,
                           kDeathTicksMax    = 3;
  private SoundEffect      mDeathSounds[][]  = null;
  private int              mDeathNextIndex[] = null,
                           mDeathCount[]     = null;
  private int              mDeathDelay;
  
  // sound for firing a blast
  private static final int kNumBlastSounds = 3;
  private SoundEffect      mBlastSounds[]  = null;
  private int              mBlastCount;
  
  // sound for firing a mega-blast
  private SoundEffect mMegaBlastSound = null;
  
  // sound for firing a mini-blast
  private SoundEffect mMiniBlastSound = null;

  // sound for a bubble appearing
  private SoundEffect mSpawnSound = null;
  
  // sound for summoning floxels
  private SoundEffect mSummonsSound = null;
  
  // sound for the player losing
  private static final int kFailSoundDelay = 15;
  private int              mFailSoundTimer;
  private SoundEffect      mFailSound = null;
  
  // sound for the player winning
  private static final int kSuccessSoundDelay = 15;
  private int              mSuccessSoundTimer;
  private SoundEffect      mSuccessSound = null;
  
  // sound for floxels being released 
  private SoundEffect mUnleashSound = null;
  
  // sound for lots of floxels being released 
  private SoundEffect mBigUnleashSound = null;
  
  // sound for minority going on the offensive 
  private SoundEffect mReversalSound = null;
  
  // sound for the maze changing 
  private SoundEffect mMorphSound = null;
  
  // silence (for testing sound system)
  private SoundEffect mTestSound = null;
  
  // constructor
  public Sounds() {
    
    mAvailable = false;
    mMuted     = false;

  } // constructor
  
  // mute or unmute the sound
  public void mute() { mMuted = true; }
  public void unmute() { mMuted = false; }
  public boolean isMuted() { return mMuted; }
  
  // load and prepare all the sound effects
  public void initialize() {

    if ( mAvailable ) return;
    
    try {

      mDeathSounds = new SoundEffect[kNumFloxelTypes][kNumDeathSounds];
      for ( int type = 0 ; type < kNumFloxelTypes ; type++ ) {
        String name = ( type == 0 ? "grunt" : "squeak" );
        for ( int k = 0 ; k < kNumDeathSounds ; k++ ) {
          mDeathSounds[type][k] = Env.resources().loadSoundEffect(
                                                        name + k + ".ogg");
        }
      }
      mDeathCount = new int[kNumFloxelTypes];
      mDeathNextIndex = new int[kNumFloxelTypes];
      mDeathDelay = 0;
      
      mBlastSounds = new SoundEffect[kNumBlastSounds];
      for ( int k = 0 ; k < kNumBlastSounds ; k++ ) {
        mBlastSounds[k] = Env.resources().loadSoundEffect("blast.ogg");
      }
      mBlastCount = 0;
      
      mMegaBlastSound = Env.resources().loadSoundEffect("megablast.ogg");
      
      mMiniBlastSound = Env.resources().loadSoundEffect("miniblast.ogg");
      mMiniBlastSound.setLooped(true);
      
      mSpawnSound = Env.resources().loadSoundEffect("spawn.ogg");

      mSummonsSound = Env.resources().loadSoundEffect("summons.ogg");

      mFailSound = Env.resources().loadSoundEffect("fail.ogg");
      mFailSoundTimer = 0;
      
      mSuccessSound = Env.resources().loadSoundEffect("success.ogg");
      mSuccessSoundTimer = 0;
      
      mUnleashSound = Env.resources().loadSoundEffect("unleash.ogg");

      mBigUnleashSound = Env.resources().loadSoundEffect("unleash_big.ogg");

      mReversalSound = Env.resources().loadSoundEffect("reversal.ogg");
      
      mMorphSound = Env.resources().loadSoundEffect("morph.ogg");
      
      mTestSound = Env.resources().loadSoundEffect("silence.ogg");
      
      mAvailable = true;
      
      mTestSound.play(); // wake up the sound code
      
    } catch ( IOException ex ) {
      
      if ( Env.debugMode() ) System.out.println(ex.getMessage());
      mDeathSounds     = null;
      mBlastSounds     = null;
      mMegaBlastSound  = null;
      mMiniBlastSound  = null;
      mSpawnSound      = null;
      mSummonsSound    = null;
      mFailSound       = null;
      mSuccessSound    = null;
      mUnleashSound    = null;
      mBigUnleashSound = null;
      mReversalSound   = null;
      mMorphSound      = null;
      mTestSound       = null;
      
    }
    
  } // initialize()

  // stop all looping sounds
  public void stop() {
    
    stopMiniBlastSound();
    
  } // stop()
  
  // note that a frame has passed
  public void advance() {
    
    if ( !mAvailable || mMuted ) return;    

    // play delayed sounds
    
    if ( mFailSoundTimer > 0 ) {
      if ( --mFailSoundTimer == 0 ) mFailSound.play();
    }

    if ( mSuccessSoundTimer > 0 ) {
      if ( --mSuccessSoundTimer == 0 ) mSuccessSound.play();
    }
    
    // choose a death sound to play
    
    if ( mDeathDelay > 0 ) {
      mDeathDelay -= 1;
    } else if ( mDeathCount[0] > 0 || mDeathCount[1] > 0 ) {
      mDeathDelay = Env.randomInt(kDeathTicksMin, kDeathTicksMax);
  
      final int type  = ( (mDeathCount[0] > mDeathCount[1]) ? 0 : 1 ),
                index = mDeathNextIndex[type];
      mDeathSounds[type][index].play();
      
      mDeathNextIndex[type] = (index + Env.randomInt(1,2)) % kNumDeathSounds;
      mDeathCount[0] = mDeathCount[1] = 0;
    }
    
  } // advance()

  // start a sound appropriate to the recent casualty counts
  public void playDeathSounds(int killCount[]) {
    
    assert( killCount.length == kNumFloxelTypes );
    
    for ( int k = 0 ; k < kNumFloxelTypes ; k++ ) {
      mDeathCount[k] += killCount[k];
    }
    
  } // playDeathSounds()
  
  // start a sound playing when the player fires a blast
  public void playBlastSound() {
    
    if ( !mAvailable || mMuted ) return;
    
    mBlastSounds[mBlastCount].play();
    mBlastCount = (mBlastCount + 1) % kNumBlastSounds;
    
  } // playBlastSound()

  // start a sound playing when the player fires a mega-blast
  public void playMegaBlastSound() {
    
    if ( !mAvailable || mMuted ) return;
    
    mMegaBlastSound.play();
    
  } // playMegaBlastSound()

  // start a sound playing (looping) when the player fires a mini-blast
  public void playMiniBlastSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mMiniBlastSound.play();
    
  } // playMiniBlastSound()
  
  // stop the sound playing for the mini-blast
  public void stopMiniBlastSound() {
    
    if ( mMiniBlastSound != null ) mMiniBlastSound.stop();
    
  } // stopMiniBlastSound()
  
  // start a sound playing when a bubble appears
  public void playSpawnSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mSpawnSound.play();
    
  } // playSpawnSound()
  
  // start a sound playing when floxels are summoned
  public void playSummonsSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mSummonsSound.play();
    
  } // playSummonsSound()
  
  // start a sound playing when the player loses
  public void playFailSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mFailSoundTimer = kFailSoundDelay;
    
  } // playFailSound()
  
  // start a sound playing when the player wins
  public void playSuccessSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mSuccessSoundTimer = kSuccessSoundDelay;
    
  } // playSuccessSound()
  
  // start a sound playing when floxels are released
  public void playUnleashSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mUnleashSound.play();
    
  } // playUnleashSound()
  
  // start a sound playing when lots of floxels are released
  public void playBigUnleashSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mBigUnleashSound.play();
    
  } // playBigUnleashSound()
  
  // start a sound playing when the minority goes on the offensive
  public void playReversalSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mReversalSound.play();
    
  } // playReversalSound()
  
  // start a sound playing when the maze changes
  public void playMorphSound() {
    
    if ( !mAvailable || mMuted ) return;    
    mMorphSound.play();
    
  } // playMorphSound()
  
} // class Sounds

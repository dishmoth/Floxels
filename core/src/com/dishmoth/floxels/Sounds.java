/*
 *  Sounds.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.Iterator;
import java.util.LinkedList;

// class for controlling audio
public class Sounds {

  // identifiers for the different effects
  public static final int  GRUNT_0      =  0,
                           GRUNT_1      =  1,
                           GRUNT_2      =  2,
                           GRUNT_3      =  3,
                           GRUNT_4      =  4,
                           GRUNT_5      =  5,
                           GRUNT_6      =  6,
                           GRUNT_7      =  7,
                           GRUNT_8      =  8,
                           GRUNT_9      =  9,
                           SQUEAK_0     = 10,
                           SQUEAK_1     = 11,
                           SQUEAK_2     = 12,
                           SQUEAK_3     = 13,
                           SQUEAK_4     = 14,
                           SQUEAK_5     = 15,
                           SQUEAK_6     = 16,
                           SQUEAK_7     = 17,
                           SQUEAK_8     = 18,
                           SQUEAK_9     = 19,
                           POP_0        = 20,
                           POP_1        = 21,
                           POP_2        = 22,
                           POP_3        = 23,
                           POP_4        = 24,
                           POP_5        = 25,
                           SUCCESS      = 26,
                           FAIL         = 27,
                           UNLEASH_0    = 28,
                           UNLEASH_1    = 29,
                           UNLEASH_2    = 30,
                           UNLEASH_3    = 31,
                           UNLEASH_4    = 32,
                           UNLEASH_BIG  = 33,
                           REVERSAL     = 34,
                           MAZE_MORPH   = 35,
                           SUMMON_A     = 36,
                           SUMMON_B     = 37,
                           SUMMON_QUICK = 38,
                           BUBBLE_ON    = 39,
                           BUBBLE_OFF   = 40;
  private static final int kNumSounds   = 41;
  
  // true if sounds have been loaded and all is operational
  private boolean mAvailable;
  
  // true if audio has been turned off by the user
  private boolean mMuted;

  // the sound clips (Sound for effects, Music for loops)
  private Sound mSounds[];
  private Music mLoops[];
  
  // queued sound effects [delay,id]
  private LinkedList<int[]> mDelayedSounds;

  // special logic for choosing which of the floxel death sounds to play
  private static final int kNumDeathSounds   = 10;
  private static final int kDeathTicksMin    = 1,
                           kDeathTicksMax    = 3;
  private int              mDeathNextIndex[] = new int[]{0,0},
                           mDeathCount[]     = new int[]{0,0},
                           mDeathDelay       = 0;
  private static final int kNumCaptureSounds = 6;
  private static final int kCaptureTicksMin  = 0,
                           kCaptureTicksMax  = 2;
  private int              mCaptureNextIndex = 0,
                           mCaptureCount     = 0,
                           mCaptureDelay     = 0;
  private static final int kNumUnleashSounds = 5;
    
  // constructor
  public Sounds() {
    
    mAvailable = false;
    mMuted = false;

    mSounds = new Sound[kNumSounds];
    mLoops  = new Music[kNumSounds];
    
    mDelayedSounds = new LinkedList<int[]>();
    
  } // constructor
  
  // mute or unmute the sound
  public void mute() { mMuted = true; }
  public void unmute() { mMuted = false; }
  public boolean isMuted() { return mMuted; }
  
  // load and prepare all the sound effects
  public void initialize() {

    if ( mAvailable ) return;

    Env.debug("Loading sound files");
    
    loadSound(SUCCESS, "success.ogg");
    loadSound(FAIL, "fail.ogg");
    loadSound(UNLEASH_BIG, "unleash_big.ogg");
    loadSound(REVERSAL, "reversal.ogg");
    loadSound(MAZE_MORPH, "maze_morph.wav");
    loadSound(SUMMON_A, "summon_A.wav");
    loadSound(SUMMON_B, "summon_B.wav");
    loadSound(SUMMON_QUICK, "summon_quick.wav");
    loadSound(BUBBLE_ON, "bubble_on.wav");
    loadSound(BUBBLE_OFF, "bubble_off.wav");

    for ( int k = 0 ; k < kNumDeathSounds ; k++ ) {
      loadSound(GRUNT_0+k, "grunt"+k+".ogg");
      loadSound(SQUEAK_0+k, "squeak"+k+".ogg");
    }

    for ( int k = 0 ; k < kNumCaptureSounds ; k++ ) {
      loadSound(POP_0+k, "pop"+k+".ogg");
    }

    for ( int k = 0 ; k < kNumUnleashSounds ; k++ ) {
      loadSound(UNLEASH_0+k, "unleash"+k+".wav");
    }

    checkSounds();
    if ( mAvailable ) Env.debug("Sounds loaded successfully");
    else              Env.debug("Sound disabled; effects failed to load");
    

  } // initialize()
    
  // identify which sounds must be looped
  private boolean isLooped(int id) {
    
    return false;
    
  } // isLooped()
  
  // treat the sound effect as a Music object 
  private boolean playAsMusic(int id) {
    
    return false;
    
  } // playAsMusic()
  
  // prepare a sound resource
  private void loadSound(int id, String fileName) {
    
    assert( id >= 0 && id < kNumSounds );
    assert( fileName != null );
    
    try {

      if ( isLooped(id) ) {
        if ( mLoops[id] != null ) return; // already loaded
        mLoops[id] = Gdx.audio.newMusic(Gdx.files.internal(fileName));
        mLoops[id].setLooping(true);
      } else if ( playAsMusic(id) ) {
        if ( mLoops[id] != null ) return; // already loaded
        mLoops[id] = Gdx.audio.newMusic(Gdx.files.internal(fileName));
        mLoops[id].setLooping(false);
      } else {
        if ( mSounds[id] != null ) return; // already loaded
        mSounds[id] = Gdx.audio.newSound(Gdx.files.internal(fileName));
      }
      
    } catch (Exception ex) {
      Env.debug(ex.getMessage());
      mSounds[id] = null;
      mLoops[id] = null;
    }

  } // loadSound()

  // check that all sounds have loaded
  private void checkSounds() {

    mAvailable = true;
    
    for ( int id = 0 ; id < kNumSounds ; id++ ) {
      if ( mSounds[id] == null && mLoops[id] == null ) {
        mAvailable = false;
        return;
      }
    }
    
  } // checkSound()

  // note that a frame has passed (and play delayed sounds)
  public void advance() {
    
    for ( Iterator<int[]> it = mDelayedSounds.iterator() ; it.hasNext() ; ) {
      int details[] = it.next();
      assert( details != null && details.length == 2 );
      assert( details[0] > 0 );
      details[0] -= 1;
      if ( details[0] == 0 ) {
        play(details[1]);
        it.remove();
      }
    }
    
    advanceSpecial();
    
  } // advance()
  
  // play a sound effect
  public void play(int id) {
    
    if ( !mAvailable || mMuted ) return;
    
    assert( id >= 0 && id < kNumSounds );
    assert( !isLooped(id) );

    if      ( mSounds[id] != null ) mSounds[id].play();
    else if ( mLoops[id]  != null ) mLoops[id].play();
    
  } // play()
  
  // play a sound effect after a delay 
  public void play(int id, int delay) {
    
    assert( id >= 0 && id < kNumSounds );
    assert( delay >= 0 );
    if ( delay == 0 ) {
      play(id);
    } else {
      mDelayedSounds.add(new int[]{ delay, id });
    }
    
  } // play(delay)
  
  // start a sound looping (if it isn't already)
  public void loop(int id) {
    
    if ( !mAvailable || mMuted ) return;
    
    assert( id >= 0 && id < kNumSounds );
    assert( isLooped(id) );
    
    if ( !mLoops[id].isPlaying() ) mLoops[id].play();
    
  } // loop()
  
  // stop a looping sound
  public void stop(int id) {

    if ( !mAvailable || mMuted ) return;
    
    assert( id >= 0 && id < kNumSounds );
    assert( isLooped(id) );
    
    if ( mLoops[id].isPlaying() ) mLoops[id].stop();
    
  } // stop()

  // stop all looping sounds
  public void stopAll() {

    for ( int id = 0 ; id < kNumSounds ; id++ ) {
      if ( isLooped(id) ) stop(id);
    }
    
  } // stopAll()

  // update counters for death and capture sounds
  private void advanceSpecial() {
        
    if ( mDeathDelay > 0 ) {
      mDeathDelay -= 1;
    } else if ( mDeathCount[0] > 0 || mDeathCount[1] > 0 ) {
      mDeathDelay = Env.randomInt(kDeathTicksMin, kDeathTicksMax);
  
      final int type  = ( (mDeathCount[0] > mDeathCount[1]) ? 0 : 1 ),
                index = mDeathNextIndex[type];
      final int id = ( type==0 ? GRUNT_0 : SQUEAK_0 ) + index;
      play(id);
      
      mDeathNextIndex[type] = (index + Env.randomInt(1,2)) % kNumDeathSounds;
      mDeathCount[0] = mDeathCount[1] = 0;
    }
    
    if ( mCaptureDelay > 0 ) {
      mCaptureDelay -= 1;
    } else if ( mCaptureCount > 0 ){
      mCaptureDelay = Env.randomInt(kCaptureTicksMin, kCaptureTicksMax);
      
      final int id = POP_0 + mCaptureNextIndex;
      play(id);
      
      mCaptureNextIndex = (mCaptureNextIndex + Env.randomInt(1,2)) 
                          % kNumCaptureSounds;
      mCaptureCount = 0;
    }
    
  } // advanceSpecial()
  
  // play sounds for dead floxels based on which are dying most
  public void playDeathSounds(int killCount[]) {
    
    if ( !mAvailable || mMuted ) return;

    assert( killCount.length == 2 );
    for ( int k = 0 ; k < 2 ; k++ ) {
      mDeathCount[k] += killCount[k];
    }
    
  } // playDeathSounds()

  // play sounds for captured floxels
  public void playCaptureSound(int count) {
    
    if ( !mAvailable || mMuted ) return;

    assert( count > 0 );
    mCaptureCount += count;
    
  } // playCaptureSound()
  
  // play sounds for unleashed floxels
  public void playUnleashSound(int count) {
    
    if ( !mAvailable || mMuted ) return;

    assert( count > 0 );
    Env.debug("count="+count);
    if      ( count <=  10 ) play(UNLEASH_0);
    else if ( count <=  50 ) play(UNLEASH_1);
    else if ( count <= 100 ) play(UNLEASH_2);
    else if ( count <= 250 ) play(UNLEASH_3);
    else                     play(UNLEASH_4);
    
  } // playUnleashSound()
  
} // class Sounds

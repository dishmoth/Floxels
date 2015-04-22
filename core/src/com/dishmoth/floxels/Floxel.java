/*
 *  Floxel.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// basic particle details
public final class Floxel {

  // number of different populations
  public static final int NUM_TYPES = 2;
  
  // number of different shades within a colour scheme
  public static final int NUM_SHADES = 16;
  
  // number of faces, and their different types 
  public static final int NUM_FACES = 8;
  public static final int NUM_NORMAL_FACES = 7;
  public static final int NUM_EXPRESSIONS = 5;
  public static final int BLINK_FACE = 5;
  public static final int STUN_FACE = 6;
  public static final int SPLAT_FACE = 7;
  
  // different types of behaviour
  public enum State { UNUSED, NORMAL, SPLATTED, RECLAIMED, STUNNED };
  
  // current mode of behaviour
  public State mState = State.UNUSED;

  // position (in base grid units)
  public float mX = 0.0f, 
               mY = 0.0f;
  
  // timer counting down depending on current state (0 if not in use)
  public short mTimer = 0;
  
  // score rating for the floxel's current cluster (0 to maxClusterScore())
  public byte mCluster = 0;

  // if true then the floxel is on top of another of the same type
  public boolean mNeedsNudge = false;

  // which population the floxel belongs to (0 to numTypes()-1)
  public byte mType = 0;

  // brightness of the floxel (0 to numShades()-1)
  public byte mShade = 0;
  
  // which face the floxel is showing (0 to numFaces()-1)
  public byte mFace = 0;
  
} // class Floxel

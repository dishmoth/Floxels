/*
 *  Floxel.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// basic particle details
public final class Floxel {

  // number of different populations
  public static int numTypes() { return 2; }
  
  // number of different shades within a colour scheme
  public static int numShades() { return 16; }
  
  // number of faces, and their different types 
  public static int numFaces() { return 8; }
  public static int numNormalFaces() { return 7; }
  public static int numExpressions() { return 5; }
  public static int blinkFace() { return 5; }
  public static int stunFace() { return 6; }
  public static int splatFace() { return 7; }
  
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

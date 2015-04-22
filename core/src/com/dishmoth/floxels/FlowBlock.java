/*
 *  FlowBlock.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// boundary conditions for a region of fluid flow
// if a boundary is present then the in-flowing velocity is specified
public final class FlowBlock {

  // enumeration of directions
  static public final int kNorth         = 0,
                          kSouth         = 1,
                          kEast          = 2,
                          kWest          = 3;
  static public final int kNumDirections = 4;
  
  // record of block's position indices
  private int mXPos,
              mYPos;

  // how refined the solution needs to be in this block
  // (an optimization if a quality solution is only needed in some regions) 
  private int mDesiredSolutionLevel;
  
  // which edges of the block have boundaries
  private boolean mBoundary[] = new boolean[kNumDirections];
  
  // velocity of fluid coming in through the boundary (0.0 => solid wall)
  private float mInFlow[] = new float[kNumDirections];
  
  // constructor
  public FlowBlock(int xPos, int yPos) {

    mXPos = xPos;
    mYPos = yPos;
    
    mBoundary[kNorth] = mBoundary[kSouth] 
      = mBoundary[kEast] = mBoundary[kWest] = false;
    
    mDesiredSolutionLevel = 999;
    
  } // constructor

  // remove all boundaries
  public void clear() {
    
    mBoundary[kNorth] = mBoundary[kSouth] 
      = mBoundary[kEast] = mBoundary[kWest] = false;

  } // clear()

  // access position indices
  public int xPos() { return mXPos; }
  public int yPos() { return mYPos; }
  
  // define boundaries
  public void setBoundaryNorth(float vel) { 
    mBoundary[kNorth] = true;
    mInFlow[kNorth] = vel;
  }
  public void setBoundarySouth(float vel) { 
    mBoundary[kSouth] = true;
    mInFlow[kSouth] = vel;
  }
  public void setBoundaryEast(float vel) { 
    mBoundary[kEast] = true;
    mInFlow[kEast] = vel;
  }
  public void setBoundaryWest(float vel) { 
    mBoundary[kWest] = true;
    mInFlow[kWest] = vel;
  }
  public void setBoundary(int index, float vel) {
    assert( index >= 0 && index < kNumDirections );
    mBoundary[index] = true;
    mInFlow[index] = vel;
  }

  // remove boundaries
  public void clearBoundaryNorth() { mBoundary[kNorth] = false; }
  public void clearBoundarySouth() { mBoundary[kSouth] = false; }
  public void clearBoundaryEast()  { mBoundary[kEast] = false; }
  public void clearBoundaryWest()  { mBoundary[kWest] = false; }
  public void clearBoundary(int index) {
    assert( index >= 0 && index < kNumDirections );
    mBoundary[index] = false;
  }
  
  // query existence of boundaries
  public boolean boundaryNorth() { return mBoundary[kNorth]; }
  public boolean boundarySouth() { return mBoundary[kSouth]; }
  public boolean boundaryEast()  { return mBoundary[kEast]; }
  public boolean boundaryWest()  { return mBoundary[kWest]; }
  public boolean boundary(int index) {
    assert( index >= 0 && index < kNumDirections );
    return mBoundary[index];
  }
  
  // in-flowing velocities
  public float inFlowNorth() { 
    assert(mBoundary[kNorth]); 
    return mInFlow[kNorth]; 
  }
  public float inFlowSouth() { 
    assert(mBoundary[kSouth]); 
    return mInFlow[kSouth]; 
  }
  public float inFlowEast() { 
    assert(mBoundary[kEast]);
    return mInFlow[kEast]; 
  }
  public float inFlowWest() { 
    assert(mBoundary[kWest]);
    return mInFlow[kWest]; 
  }
  public float inFlow(int index) {
    assert( index >= 0 && index < kNumDirections );
    assert(mBoundary[index]);
    return mInFlow[index];
  }

  // access the desired solution level for this block
  public void setDesiredSolutionLevel(int level) { 
    assert( level >= 0 );
    mDesiredSolutionLevel = level; 
  }
  public void setMinDesiredSolutionLevel(int level) { 
    assert( level >= 0 );
    if ( level > mDesiredSolutionLevel ) mDesiredSolutionLevel = level; 
  }
  public int desiredSolutionLevel() { return mDesiredSolutionLevel; }
  
} // class FlowBlock

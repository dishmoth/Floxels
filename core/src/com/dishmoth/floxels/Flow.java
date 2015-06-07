/*
 *  Flow.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.Arrays;

// class for generating flow field
public class Flow {

  // simple class for returning velocity components
  static public class Vel {
    public float x=0.0f, y=0.0f;
  } // class Flow.Vel

  // the base grid (no refinement)
  private FlowBlock mBaseBlocks[][] = null;
  private int mBaseXSize,
              mBaseYSize;
  
  // the most refined solution grid
  private FlowGrid mTopGrid = null;
  
  // refinement factor and level for the topmost grid
  private int mRefineFactor,
              mRefineLevel;

  // constructor
  public Flow(int baseXSize, int baseYSize, int refineLevel) {
    
    assert( baseXSize > 0 && baseYSize > 0 );
    assert( refineLevel >= 0 );
    
    mBaseXSize = baseXSize;
    mBaseYSize = baseYSize;
    mBaseBlocks = new FlowBlock[mBaseYSize][mBaseXSize];
    
    mRefineLevel = refineLevel;
    mRefineFactor = ( 1 << mRefineLevel );
    
    mTopGrid = new FlowGrid(mBaseBlocks, mRefineLevel);
    
  } // constructor

  // access to block layout
  public FlowBlock[][] blocks() { return mBaseBlocks; }

  // access to potential field
  public float[][] data() { return mTopGrid.data(); }

  // access to source terms
  public float[][] source() { return mTopGrid.source(); }

  // top grid's refinement level
  public int refineLevel() { return mRefineLevel; }
  
  // top grid's refinement factor
  public int refineFactor() { return mRefineFactor; }
  
  // size of the base grid
  public int baseXSize() { return mBaseXSize; }
  public int baseYSize() { return mBaseYSize; }
  
  // clear the current solution
  public void reset() { mTopGrid.reset(); }
  
  // clear the current source terms
  public void clearSource() { 
    
    float source[][] = mTopGrid.source();
    for ( int ky = 0 ; ky < source.length ; ky++ ) {
      Arrays.fill(source[ky], 0.0f);
    }
    
  } // clearSource()
  
  // produce a solution
  public void solve() { mTopGrid.solve(); }

  // calculate and return velocity at a position
  public void getVelocity(float x, float y, Vel vel) {
    
    assert( x > 0.0f && x < mBaseXSize );
    assert( y > 0.0f && y < mBaseYSize );

    final int ix = (int)(x*mRefineFactor),
              iy = (int)(y*mRefineFactor);
    
    final int kx = ( ix >> mRefineLevel ),
              ky = ( iy >> mRefineLevel );
    FlowBlock block = mBaseBlocks[ky][kx];
    
    float data[][] = mTopGrid.data();
    final float delta = 1.0f/mRefineFactor;
    
    final int iy0 = (ky << mRefineLevel),
              iy1 = iy0 + mRefineFactor-1,
              ix0 = (kx << mRefineLevel),
              ix1 = ix0 + mRefineFactor-1;
        
    if ( block.boundaryWest() && ix == ix0 ) {
      vel.x = 0.5f*( (data[iy][ix+1] - data[iy][ix])/delta
                   + block.inFlowWest() );
    } else if ( block.boundaryEast() && ix == ix1 ) {
      vel.x = 0.5f*( (data[iy][ix] - data[iy][ix-1])/delta
                   - block.inFlowEast() );                
    } else {
      vel.x = (data[iy][ix+1] - data[iy][ix-1])/(2*delta);
    }
    
    if ( block.boundaryNorth() && iy == iy0 ) {
      vel.y = 0.5f*( (data[iy+1][ix] - data[iy][ix])/delta
                   + block.inFlowNorth() );
    } else if ( block.boundarySouth() && iy == iy1 ) {
      vel.y = 0.5f*( (data[iy][ix] - data[iy-1][ix])/delta
                   - block.inFlowSouth() );                
    } else {
      vel.y = (data[iy+1][ix] - data[iy-1][ix])/(2*delta);
    }    
    
  } // getVelocity()

  // set the desired solution level uniformly across the grid
  public void resetDesiredSolutionLevel(int level) {
    
    assert( level >= 0 && level <= mRefineLevel );

    for ( int ky = 0 ; ky < mBaseYSize ; ky++ ) {
      for ( int kx = 0 ; kx < mBaseXSize ; kx++ ) {
        mBaseBlocks[ky][kx].setDesiredSolutionLevel(level);
      }
    }
    
  } // resetDesiredSolutionLevel()
  
  // specify the desired solution level for a block and its neighbours
  // (the maximum level should be used wherever we want velocity values)
  public void setDesiredSolutionLevel(int xBlock, int yBlock, int level) {

    assert( xBlock >= 0 && xBlock < mBaseXSize );
    assert( yBlock >= 0 && yBlock < mBaseYSize );
    assert( level >= 0 && level <= mRefineLevel );

    FlowBlock block = mBaseBlocks[yBlock][xBlock];
    block.setMinDesiredSolutionLevel(level);
    
    if ( !block.boundaryEast() ) {
      mBaseBlocks[yBlock][xBlock+1].setMinDesiredSolutionLevel(level);
    }
    if ( !block.boundaryWest() ) {
      mBaseBlocks[yBlock][xBlock-1].setMinDesiredSolutionLevel(level);
    }
    if ( !block.boundaryNorth() ) {
      mBaseBlocks[yBlock-1][xBlock].setMinDesiredSolutionLevel(level);
    }
    if ( !block.boundarySouth() ) {
      mBaseBlocks[yBlock+1][xBlock].setMinDesiredSolutionLevel(level);
    }
          
  } // setDesiredSolutionLevel()
  
} // class Flow

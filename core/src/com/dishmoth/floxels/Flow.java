/*
 *  Flow.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// class for generating flow field
public class Flow {

  // simple class for returning velocity components
  static public class Vel {
    public float x=0.0f, y=0.0f;
  } // class Flow.Vel

  // value in the mBaseWalls array that indicates an opening
  static public final float OPEN = Float.MAX_VALUE;

  // the base grid (no refinement)
  private final int mBaseXSize,
                    mBaseYSize;

  // the boundaries of the base grid, and the velocity conditions there
  // [y][x][direc] = in-flow value (boundary) or OPEN (no boundary)
  // (in-flow is velocity of fluid coming in through the boundary)
  private float mBaseWalls[][][];

  // how refined the solution needs to be on the base grid
  // (an optimization if a quality solution is only needed in some regions) 
  private int mDesiredSolutionLevel[][];
  
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
    
    mBaseWalls = new float[mBaseYSize][mBaseXSize][4];
    for ( int iy = 0 ; iy < mBaseYSize ; iy++ ) {
      for ( int ix = 0 ; ix < mBaseXSize ; ix++ ) {
        for ( int d = 0 ; d < 4 ; d++ ) mBaseWalls[iy][ix][d] = OPEN;
      }
    }    

    mRefineLevel = refineLevel;
    mRefineFactor = ( 1 << mRefineLevel );
    
    mDesiredSolutionLevel = new int[mBaseYSize][mBaseXSize];
    resetDesiredSolutionLevel(mRefineLevel);
    
    mTopGrid = new FlowGrid(this, mRefineLevel);
    
  } // constructor

  // access to wall layout and in-flow values
  public float[][][] walls() { return mBaseWalls; }

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
      for ( int kx = 0, len = source[ky].length ; kx < len ; kx++ ) {
        source[ky][kx] = 0.0f;
      }
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
    float walls[] = mBaseWalls[ky][kx];
    
    float data[][] = mTopGrid.data();
    final float delta = 1.0f/mRefineFactor;
    
    final int iy0 = (ky << mRefineLevel),
              iy1 = iy0 + mRefineFactor-1,
              ix0 = (kx << mRefineLevel),
              ix1 = ix0 + mRefineFactor-1;
        
    if ( ix == ix0 && walls[Env.WEST] != OPEN ) {
      vel.x = 0.5f*((data[iy][ix+1] - data[iy][ix])/delta + walls[Env.WEST]);
    } else if ( ix == ix1 && walls[Env.EAST] != OPEN ) {
      vel.x = 0.5f*((data[iy][ix] - data[iy][ix-1])/delta - walls[Env.EAST]);                
    } else {
      vel.x = (data[iy][ix+1] - data[iy][ix-1])/(2*delta);
    }
    
    if ( iy == iy0 && walls[Env.NORTH] != OPEN ) {
      vel.y = 0.5f*((data[iy+1][ix] - data[iy][ix])/delta + walls[Env.NORTH]);
    } else if ( iy == iy1 && walls[Env.SOUTH] != OPEN ) {
      vel.y = 0.5f*((data[iy][ix] - data[iy-1][ix])/delta - walls[Env.SOUTH]);                
    } else {
      vel.y = (data[iy+1][ix] - data[iy-1][ix])/(2*delta);
    }    
    
  } // getVelocity()

  // access to the desired solution levels
  public int[][] desiredSolutionLevel() { return mDesiredSolutionLevel; }
  
  // set the desired solution level uniformly across the grid
  public void resetDesiredSolutionLevel(int level) {
    
    for ( int ky = 0 ; ky < mBaseYSize ; ky++ ) {
      for ( int kx = 0 ; kx < mBaseXSize ; kx++ ) {
        mDesiredSolutionLevel[ky][kx] = level;
      }
    }
    
  } // resetDesiredSolutionLevel()
  
  // specify the desired solution level for a block and its neighbours
  // (the maximum level should be used wherever we want velocity values)
  public void setDesiredSolutionLevel(int xBlock, int yBlock, int level) {

    assert( xBlock >= 0 && xBlock < mBaseXSize );
    assert( yBlock >= 0 && yBlock < mBaseYSize );
    assert( level >= 0 && level <= mRefineLevel );

    setMinDesiredSolutionLevel(xBlock, yBlock, level);

    float walls[] = mBaseWalls[yBlock][xBlock];
    if ( walls[Env.EAST] == OPEN ) {
      setMinDesiredSolutionLevel(xBlock+1, yBlock, level);
    }
    if ( walls[Env.WEST] == OPEN ) {
      setMinDesiredSolutionLevel(xBlock-1, yBlock, level);
    }
    if ( walls[Env.NORTH] == OPEN ) {
      setMinDesiredSolutionLevel(xBlock, yBlock-1, level);
    }
    if ( walls[Env.SOUTH] == OPEN ) {
      setMinDesiredSolutionLevel(xBlock, yBlock+1, level);
    }
          
  } // setDesiredSolutionLevel()
  
  // specify the desired solution level for a block
  private void setMinDesiredSolutionLevel(int xBlock, int yBlock, int level) {
    
    if ( mDesiredSolutionLevel[yBlock][xBlock] < level ) {
      mDesiredSolutionLevel[yBlock][xBlock] = level;
    }
    
  } // setMinDesiredSolutionLevel()
  
} // class Flow

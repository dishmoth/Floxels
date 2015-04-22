/*
 *  Maze.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.Collections;
import java.util.LinkedList;

// simple structure for holding a grid of walls
public class Maze {

  // a single difference between two mazes
  public static class Delta {
    public boolean mHoriz;
    public int     mXPos, 
                   mYPos;
    public Delta(boolean h, int ix, int iy) { mHoriz=h; mXPos=ix; mYPos=iy; }
  } // class Maze.Delta
  
  // size of the maze
  final private int mNumTilesX,
                    mNumTilesY;
  
  // where the walls are
  final private boolean mHorizWalls[][],
                        mVertWalls[][]; 
  
  // constructor
  public Maze(String data[]) {

    assert( data != null );
    assert( data[0] != null );
    
    mNumTilesY = data.length - 1;
    assert( mNumTilesY > 0 );

    mNumTilesX = (data[0].length() - 1)/2;
    assert( 2*mNumTilesX + 1 == data[0].length() );

    mHorizWalls = new boolean[mNumTilesY+1][mNumTilesX];
    mVertWalls = new boolean[mNumTilesY][mNumTilesX+1];
    
    for ( int iy = 0 ; iy < mNumTilesY+1 ; iy++ ) {
      assert( data[iy] != null && data[iy].length() == 2*mNumTilesX+1 );
      for ( int ix = 0 ; ix < mNumTilesX ; ix++ ) {
        char ch = data[iy].charAt(2*ix+1);
        assert( ch == ' ' || ch == '_' );
        mHorizWalls[iy][ix] = (ch == '_');
      }
    }
    
    for ( int iy = 0 ; iy < mNumTilesY ; iy++ ) {
      for ( int ix = 0 ; ix < mNumTilesX+1 ; ix++ ) {
        char ch = data[iy+1].charAt(2*ix);
        assert( ch == ' ' || ch == 'I' );
        mVertWalls[iy][ix] = (ch == 'I');
      }
    }
    
  } // constructor

  // size of the maze
  public int numTilesX() { return mNumTilesX; }
  public int numTilesY() { return mNumTilesY; }
  
  // is there an ix'th wall section on the iy'th row 
  public boolean horizWall(int ix, int iy) {
    
    if ( ix < 0 || ix >= mNumTilesX ||
         iy < 0 || iy >  mNumTilesY ) return false;
    return mHorizWalls[iy][ix];
    
  } // horizWall()
  
  // is there an iy'th wall section on the ix'th column 
  public boolean vertWall(int ix, int iy) {
    
    if ( ix < 0 || ix >  mNumTilesX ||
         iy < 0 || iy >= mNumTilesY ) return false;
    return mVertWalls[iy][ix];
    
  } // vertWall()

  // list the differences between this maze and another
  public void collectDifferences(Maze other, LinkedList<Delta> deltas) {

    for ( int iy = 0 ; iy < numTilesY()+1 ; iy++ ) {
      for ( int ix = 0 ; ix < numTilesX()+1 ; ix++ ) {
        if ( iy < numTilesY() && 
             mVertWalls[iy][ix] != other.mVertWalls[iy][ix] ) {
          deltas.add( new Delta(false, ix, iy) );
        }
        if ( ix < numTilesX() && 
             mHorizWalls[iy][ix] != other.mHorizWalls[iy][ix] ) {
          deltas.add( new Delta(true, ix, iy) );
        }
      }
    }
    Collections.shuffle(deltas);
  
  } // collectDifferences()

  // make one change to the maze
  public void applyDifference(Delta delta) {
    
    final int ix = delta.mXPos,
              iy = delta.mYPos;
    if ( delta.mHoriz )  mHorizWalls[iy][ix] = !mHorizWalls[iy][ix];
    else                 mVertWalls[iy][ix] = !mVertWalls[iy][ix];      
    
  } // applyDifference()
  
} // class Maze

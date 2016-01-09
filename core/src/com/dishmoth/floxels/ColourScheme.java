/*
 *  ColourScheme.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// class for keeping track of floxel colours
public class ColourScheme {

  // different base colours (red, green, blue from 0 to 255)
  static private final int kColours[][] = { { 255, 100,  89 },   // red
                                            { 255, 170,  50 },   // orange
                                            { 229, 229,  22 },   // yellow
                                            {  80, 240,  80 },   // green
                                            {  87, 219, 219 },   // cyan
                                            { 120, 120, 255 },   // blue
                                            { 166,  86, 247 },   // purple
                                            { 250, 110, 180 } }; // pink
  
  // names of the populations
  static private final String kNames[] = { "Reds",
                                           "Oranges",
                                           "Yellows",
                                           "Greens",
                                           "Teals",
                                           "Blues",
                                           "Purples",
                                           "Pinks" };
  
  // access the different colours
  static public int num() { return kColours.length; }
  static public int[] colour(int index) { return kColours[index]; }
  static public String name(int index) { return kNames[index]; }
  
  // colours of the floxel populations
  private int mNewIndex,
              mOldIndex,
              mOldOldIndex;
  
  // generate a simple pattern
  private int     mCount;
  private boolean mReverse;
  
  // constructor
  public ColourScheme() {
    
    mNewIndex = 2;
    mOldIndex = -1;
    mOldOldIndex = -1;
    
    mCount = 0;
    mReverse = false;
    
  } // constructor
  
  // colour index for the majority population
  public int oldIndex() {
    
    assert( mOldIndex >= 0 && mOldIndex < kColours.length );
    return mOldIndex;
    
  } // oldIndex()
  
  // colour index for the minority population
  public int newIndex() {
    
    assert( mNewIndex >= 0 && mNewIndex < kColours.length );
    return mNewIndex;
    
  } // newIndex()
  
  // change the colours in a random-ish way
  public void advance() {
    
    final int steps[] = { +3, +4, +2 };
    int step = steps[ mCount % steps.length ];
    if ( mReverse ) step = -step;
    
    mOldOldIndex = mOldIndex;
    mOldIndex = mNewIndex;
    mNewIndex = Env.fold(mNewIndex+step, kColours.length);
    
    mCount += 1;
    if ( (mCount+1)%24 == 0 ) mReverse = !mReverse;

    int oldColourAbove = Env.fold(mOldIndex+1, kColours.length),
        oldColourBelow = Env.fold(mOldIndex-1, kColours.length);
    assert( mNewIndex != mOldIndex );
    assert( mNewIndex != oldColourAbove );
    assert( mNewIndex != oldColourBelow );
    assert( mNewIndex != mOldOldIndex );
    
  } // advance()

} // class ColourScheme

/*
 *  Mazes.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// collection of maze data
public class Mazes {

  static private final String kMazeData10x10[][] = {

        // maze 0
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _ _    I  _    I",
          "I   I  _ _I_ _ _ _I I",
          "I I I  _  I  _    I I",
          "I I I I  _I_  I I I I",
          "I  _I I I  _ _ _I  _I",
          "I I    _I I  _  I_  I",
          "I_  I_ _ _I   I I   I",
          "I    _ _  I I I I I I",
          "I I_ _    I I I I_  I",
          "I_ _ _ _I_ _ _I_ _ _I" },

        // maze 1
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _    I  _ _ _  I",
          "I I  _ _I I I   I  _I",
          "I I I  _ _I_ _I I_  I",
          "I  _I_    I  _ _I  _I",
          "I_  I  _I  _ _  I_  I",
          "I  _I   I_ _      I I",
          "I   I I  _ _  I I I I",
          "I I I I I   I_ _I I I",
          "I I_ _I I I_ _ _ _I I",
          "I_ _ _ _I_ _ _ _ _ _I" },

        // maze 2
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _  I    _ _    I",
          "I   I     I_     _I I",
          "I I   I   I  _ _  I I",
          "I I   I I  _ _     _I",
          "I  _I_  I      _ _  I",
          "I_     _ _  I   I   I",
          "I    _ _    I I   I I",
          "I I_     _I   I   I I",
          "I I  _ _  I    _I_  I",
          "I_ _ _ _ _ _I_ _ _ _I" },

        // maze 3
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I     I  _ _        I",
          "I  _I  _     _I I_  I",
          "I  _ _I  _ _  I_   _I",
          "I   I     I     I   I",
          "I  _ _  I   I  _ _  I",
          "I       I   I       I",
          "I_  I_   _I_   _I_  I",
          "I  _  I_     _I  _  I",
          "I   I I  _ _    I   I",
          "I_ _ _ _ _ _ _I_ _ _I" },

        // maze 4
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _    I  _   _  I",
          "I I     I_  I     I I",
          "I      _I   I     I I",
          "I I_ _    I   I_ _  I",
          "I_     _ _I_ _  I  _I",
          "I  _I_    I    _ _  I",
          "I     I   I  _    I I",
          "I I     I  _I       I",
          "I I_   _I   I  _ _I I",
          "I_ _ _ _ _I_ _ _ _ _I" },

        // maze 5
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _ _ _     _ _  I",
          "I I        _I_    I I",
          "I I   I  _ _ _ _  I I",
          "I  _I I I    _    I I",
          "I   I I       I   I I",
          "I I   I_      I I_  I",
          "I I  _ _ _ _I I I   I",
          "I I    _ _    I   I I",
          "I I_ _  I  _ _ _ _I I",
          "I_ _ _ _ _ _ _ _ _ _I" }
          
        };

  static private final String kMazeData11x9[][] = {

        // maze 0
        { " _ _ _ _ _ _ _ _ _ ",
          "I  _ _    I  _    I",
          "I   I  _I_ _ _ _I I",
          "I I I   I  _    I I",
          "I I I I I_  I I I I",
          "I  _I I  _ _ _I  _I",
          "I I  _ _I  _  I_  I",
          "I I I   I_  I I   I",
          "I_  I I I   I I I I",
          "I   I I I I I I I I",
          "I I_ _I   I I I_  I",
          "I_ _ _ _I_ _I_ _ _I" }
        
        };

  static private final String kMazeData12x8[][] = {

        // maze 0
        { " _ _ _ _ _ _ _ _ ",
          "I  _ _  I  _    I",
          "I I   I_ _ _ _I I",
          "I I I  _      I I",
          "I I I_ _ _I I  _I",
          "I I I  _ _ _I   I",
          "I I I I  _  I I I",
          "I I I_    I I_  I",
          "I I_  I I I_  I I",
          "I_   _I I I  _I I",
          "I  _  I I I I  _I",
          "I  _ _I_  I I_  I",
          "I_ _ _ _ _I_ _ _I" }
        
        };

  // return a maze object for the specified index
  static public Maze get(int index) {

    String mazeData[][] = null;
    boolean flipXY = false;
    
    if ( Env.numTilesX() == 10 && Env.numTilesY() == 10 ) {
      mazeData = kMazeData10x10;
    } else if ( Env.numTilesX() == 9 && Env.numTilesY() == 11 ) {
      mazeData = kMazeData11x9;
    } else if ( Env.numTilesX() == 11 && Env.numTilesY() == 9 ) {
      mazeData = kMazeData11x9;
      flipXY = true;
    } else if ( Env.numTilesX() == 8 && Env.numTilesY() == 12 ) {
      mazeData = kMazeData12x8;
    } else if ( Env.numTilesX() == 12 && Env.numTilesY() == 8 ) {
      mazeData = kMazeData12x8;
      flipXY = true;
    }
    assert( mazeData != null );

    boolean flipVert  = true,
            flipHoriz = false;

    index = Env.fold(index, mazeData.length);
    return new Maze( mazeData[index], flipXY, flipVert, flipHoriz );
    
  } // get()
  
} // class Mazes

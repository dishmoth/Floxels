/*
 *  Mazes.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

// collection of maze data
public class Mazes {

  static private final String kMazeData[][] = {

        // maze 0
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I    _ _  I  _ _ _  I",
          "I I  _ _ _   _ _  I I",
          "I I I  _     _    I I",
          "I     I  _    I     I",
          "I_  I   I   I   I  _I",
          "I   I   I  _I   I   I",
          "I     I_     _I     I",
          "I I  _ _   _ _ _I I I",
          "I I_ _ _     _ _  I I",
          "I_ _ _ _ _I_ _ _ _ _I" },

        // maze 1
        { " _ _ _ _ _ _ _ _ _ _ ",
          "I  _ _         _ _  I",
          "I I    _I I I_    I I",
          "I I       I       I I",
          "I  _I     I     I_  I",
          "I_   _ _I   I_ _   _I",
          "I  _    I   I    _  I",
          "I   I     I     I   I",
          "I I    _  I  _    I I",
          "I I_ _  I I I  _ _I I",
          "I_ _ _ _ _ _ _ _ _ _I" },

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

  // return a maze object for the specified index
  static public Maze get(int index) {
    
    index = Env.fold(index, kMazeData.length);
    return new Maze( kMazeData[index] );
    
  } // get()
  
} // class Mazes

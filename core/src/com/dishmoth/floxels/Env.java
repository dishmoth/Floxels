/*
 *  Env.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.*;

import com.badlogic.gdx.Gdx;

public class Env {

  // enumeration of directions
  static public final int NORTH = 0,
                          SOUTH = 1,
                          EAST  = 2,
                          WEST  = 3;
  
  // size of a square tile
  static private int kTileWidth = 58;
  
  // number of tiles in the game area
  static private int kNumTilesX = 0,
                     kNumTilesY = 0;
  
  // target frame rate
  static public final int   TICKS_PER_SEC = 30;
  static public final float TICK_TIME     = 1.0f/TICKS_PER_SEC;

  // frame rate to display (percentage of required rate)
  static private int kFrameRate = 0;
  
  // label to use for debug logging
  private static final String kLogTag = "Floxels";
  
  // whether to display debug messages
  static private boolean kDebugMode = true;
  
  // assorted helper objects
  static private Random       kRandom;
  static private MouseMonitor kMouseMonitor;
  static private Sounds       kSounds;
  static private Painter      kPainter;
  
  // this sets up a global Env for the applet
  static public void initialize() {
  
    kRandom       = new Random();
    kMouseMonitor = new MouseMonitor();
    kSounds       = new Sounds();
    kPainter      = new Painter();
    
  } // initialize()

  // whether debug messages are displayed
  static public boolean debugMode() { return kDebugMode; }

  // display debug text
  static public void debug(String message) { 
    
    if ( kDebugMode ) Gdx.app.log(kLogTag, message);
    
  } // debug()
  
  // display debug text for an exception
  static public void debug(String message, Exception ex) { 
    
    if ( kDebugMode ) Gdx.app.log(kLogTag, message, ex);
    
  } // debug()
  
  // the size of the game area in tiles
  static public void setTilesXY(int x, int y) { kNumTilesX=x; kNumTilesY=y; }
  static public int numTilesX() { return kNumTilesX; }
  static public int numTilesY() { return kNumTilesY; }
  
  // the size of a tile (in pixels)
  static public void setTileWidth(int w) {
    kTileWidth = w;
    kPainter.prepare(w);
  }
  static public int tileWidth() { return kTileWidth; }

  // return the size of the game area in pixels
  static public int gameWidth()  { return kTileWidth*kNumTilesX; }
  static public int gameHeight() { return kTileWidth*kNumTilesY; }
  
  // return the top-left corner of the game area
  static public int gameOffsetX() { return (Gdx.graphics.getWidth()-gameWidth())/2; }
  static public int gameOffsetY() { return (Gdx.graphics.getHeight()-gameHeight())/2; }

  // measured frame rate
  static public void setFrameRate(int percentage) { kFrameRate = percentage; }
  static public int frameRate() { return kFrameRate; }
  
  // return reference to mouse monitor
  static public MouseMonitor mouse() { return kMouseMonitor; }
  
  // return reference to game audio
  static public Sounds sounds() { return kSounds; }

  //
  static public Painter painter() { return kPainter; }
  
  // end the game, close the window (could be done better?)
  static public void exitGame() {
    
    //if ( kOwner != null && kOwner instanceof MainWindow ) {
    //  ((MainWindow)kOwner).exit();
    //} else {
      System.exit(0);
    //}
    
  } // exitGame()
  
  // assorted functions for returning random numbers
  static public float randomFloat() { // in range [0,1]
    return kRandom.nextFloat(); 
  } // randomFloat()
  static public float randomFloat(float a, float b) { // in range [a,b] 
    return ( a + (b-a)*kRandom.nextFloat() ); 
  } // randomFloat()
  static public double randomDouble() { // in range [0,1]
    return kRandom.nextDouble(); 
  } // randomDouble()
  static public double randomDouble(double a, double b) { // in range [a,b] 
    return ( a + (b-a)*kRandom.nextDouble() ); 
  } // randomDouble()
  static public int randomInt(int n) { // in range [0,n-1] 
    return kRandom.nextInt(n); 
  } // randomInt()
  static public int randomInt(int a, int b) { // in range [a,b]
    if ( a > b )      return ( b + kRandom.nextInt(a-b+1) );
    else if ( a < b ) return ( a + kRandom.nextInt(b-a+1) );
    else              return a;
  } // randomInt()
  static public boolean randomBoolean() { 
    return kRandom.nextBoolean(); 
  } // randomBoolean()

  // assorted modulo-type functions
  static public int fold(int a, int b) {
    // result is between 0 and (b-1)
    if ( a >= 0 ) return (a%b);
    else {
      int temp = b + (a%b);
      return ( (temp==b) ? 0 : temp );
    }
  } // fold()
  static public double fold(double a, double b) {
    // result is in interval [0,b)
    // (probably a more efficient way of doing this?)
    return ( a - b*Math.floor(a/b) );
  } // fold()
  static public float fold(float a, float b) {
    // result is in interval [0,b)
    // (probably a more efficient way of doing this?)
    return ( a - b*(float)Math.floor(a/b) );
  } // fold()
  
  // more modulo-type functions
  static public float foldNearTo(float a, float target, float modSize) {
    return ( target + fold(a-target+0.5f*modSize, modSize) - 0.5f*modSize );
  } // foldNearTo()
  
} // class Env

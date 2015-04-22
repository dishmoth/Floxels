/*
 *  Env.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Env {

  static private Component kOwner;

  // enumeration of directions (used in various places)
  static public final int kDirectionUp    = 0,
                          kDirectionDown  = 1,
                          kDirectionLeft  = 2,
                          kDirectionRight = 3;
  
  // size of a square tile
  static private final int kTileWidth = 58;
  
  // number of tiles in the game area
  static private final int kNumTilesX = 10,
                           kNumTilesY = 10;
  
  // size of the full game canvas
  static private int kScreenWidth, 
                     kScreenHeight;
  
  // frame rate
  static private final int kTicksPerSecond = 30;

  // whether to display debug messages, timing statistics
  static private boolean kDebugMode        = false,
                         kPerformanceStats = false;
  
  // assorted helper objects
  static private Random       kRandom;
  static private MouseMonitor kMouseMonitor;
  static private Resources    kResources;
  static private Sounds       kSounds;
  
  // some graphics objects
  static private GraphicsEnvironment   kGraphicsEnvironment;
  static private GraphicsDevice        kGraphicsDevice;
  static private GraphicsConfiguration kGraphicsConfiguration;
  
  // this sets up a global Env for the applet
  static public void initialize(Component owner, 
                                int screenWidth, int screenHeight) {
  
    kOwner = owner;

    assert( screenWidth > 0 && screenHeight > 0 );
    kScreenWidth  = screenWidth;
    kScreenHeight = screenHeight;
    
    kRandom       = new Random();
    kMouseMonitor = new MouseMonitor();
    kResources    = new Resources();
    kSounds       = new Sounds();

    kGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    kGraphicsDevice = kGraphicsEnvironment.getDefaultScreenDevice();
    kGraphicsConfiguration = owner.getGraphicsConfiguration();
    
  } // initialize()

  // whether debug messages, timing statistics should be displayed
  static public boolean debugMode() { return kDebugMode; }
  static public boolean performanceStats() { return kPerformanceStats; }
  static public void setVerbose() { kDebugMode = kPerformanceStats = true; }

  // return the dimensions of a tile
  static public int tileWidth() { return kTileWidth; }

  // return the size of the game area in tiles
  static public int numTilesX() { return kNumTilesX; }
  static public int numTilesY() { return kNumTilesY; }
  
  // return the size of the game area in pixels
  static public int gameWidth()  { return kTileWidth*kNumTilesX; }
  static public int gameHeight() { return kTileWidth*kNumTilesY; }
  
  // return the top-left corner of the game area
  static public int gameOffsetX() { return (screenWidth()-gameWidth())/2; }
  static public int gameOffsetY() { return (screenHeight()-gameHeight())/2; }
  
  // return dimensions of full game canvas
  static public int screenWidth()  { return kScreenWidth; }
  static public int screenHeight() { return kScreenHeight; }

  // frame rate
  static public int ticksPerSecond() { return kTicksPerSecond; }
  
  // return reference to mouse monitor
  static public MouseMonitor mouse() { return kMouseMonitor; }
  
  // return reference to game resources
  static public Resources resources() { return kResources; }
  
  // return reference to game audio
  static public Sounds sounds() { return kSounds; }

  // return a non-transparent buffered image suited to the graphics device
  static public BufferedImage createOpaqueImage(int width, int height) {
    
    assert( kGraphicsConfiguration != null );
    return kGraphicsConfiguration.createCompatibleImage(width, height,
                                                        Transparency.OPAQUE);
    
  } // createOpaqueImage()
  
  // return a transparent buffered image suited to the graphics device
  static public BufferedImage createTranslucentImage(int width, int height) {
    
    assert( kGraphicsConfiguration != null );
    return kGraphicsConfiguration.createCompatibleImage(width, height,
                                                    Transparency.TRANSLUCENT);
    
  } // createTranslucentImage()
  
  // comment on available accelerated memory
  static public void reportAcceleratedMemory() {

    if ( !kPerformanceStats || kGraphicsDevice == null ) return;
    int am = kGraphicsDevice.getAvailableAcceleratedMemory();
    System.out.println("accelerated memory: "  + (am/1024) + "K");
    
  } // reportAcceleratedMemory();
  
  // called when a break in the action (to collect garbage) won't be noticed
  static public void naturalBreak() { System.gc(); }
  
  // end the game, close the window (could be done better?)
  static public void exitGame() {
    
    if ( kOwner != null && kOwner instanceof MainWindow ) {
      ((MainWindow)kOwner).exit();
    } else {
      System.exit(0);
    }
    
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

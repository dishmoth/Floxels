/*
 *  Fonts.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// class for holding different bitmap fonts
public class Fonts {

  // available font sizes
  private static final int kFontSizes[] = { 7, 8, 9, 
                                            10, 11, 12, 14, 16, 18,
                                            20, 30 };
  
  // relative size of the fonts
  private static final float kSmallFontScale = 0.20f;
  
  // font objects
  private BitmapFont mSmallFont;
  
  // constructor
  public Fonts(int tileWidth) {
    
    float smallFontSize = kSmallFontScale*tileWidth;
    mSmallFont = pickNearestFont(smallFontSize);
    
  } // constructor
  
  // choose the nearest font to the requested size
  private BitmapFont pickNearestFont(float size) {
    
    int bestSize = -1;
    float bestDelta = Float.MAX_VALUE;
    for ( int k = 0 ; k < kFontSizes.length ; k++ ) {
      float delta = Math.abs( size - kFontSizes[k] );
      if ( delta < bestDelta ) {
        bestDelta = delta;
        bestSize = kFontSizes[k];
      }
    }
    
    String fileName = "calibri" + bestSize  + ".fnt";
    Env.debug("Using font: " + fileName);
    return new BitmapFont(Gdx.files.internal(fileName));
    
  } // pickNearestFont();
  
  // access to fonts
  public BitmapFont smallFont() { return mSmallFont; } 
  
} // class Fonts

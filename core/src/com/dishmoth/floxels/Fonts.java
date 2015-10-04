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
  private static final int kFontSizes[] = { 8, 12, 30 };
  
  // relative size of the fonts
  private static final float kSmallFontScale = 12/66.0f;
  
  // font objects
  private BitmapFont mSmallFont;
  
  // constructor
  public Fonts(int tileWidth) {
    
    int smallFontSize = Math.round(kSmallFontScale*tileWidth);
    mSmallFont = pickNearestFont(smallFontSize);
    
  } // constructor
  
  // choose the nearest font to the requested size
  private BitmapFont pickNearestFont(int size) {
    
    int bestSize = -1;
    int bestDelta = Integer.MAX_VALUE;
    for ( int k = 0 ; k < kFontSizes.length ; k++ ) {
      int delta = Math.abs( size - kFontSizes[k] );
      if ( delta < bestDelta ) {
        bestDelta = delta;
        bestSize = kFontSizes[k];
      }
    }
    
    String fileName = "calibri" + bestSize  + ".fnt"; 
    return new BitmapFont(Gdx.files.internal(fileName));
    
  } // pickNearestFont();
  
  // access to fonts
  public BitmapFont smallFont() { return mSmallFont; } 
  
} // class Fonts

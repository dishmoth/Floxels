/*
 *  Fonts.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// class for holding different bitmap fonts
public class Fonts {

  // file naming
  private static final String kFontName      = "calibri",
                              kFontNameLarge = "calibri_light";
  private static final int    kFontSizeLarge = 25;
  
  // available font sizes
  private static final int kFontSizes[] = { 7, 8, 9, 
                                            10, 11, 12, 14, 16, 18,
                                            20, 22, 24, 27,
                                            30, 33, 36,
                                            40, 44, 48,
                                            52, 56,
                                            60, 65,
                                            70,
                                            80 };
  
  // relative size of the fonts
  private static final float kSmallFontScale  = 0.20f,
                             kMediumFontScale = 0.40f;
  
  // font objects
  private BitmapFont mSmallFont,
                     mMediumFont;

  // constructor
  public Fonts(int tileWidth) {
    
    float smallFontSize = kSmallFontScale*tileWidth;
    mSmallFont = pickNearestFont(smallFontSize);
    
    float mediumFontSize = kMediumFontScale*tileWidth;
    mMediumFont = pickNearestFont(mediumFontSize);
    
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

    String fname = ( bestSize >= kFontSizeLarge ? kFontNameLarge : kFontName );
    String fileName = fname + bestSize  + ".fnt";
    BitmapFont font = new BitmapFont(Gdx.files.internal(fileName));
    if ( size > 1.2f*kFontSizes[kFontSizes.length-1] ) {
      float scale = size/(float)bestSize;
      font.getRegion().getTexture().setFilter(TextureFilter.Linear,
                                              TextureFilter.Linear);
      font.getData().setScale(scale);
      font.setUseIntegerPositions(false);
      Env.debug("Using font: " + fileName + " (x" +
                Env.decimalPlaces(scale, 2) + ")");
    } else {
      Env.debug("Using font: " + fileName);
    }
    return font;
    
  } // pickNearestFont();
  
  // access to fonts
  public BitmapFont smallFont()  { return mSmallFont; } 
  public BitmapFont mediumFont() { return mMediumFont; } 
  
} // class Fonts

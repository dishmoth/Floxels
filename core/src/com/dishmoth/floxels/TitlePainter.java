/*
 *  TitlePainter.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// class for keeping track of the image for the title screen
public class TitlePainter {

  // different region positions and sizes
  private static final int kSizes[][] = { { 293, 175,  98,  28 },
                                          {   1, 200, 115,  33 },
                                          { 117, 200, 148,  43 },
                                          { 293, 118, 195,  56 },
                                          {   1, 118, 291,  81 },
                                          {   1,   1, 428, 116 } };
  
  // the texture data
  private Texture mTexture;
  
  // texture data split into regions
  private TextureRegion mRegions[];
  
  // constructor
  public TitlePainter() {
    
    mTexture = new Texture(Gdx.files.internal("Title.png"));
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    mRegions = new TextureRegion[ kSizes.length ];
    for ( int k = 0 ; k < kSizes.length ; k++ ) {
      int size[] = kSizes[k];
      mRegions[k] = new TextureRegion(mTexture,
                                      size[0], size[1], 
                                      size[2], size[3]);
    }

  } // constructor

  // return a texture region close to the target width
  public TextureRegion region(int targetWidth) {
    
    int best = -1;
    if ( targetWidth <= kSizes[0][2] ) {
      best = 0;
    } else if ( targetWidth >= kSizes[kSizes.length-1][2] ) {
      best = kSizes.length-1;
    } else {
      for ( int k = 0 ; k < kSizes.length-1 ; k++ ) {
        int width0 = kSizes[k][2],
            width1 = kSizes[k+1][2];
        assert( width0 < width1 );
        if ( width0 <= targetWidth && targetWidth <= width1 ) {
          final float frac = 0.1f; 
          float midWidth = (1-frac)*width0 + frac*width1;
          if ( targetWidth <= midWidth ) best = k;
          else                           best = k+1;
          break;
        }
      }
    }
      
    Env.debug("Title width: " + kSizes[best][2] 
              + " pixels (target " + targetWidth + ")");
    return mRegions[best];
    
  } // region()
  
} // class TitlePainter

/*
 *  BackgroundPainter.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

// texture for the background image 
public class BackgroundPainter {

  // range of background colours (r,g,b out of 255)
  private static final int kLightColour[] = { 189, 188, 141 },
                           kDarkColour[]  = { 175, 173, 126 };

  // details of textured tile image for the background
  private static final int   kNoiseImageSize  = 256,
                             kNoiseSize       = 8;
  private static final float kGradientDeltaX  = -0.3f,
                             kGradientDeltaY  = -0.5f;
  private static final float kLightCutoff     = 5.0f,
                             kDarkCutoff      = -2.0f;
  
  // textured background tile
  private Pixmap  mNoiseImage;
  private Texture mNoiseTexture;
  
  // constructor
  public BackgroundPainter() {

    mNoiseImage = null;
    mNoiseTexture = null;
    
    makeNoise();
    makeTexture();
    
  } // constructor

  // simple fractal noise sum
  static private float calcNoise(Perlin2D noise, float x, float y) {
    
    final float x1 = 2*x + 0.213f,
                y1 = 2*y + 0.312f;
    return noise.value(x,y) + 0.8f*noise.value(x1,y1);
    
  } // calcNoise()
  
  // build a noisy background image
  private void makeNoise() {

    assert( mNoiseImage == null );
    
    final long seed = 1; 
    Perlin2D noise = new Perlin2D(seed, kNoiseSize);
    mNoiseImage = new Pixmap(kNoiseImageSize, kNoiseImageSize,
                             Pixmap.Format.RGB888);
    
    final int rL = kLightColour[0],
              gL = kLightColour[1],
              bL = kLightColour[2];
    final int rD = kDarkColour[0],
              gD = kDarkColour[1],
              bD = kDarkColour[2];

    final float scale = kNoiseSize/(float)kNoiseImageSize;
    final float deltaX = kGradientDeltaX*scale,
                deltaY = kGradientDeltaY*scale;
    final float deltaLen = (float)Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    for ( int iy = 0 ; iy < kNoiseImageSize ; iy++ ) {
      for ( int ix = 0 ; ix < kNoiseImageSize ; ix++ ) {
        final float x  = ix*scale,
                    y  = iy*scale;
        final float f1 = calcNoise(noise, x+deltaX, y+deltaY),
                    f0 = calcNoise(noise, x, y);
        final float df = (f1 - f0)/deltaLen;
        final float h = Math.min(1.0f, Math.max(0.0f, 
                          (df - kDarkCutoff)/(kLightCutoff - kDarkCutoff)));
        final int r = Math.round( h*rL + (1-h)*rD ),
                  g = Math.round( h*gL + (1-h)*gD ),
                  b = Math.round( h*bL + (1-h)*bD );
        final int rgba = (r<<24) + (g<<16) + (b<<8) + 255;
        mNoiseImage.drawPixel(ix, iy, rgba);
      }
    }
    
  } // makeNoise()
  
  // construct the texture
  private void makeTexture() {
    
    assert( mNoiseTexture == null );
    mNoiseTexture = new Texture(mNoiseImage);
    mNoiseTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    mNoiseTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    
  } // makeTexture()
  
  // access to the texture
  public Texture texture() { return mNoiseTexture; } 
  
  // rebuild unmanaged texture when the game is resumed
  public void resetTexture() {
    
    if ( mNoiseTexture != null ) {
      mNoiseTexture.dispose();
      mNoiseTexture = null;
    }
    makeTexture();
    
  } // resetTexture()
  
} // class BackgroundPainter

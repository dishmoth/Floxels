/*
 *  Background.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// static image of the maze and its background 
public class Background extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 0;
  
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
  
  // how much the noise is stretched to cover the game screen
  private static final float kNoiseRepeats = 2.3f;
  
  // textured background tile
  private static Pixmap  kNoiseImage   = null;
  private static Texture kNoiseTexture = null;
  
  // reference to the maze object
  private Maze mMaze;
  
  // constructor
  public Background(Maze maze) {
    
    super(kScreenLayer);
    
    mMaze = maze;
    
    if ( kNoiseImage == null ) makeNoise();
    
  } // constructor

  // simple fractal noise sum
  static private float calcNoise(Perlin2D noise, float x, float y) {
    
    final float x1 = 2*x + 0.213f,
                y1 = 2*y + 0.312f;
    return noise.value(x,y) + 0.8f*noise.value(x1,y1);
    
  } // calcNoise()
  
  // build a noisy background image
  static private void makeNoise() {
    
    final long seed = 1; 
    Perlin2D noise = new Perlin2D(seed, kNoiseSize);
    kNoiseImage = new Pixmap(kNoiseImageSize, kNoiseImageSize,
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
        kNoiseImage.drawPixel(ix, iy, rgba);
      }
    }
    
    kNoiseTexture = new Texture(kNoiseImage);
    kNoiseTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    kNoiseTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    
  } // makeNoise()
  
  // nothing to do
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
  } // Sprite.advance()

  // display the image
  @Override
  public void draw(SpriteBatch batch) {

    batch.draw(kNoiseTexture,
               Env.gameOffsetX(), Env.gameOffsetY(),
               Env.gameWidth(), Env.gameHeight(),
               0.0f, 0.0f,
               kNoiseRepeats, kNoiseRepeats);

    Env.painter().mazePainter().draw(batch, mMaze);
    
  } // Sprite.draw()
  
} // class ScreenImage

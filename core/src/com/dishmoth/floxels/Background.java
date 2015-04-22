/*
 *  Background.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.LinkedList;

// static image of the maze and its background 
public class Background extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 0;
  
  // colour around the edge of the maze
  private static final Color kBorderColour = Color.WHITE;
  
  // range of background colours
  private static final Color kLightColour = new Color(189, 188, 141),
                             kDarkColour  = new Color(175, 173, 126);

  // details of textured tile image for the background
  private static final int     kNoiseImageSize  = 200,
                               kNoiseSize       = 8;
  private static final float   kGradientDeltaX  = -0.3f,
                               kGradientDeltaY  = -0.5f;
  private static final float   kLightCutoff     = 5.0f,
                               kDarkCutoff      = -2.0f;
  
  // textured background tile
  private static BufferedImage kNoiseImage = null;
  
  // reference to the maze object
  private Maze mMaze;
  
  // the image to be displayed
  private BufferedImage mImage;
  
  // constructor
  public Background(Maze maze) {
    
    super(kScreenLayer);
    
    mMaze = maze;
    
    if ( kNoiseImage == null ) kNoiseImage = makeNoise();
    updateImage();
    
  } // constructor

  // simple fractal noise sum
  static private float calcNoise(Perlin2D noise, float x, float y) {
    
    final float x1 = 2*x + 0.213f,
                y1 = 2*y + 0.312f;
    return noise.value(x,y) + 0.8f*noise.value(x1,y1);
    
  } // calcNoise()
  
  // build a noisy background image
  static private BufferedImage makeNoise() {
    
    final long seed = 1; 
    Perlin2D noise = new Perlin2D(seed, kNoiseSize);
    BufferedImage image = new BufferedImage(kNoiseImageSize, kNoiseImageSize,
                                            BufferedImage.TYPE_INT_RGB);
    
    final int rL = kLightColour.getRed(),
              gL = kLightColour.getGreen(),
              bL = kLightColour.getBlue();
    final int rD = kDarkColour.getRed(),
              gD = kDarkColour.getGreen(),
              bD = kDarkColour.getBlue();

    WritableRaster raster = image.getRaster();
    int pixels[] = ((DataBufferInt)(raster.getDataBuffer())).getData();
    int pixInd = 0;
    
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
        final int rgb = (r<<16) + (g<<8) + b;
        pixels[pixInd++] = rgb;
      }
    }

    return image;
    
  } // makeNoise()
  
  // prepare the background image including with the maze walls
  public void updateImage() {
    
    if ( mImage == null ) {
      mImage = Env.createOpaqueImage(Env.screenWidth(), Env.screenHeight());
    }
    
    Graphics2D g2 = mImage.createGraphics();
    g2.setBackground(kBorderColour);
    g2.clearRect(0, 0, Env.screenWidth(), Env.screenHeight());

    g2.setClip(Env.gameOffsetX(), Env.gameOffsetY(), 
               Env.gameWidth(), Env.gameHeight());
    final int ny = (int)Math.ceil( Env.gameHeight() / (float)kNoiseImageSize ),
              nx = (int)Math.ceil( Env.gameWidth() / (float)kNoiseImageSize );
    for ( int iy = 0 ; iy < ny ; iy++ ) {
      for ( int ix = 0 ; ix < nx ; ix++ ) {
        g2.drawImage(kNoiseImage, 
                     Env.gameOffsetX() + ix*kNoiseImageSize, 
                     Env.gameOffsetY() + iy*kNoiseImageSize, 
                     null);
      }
    }
    g2.setClip(0, 0, Env.screenWidth(), Env.screenHeight());
    
    MazePainter.draw(g2, mMaze, Env.gameOffsetX(), Env.gameOffsetY());
    
    g2.dispose();
    
  } // prepareImage()
  
  // nothing to do
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
  } // Sprite.advance()

  // display the image
  @Override
  public void draw(Graphics2D g2) {

    g2.drawImage(mImage, 0, 0, null);
    
  } // Sprite.draw()
  
} // class ScreenImage

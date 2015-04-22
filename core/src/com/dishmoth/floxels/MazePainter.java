/*
 *  MazePainter.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

// class that draws the wall of a maze
public class MazePainter {

  // number of pixels to cross the wall image
  private static final int kWallWidth = 10;
  
  // raw image data
  static private Pixmap  kPixmap;
  
  // texture holding packed wall/corner images
  static private Texture kTexture;
  
  // images for each type of join between wall sections
  // ordered according to: index = (wall goes right) + 2*(wall goes up) 
  //                             + 4*(wall goes left) + 8*(wall goes down) - 1
  private static final int     kNumCorners     = 15;
  private static TextureRegion kCornerImages[] = null;
  
  // two different wall section images
  private static TextureRegion kWallImageHoriz = null,
                               kWallImageVert  = null;
  
  // load resources, etc.
  static public void initialize() {
    
    if ( kCornerImages != null ) return; // already done
    
    // prepare the corner images

    Pixmap sourceImage = new Pixmap( Gdx.files.internal("Corners.png") );
    assert( sourceImage.getWidth()  == 5*(kWallWidth+2) );
    assert( sourceImage.getHeight() == 3*(kWallWidth+2) );

    kPixmap = new Pixmap(MathUtils.nextPowerOfTwo(sourceImage.getWidth()), 
                         MathUtils.nextPowerOfTwo(sourceImage.getHeight()),
                         Format.RGBA8888);
    kPixmap.drawPixmap(sourceImage, 0, 0);
    
    kTexture = new Texture(kPixmap);
    kTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    kCornerImages = new TextureRegion[kNumCorners];
    for ( int k = 0 ; k < kNumCorners ; k++ ) {
      final int x = (kWallWidth+2)*(k % 5) + 1,
                y = (kWallWidth+2)*(k / 5) + 1; 
      kCornerImages[k] = new TextureRegion(kTexture, x, y+kWallWidth, 
                                           kWallWidth, -kWallWidth);
    }

    // prepare the wall images

    kWallImageHoriz = kCornerImages[4];
    kWallImageVert  = kCornerImages[9];
      
  } // initialize()
  
  // display the maze walls
  static public void draw(SpriteBatch batch, Maze maze) {

    initialize();

    final int delta = Env.tileWidth(),
              eps   = Math.max(5, Math.round(kWallWidth*Env.tileWidth()/58.0f));
    
    final int xStart = Env.gameOffsetX() - eps/2,
              yStart = Env.gameOffsetY() - eps/2;
    
    int y = yStart;
    for ( int iy = 0 ; iy <= maze.numTilesY() ; iy++ ) {
      int x = xStart;
      for ( int ix = 0 ; ix <= maze.numTilesX() ; ix++ ) {
        final boolean right = maze.horizWall(ix, iy),
                      up    = maze.vertWall(ix, iy-1),
                      left  = maze.horizWall(ix-1, iy),
                      down  = maze.vertWall(ix, iy);
        final int index = 8*(down ? 1 : 0) + 4*(left  ? 1 : 0)
                        + 2*(up   ? 1 : 0) +   (right ? 1 : 0) - 1;
        if ( index >= 0 ) batch.draw(kCornerImages[index], x,y, eps,eps);
        if ( right )      batch.draw(kWallImageHoriz, x+eps,y, delta-eps,eps);
        if ( down )       batch.draw(kWallImageVert, x,y+eps, eps,delta-eps);
        x += delta;
      }
      y += delta;
    }
    
  } // draw()
  
} // class MazePainter

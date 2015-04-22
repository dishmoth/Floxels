/*
 *  MazePainter.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// class that draws the wall of a maze
public class MazePainter {

  // number of pixels to cross the wall image
  private static final int kWallWidth = 10;
  
  // images for each type of join between wall sections
  // ordered according to: index = (wall goes right) + 2*(wall goes up) 
  //                             + 4*(wall goes left) + 8*(wall goes down) - 1
  private static final int     kNumCorners     = 15;
  private static BufferedImage kCornerImages[] = null;
  
  // two different wall section images
  private static BufferedImage kWallImageHoriz = null,
                               kWallImageVert  = null;
  
  // load resources, etc.
  static public void initialize() {
    
    if ( kCornerImages != null ) return; // already done
    
    // prepare the corner images

    BufferedImage sourceImage = Env.resources().loadImage("Corners.png");
    assert( sourceImage.getWidth()  == 5*kWallWidth );
    assert( sourceImage.getHeight() == 3*kWallWidth );
    
    Color blankColour = new Color(0, 0, 0, 0);

    kCornerImages = new BufferedImage[kNumCorners];
    for ( int k = 0 ; k < kNumCorners ; k++ ) {
      final int x = kWallWidth*(k % 5),
                y = kWallWidth*(k / 5); 
      
      BufferedImage im = Env.createTranslucentImage(kWallWidth, kWallWidth);
      Graphics2D g2 = im.createGraphics();
      g2.setBackground(blankColour);
      g2.clearRect(0, 0, kWallWidth, kWallWidth);
      g2.drawImage(sourceImage, -x, -y, null);
      g2.dispose();

      kCornerImages[k] = im;
    }

    // prepare the wall images
    
    final int wallLength = Env.tileWidth() - kWallWidth; 
    final int repeats    = (wallLength + kWallWidth - 1)/kWallWidth;
    
    kWallImageHoriz = Env.createTranslucentImage(wallLength, kWallWidth);
    Graphics2D g2 = kWallImageHoriz.createGraphics();
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, wallLength, kWallWidth);
    for ( int k = 0 ; k < repeats ; k++ ) {
      g2.drawImage(kCornerImages[4], kWallWidth*k, 0, null);
    }
    g2.dispose();
    
    kWallImageVert = Env.createTranslucentImage(kWallWidth, wallLength);
    g2 = kWallImageVert.createGraphics();
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, kWallWidth, wallLength);
    for ( int k = 0 ; k < repeats ; k++ ) {
      g2.drawImage(kCornerImages[9], 0, kWallWidth*k, null);
    }
    g2.dispose();
        
  } // initialize()
  
  // display the maze walls
  static public void draw(Graphics2D g2, Maze maze, int x, int y) {

    initialize();

    final int delta = Env.tileWidth(),
              eps   = kWallWidth;
    final int xStart = x - eps/2,
              yStart = y - eps/2;
    
    y = yStart;
    for ( int iy = 0 ; iy <= maze.numTilesY() ; iy++ ) {
      x = xStart;
      for ( int ix = 0 ; ix <= maze.numTilesX() ; ix++ ) {
        final boolean right = maze.horizWall(ix, iy),
                      up    = maze.vertWall(ix, iy-1),
                      left  = maze.horizWall(ix-1, iy),
                      down  = maze.vertWall(ix, iy);
        final int index = 8*(down ? 1 : 0) + 4*(left  ? 1 : 0)
                        + 2*(up   ? 1 : 0) +   (right ? 1 : 0) - 1;
        if ( index >= 0 ) g2.drawImage(kCornerImages[index], x, y, null);
        if ( right )      g2.drawImage(kWallImageHoriz, x+eps, y, null);
        if ( down )       g2.drawImage(kWallImageVert, x, y+eps, null);
        x += delta;
      }
      y += delta;
    }
    
  } // draw()
  
} // class MazePainter

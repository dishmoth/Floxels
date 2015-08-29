/*
 *  Painter.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;

// store of textures, etc. for painting assorted things
public class Painter {

  // objects for painting different types of things
  private FloxelPainter mFloxelPainter;
  private MazePainter   mMazePainter;
  private HoopPainter   mHoopPainter;
  
  // raw image data
  private Pixmap mPixmap;
  
  // texture data
  private Texture mTexture;
  
  // Screen 2048x1536 => tiles 11x9  => tile 166 => floxel 31
  // Screen 1900x1200 => tiles 12x8  => tile 150 => floxel 28
  // Screen 1280x800  => tiles 12x8  => tile 100 => floxel 19
  // Screen 1280x720  => tiles 12x8  => tile 90  => floxel 17
  // Screen 600x600   => tiles 10x10 => tile 60  => floxel 11
  // Screen 480x320   => tiles 12x8  => tile 40  => floxel 8
  
  // constructor
  public Painter() {
    
    mFloxelPainter = null;
    mMazePainter   = null;
    mHoopPainter   = null;
    
  } // constructor
    
  // constructor
  public void prepare(int tileWidth) {
  
    final int floxelSize = Math.round( tileWidth*11.0f/58.0f ),
              mazeSize   = Math.round( tileWidth*10.0f/58.0f );
    
    mFloxelPainter = new FloxelPainter(floxelSize);
    mMazePainter   = new MazePainter(mazeSize);
    mHoopPainter   = new HoopPainter(tileWidth);

    Pixmap floxelPixmap = mFloxelPainter.floxelPixmap();
    Pixmap splatPixmap  = mFloxelPainter.splatPixmap();
    Pixmap mazePixmap   = mMazePainter.pixmap();
    
    final int textureMinWidth  = floxelPixmap.getWidth(),
              textureMinHeight = floxelPixmap.getHeight()
                                 + splatPixmap.getHeight()
                                 + mazePixmap.getHeight();
    final int textureWidth  = MathUtils.nextPowerOfTwo(textureMinWidth), 
              textureHeight = MathUtils.nextPowerOfTwo(textureMinHeight);
    mPixmap = new Pixmap(textureWidth, textureHeight, Format.RGBA8888);
    
    final int xFloxel = 0,
              yFloxel = 0,
              xSplat  = 0,
              ySplat  = yFloxel + floxelPixmap.getHeight(),
              xMaze   = 0,
              yMaze   = ySplat + splatPixmap.getHeight();
    
    Pixmap.Blending oldMode = Pixmap.getBlending();
    Pixmap.setBlending(Pixmap.Blending.None);
    
    mPixmap.drawPixmap(floxelPixmap, xFloxel, yFloxel);
    mPixmap.drawPixmap(splatPixmap,  xSplat, ySplat);
    mPixmap.drawPixmap(mazePixmap, xMaze, yMaze);
    
    Pixmap.setBlending(oldMode);
    
    //PixmapIO.writePNG(Gdx.files.external("pixmap.png"), mPixmap);

    mTexture = new Texture(mPixmap, false);
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    mFloxelPainter.setTexture(mTexture, xFloxel,yFloxel, xSplat,ySplat);
    mMazePainter.setTexture(mTexture, xMaze, yMaze);
    mHoopPainter.setTexture(mMazePainter.wallTexture(true));
    
  } // prepare()

  // access to the specific painter objects
  public FloxelPainter floxelPainter() { return mFloxelPainter; }
  public MazePainter   mazePainter()   { return mMazePainter; }
  public HoopPainter   hoopPainter()   { return mHoopPainter; }
  
  // discard resources
  public void dispose() {
  } // dispose()
  
} // class Painter

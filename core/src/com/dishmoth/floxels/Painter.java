/*
 *  Painter.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

//import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
//import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;

// store of textures, etc. for painting assorted things
public class Painter {

  // objects for painting different types of things
  private FloxelPainter     mFloxelPainter;
  private MazePainter       mMazePainter;
  private HoopPainter       mHoopPainter;
  private TitlePainter      mTitlePainter;
  private PatchPainter      mPatchPainter;
  private BackgroundPainter mBackgroundPainter;
  private Fonts             mFonts;
  
  // raw image data
  private Pixmap mPixmap;
  
  // texture data for floxels, maze, hoops, etc.
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
    mTitlePainter  = null;
    mPatchPainter  = null;
    mFonts         = null;
    
  } // constructor
    
  // constructor
  public void prepare(int tileWidth) {
  
    final int floxelSize = Math.round( tileWidth*11.0f/58.0f ),
              mazeSize   = Math.round( tileWidth*10.0f/58.0f );
    
    mFloxelPainter = new FloxelPainter(floxelSize);
    mMazePainter   = new MazePainter(mazeSize);
    mHoopPainter   = new HoopPainter(tileWidth);
    mPatchPainter  = new PatchPainter(tileWidth);

    Pixmap floxelPixmap = mFloxelPainter.floxelPixmap();
    Pixmap splatPixmap  = mFloxelPainter.splatPixmap();
    Pixmap mazePixmap   = mMazePainter.pixmap();
    Pixmap patchPixmap  = mPatchPainter.pixmap();
    
    final int textureMinWidth  = floxelPixmap.getWidth(),
              textureMinHeight = floxelPixmap.getHeight()
                                 + splatPixmap.getHeight()
                                 + Math.max( mazePixmap.getHeight(),
                                             patchPixmap.getHeight() );
    final int textureWidth  = MathUtils.nextPowerOfTwo(textureMinWidth), 
              textureHeight = MathUtils.nextPowerOfTwo(textureMinHeight);
    mPixmap = new Pixmap(textureWidth, textureHeight, Format.RGBA8888);
    
    final int xFloxel = 0,
              yFloxel = 0,
              xSplat  = 0,
              ySplat  = yFloxel + floxelPixmap.getHeight(),
              xMaze   = 0,
              yMaze   = ySplat + splatPixmap.getHeight(),
              xPatch  = xMaze + mazePixmap.getWidth(),
              yPatch  = yMaze;
    
    Pixmap.Blending oldMode = mPixmap.getBlending();
    mPixmap.setBlending(Pixmap.Blending.None);
    
    mPixmap.drawPixmap(floxelPixmap, xFloxel, yFloxel);
    mPixmap.drawPixmap(splatPixmap,  xSplat, ySplat);
    mPixmap.drawPixmap(mazePixmap, xMaze, yMaze);
    mPixmap.drawPixmap(patchPixmap, xPatch, yPatch);
    
    mPixmap.setBlending(oldMode);
    
    //PixmapIO.writePNG(Gdx.files.external("pixmap.png"), mPixmap);

    mTexture = new Texture(mPixmap, false);
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    mFloxelPainter.setTexture(mTexture, xFloxel,yFloxel, xSplat,ySplat);
    mMazePainter.setTexture(mTexture, xMaze, yMaze);
    mHoopPainter.setTexture(mMazePainter.wallTexture(true));
    mPatchPainter.setTexture(mTexture, xPatch, yPatch);

    mTitlePainter = new TitlePainter();
    mBackgroundPainter = new BackgroundPainter();
    
    mFonts = new Fonts(tileWidth);
    
  } // prepare()

  // access to the specific painter objects
  public FloxelPainter     floxelPainter()      { return mFloxelPainter; }
  public MazePainter       mazePainter()        { return mMazePainter; }
  public HoopPainter       hoopPainter()        { return mHoopPainter; }
  public TitlePainter      titlePainter()       { return mTitlePainter; }
  public PatchPainter      patchPainter()       { return mPatchPainter; }
  public BackgroundPainter backgroundPainter()  { return mBackgroundPainter; }
  public Fonts             fonts()              { return mFonts; }
  
  // rebuild unmanaged textures when the game is resumed
  public void resetTextures() {
    
    if ( mTexture != null ) mTexture.dispose();

    mTexture = new Texture(mPixmap, false);
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    mTexture.draw(mPixmap, 0, 0);
    
    mFloxelPainter.resetTexture(mTexture);
    mMazePainter.resetTexture(mTexture);
    mHoopPainter.resetTexture(mTexture);
    mPatchPainter.resetTexture(mTexture);
    
    mBackgroundPainter.resetTexture();
    
  } // resetTextures()
  
} // class Painter

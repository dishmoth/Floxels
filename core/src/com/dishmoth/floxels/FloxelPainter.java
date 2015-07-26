/*
 *  FloxelPainter.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

// class for drawing different types of floxels
public class FloxelPainter {

  // different base colours (red, green, blue from 0 to 255)
  static private final int kColours[][] = { { 240, 185,   0 },   // orange
                                            { 120, 120, 255 },   // blue
                                            { 240, 100, 240 },   // pink
                                            { 100, 220, 220 },   // cyan
                                            { 255, 120, 110 },   // red
                                            {  80, 240,  80 },   // green
                                            { 230, 220,   0 },   // yellow
                                            { 110, 170, 230 },   // light blue 
                                            { 250, 110, 180 },   // cherry
                                            {  90, 230, 150 },   // turquoise
                                            { 255, 170,  50 },   // copper
                                            { 150,  80, 250 } }; // purple 

  // details of different colour shades for floxel faces
  static private final float  kFaceEdgeMaxWhiteness = 0.6f,
                              kFaceFillMinBlackness = 0.05f;
  static private final int    kFacesFileMinNum      = 8,
                              kFacesFileMaxNum      = 22;
  static private final String kFacesImageFile       = "Faces";
  
  // images of floxels when splatted
  static private final String kSplatImageFile  = "Splat";
  static private final int    kSplatFileMinNum = 8,
                              kSplatFileMaxNum = 22;

  // different facial expressions for floxels (uncoloured)
  private final Pixmap mFacesImage;
  
  // size of a floxel face image in pixels and texels
  private final int mFacePixSize,
                    mFaceTexSize;
  
  // number of texels int border around face image to avoid bleeding if scaled
  private final int mFaceTexPadding;
  
  // basic image for splatted floxels (uncoloured)
  private final Pixmap mSplatImage;
  
  // size of the on-screen splat compared to the texture data
  private final float mSplatScale; 
  
  // how the images are packed in the texture
  private final int mFacesPerRow,
                    mNumFaceRows,
                    mSplatBaseY;
  
  // size of a floxel image in final texture units
  private final float kTextureFaceSizeU,
                      kTextureFaceSizeV,
                      kTextureFacePadU,
                      kTextureFacePadV;
  
  // raw image data
  private Pixmap mPixmap;
  
  // texture holding packed floxel images
  private Texture mTexture;
  
  // colour of the first floxel type
  static private int kColourOffset = 0;
  
  // number of different colour sets
  public static int numColours() { return kColours.length; }
  
  // Screen 2048x1536 => tiles 11x9  => tile 166 => floxel 31
  // Screen 1900x1200 => tiles 12x8  => tile 150 => floxel 28
  // Screen 1280x800  => tiles 12x8  => tile 100 => floxel 19
  // Screen 1280x720  => tiles 12x8  => tile 90  => floxel 17
  // Screen 600x600   => tiles 10x10 => tile 60  => floxel 11
  // Screen 480x320   => tiles 12x8  => tile 40  => floxel 8
  
  // constructor
  public FloxelPainter(int targetSize) {

    mFacePixSize = targetSize;
    mFaceTexSize = Math.min(mFacePixSize, kFacesFileMaxNum);
    mFaceTexPadding = (mFacePixSize > mFaceTexSize) ? 1 : 0;

    if ( mFaceTexSize >= kFacesFileMinNum ) {
      String fname = kFacesImageFile + mFaceTexSize + ".png";
      mFacesImage = new Pixmap( Gdx.files.internal(fname) );
      assert( mFacesImage.getHeight() == mFaceTexSize );
      assert( mFacesImage.getWidth() == 7*mFaceTexSize );
    } else {
      mFacesImage = null;
    }

    final int splatSize = Math.max(kSplatFileMinNum, 
                                   Math.min(kSplatFileMaxNum, targetSize));
    final String splatFname = kSplatImageFile + splatSize + ".png";
    mSplatImage = new Pixmap( Gdx.files.internal(splatFname) );
    assert( mSplatImage.getWidth() == mSplatImage.getHeight() );

    final int totalFaces = kColours.length * Floxel.NUM_SHADES
                           * Floxel.NUM_NORMAL_FACES;
    final int faceSizePadded = mFaceTexSize + 2*mFaceTexPadding;
    final int textureWidth = 1024;
    mFacesPerRow = textureWidth/faceSizePadded;
    mNumFaceRows = (int)Math.ceil(totalFaces/(float)mFacesPerRow);

    mSplatBaseY = faceSizePadded*mNumFaceRows;
    assert( (mSplatImage.getWidth()+2)*kColours.length <= textureWidth );
    
    mSplatScale = targetSize/(float)splatSize;
    
    final int textureMinHeight = mSplatBaseY + mSplatImage.getHeight()+2;
    final int textureHeight = MathUtils.nextPowerOfTwo(textureMinHeight); 
    mPixmap = new Pixmap(textureWidth, textureHeight, Format.RGBA8888);
    
    float edgeWidth = (mFaceTexSize < 13) ? 1.0f : mFaceTexSize/11.0f;
    
    int ix = 0,
        iy = 0;
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      for ( int iShade = 0 ; iShade < Floxel.NUM_SHADES ; iShade++ ) {
        for ( int iFace = 0 ; iFace < Floxel.NUM_NORMAL_FACES ; iFace++ ) {
          prepareFace(iCol, iShade, iFace, 
                      ix*faceSizePadded, iy*faceSizePadded,
                      edgeWidth);
          if ( ++ix >= mFacesPerRow ) {
            ix = 0;
            iy += 1;
          }
        }
      }
    }
    
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      int x = iCol*(mSplatImage.getWidth()+2);
      prepareSplat(iCol, x+1, mSplatBaseY+1);
    }

    //PixmapIO.writePNG(Gdx.files.external("pixmap.png"), mPixmap);
    
    mTexture = new Texture( mPixmap, false );
    mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    kTextureFaceSizeU = faceSizePadded/(float)mPixmap.getWidth();
    kTextureFaceSizeV = faceSizePadded/(float)mPixmap.getHeight();
    kTextureFacePadU = mFaceTexPadding/(float)mPixmap.getWidth();
    kTextureFacePadV = mFaceTexPadding/(float)mPixmap.getWidth();
    
  } // constructor

  // create the pixels for one floxel face, including padding
  private void prepareFace(int colInd, int shadeInd, int faceInd,
                           int x, int y, float edgeWidth) {
    
    float r = kColours[colInd][0]/255.0f,
          g = kColours[colInd][1]/255.0f,
          b = kColours[colInd][2]/255.0f;
    
    final float shade = shadeInd/(Floxel.NUM_SHADES-1.0f);
    final float hE = shade*kFaceEdgeMaxWhiteness;
    final float rE = (1-hE)*r + hE,
                gE = (1-hE)*g + hE,
                bE = (1-hE)*b + hE;

    final float hF = (1.0f-shade)*kFaceFillMinBlackness + shade;
    final float rF = hF*r,
                gF = hF*g,
                bF = hF*b;

    final int faceSizePadded = mFaceTexSize + 2*mFaceTexPadding;
    final int x1 = x + mFaceTexPadding,
              y1 = y + mFaceTexPadding;

    mPixmap.setColor(rE, gE, bE, 1.0f);
    mPixmap.fillRectangle(x, y, faceSizePadded, faceSizePadded);

    int edge = (int)edgeWidth;
    mPixmap.setColor(rF, gF, bF, 1.0f);
    mPixmap.fillRectangle(x1+edge, y1+edge, 
                          mFaceTexSize-2*edge, mFaceTexSize-2*edge);
    
    if ( edge < edgeWidth ) {
      float e = edgeWidth - edge;
      mPixmap.setColor((1-e)*rF+e*rE, (1-e)*gF+e*gE, (1-e)*bF+e*bE, 1.0f);
      mPixmap.drawRectangle(x1+edge, y1+edge, 
                            mFaceTexSize-2*edge, mFaceTexSize-2*edge);
    }
    
    if ( mFacesImage != null ) {
      final int srcSize = mFacesImage.getHeight();
      mPixmap.drawPixmap(mFacesImage,
                         faceInd*srcSize, 0, srcSize, srcSize,
                         x1, y1, mFaceTexSize, mFaceTexSize);
    }
    
  } // prepareFace()
  
  // create the pixels for one splat
  private void prepareSplat(int colInd, int x, int y) {
    
    Pixmap.setBlending(Pixmap.Blending.None);
    
    mPixmap.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    mPixmap.fillRectangle(x-1, y-1, 
                         mSplatImage.getWidth()+2,
                         mSplatImage.getHeight()+2);

    colourCopyPixmap(mPixmap,
                     x, y,
                     mSplatImage, 
                     kColours[colInd][0]/255.0f,
                     kColours[colInd][1]/255.0f,
                     kColours[colInd][2]/255.0f);
    
    Pixmap.setBlending(Pixmap.Blending.SourceOver);
    
  } // prepareSplat()
  
  // make a tinted copy of a pixmap
  static private void colourCopyPixmap(Pixmap pixmap, int x0, int y0,
                                       Pixmap source, 
                                       float r0, float g0, float b0) {
    
    for ( int iy = 0 ; iy < source.getHeight() ; iy++ ) {
      for ( int ix = 0 ; ix < source.getWidth() ; ix++ ) {
        int rgba = source.getPixel(ix, iy);
        int r = Math.round( ((rgba>>24)&0xFF) * r0 ),
            g = Math.round( ((rgba>>16)&0xFF) * g0 ),
            b = Math.round( ((rgba>> 8)&0xFF) * b0 ),
            a = (rgba&0xFF);
        if ( a == 0 ) {
          r = Math.round(255*r0);
          g = Math.round(255*g0);
          b = Math.round(255*b0);
        }
        pixmap.drawPixel( x0+ix, y0+iy, ((r<<24)|(g<<16)|(b<<8)|a) );
      }
    }
    
  } // colourCopyPixmap()
  
  // release resources
  public void dispose() {
    
    mFacesImage.dispose();
    mSplatImage.dispose();
    mPixmap.dispose();
    mTexture.dispose();
    
  } // dispose()
  
  // pixel width of floxel
  public int targetSize() { return mFacePixSize; }
  
  // change the base floxel colour
  static void advanceColourIndex() {
    
    kColourOffset = ( kColourOffset + 1 ) % kColours.length;
    
  } // advanceColourIndex()
  
  // display a floxel
  void draw(SpriteBatch batch, Floxel floxel) {

    final int colour = (floxel.mType + kColourOffset) % kColours.length;
    
    final float xScale = Env.gameWidth() / (float)Env.numTilesX(),
                yScale = Env.gameHeight() / (float)Env.numTilesY();

    int x = (int)(floxel.mX * xScale) + Env.gameOffsetX(),
        y = (int)(floxel.mY * yScale) + Env.gameOffsetY();
      
    if ( floxel.mFace < Floxel.NUM_NORMAL_FACES ) {
      
      final int index = ( colour*Floxel.NUM_SHADES 
                          + floxel.mShade )*Floxel.NUM_NORMAL_FACES
                        + floxel.mFace;
  
      final int iu = index % mFacesPerRow,
                iv = index / mFacesPerRow;
      
      batch.draw( mTexture,
                  x-mFacePixSize/2, y-mFacePixSize/2, 
                  mFacePixSize, mFacePixSize,
                  iu*kTextureFaceSizeU + kTextureFacePadU,
                  (iv+1)*kTextureFaceSizeV - kTextureFacePadV, 
                  (iu+1)*kTextureFaceSizeU - kTextureFacePadU, 
                  iv*kTextureFaceSizeV + kTextureFacePadV );
      
    } else {

      assert( floxel.mFace == Floxel.SPLAT_FACE );
      
      final int texSize = mSplatImage.getWidth(),
                pixSize = Math.round( texSize * mSplatScale );
      
      int iu0 = colour*(texSize+2) + 1,
          iu1 = iu0 + texSize,
          iv0 = mSplatBaseY + 1,
          iv1 = iv0 + texSize;
      
      batch.draw( mTexture,
                  x-pixSize/2, y-pixSize/2, pixSize, pixSize,
                  iu0/(float)mTexture.getWidth(),
                  iv1/(float)mTexture.getHeight(), 
                  iu1/(float)mTexture.getWidth(),
                  iv0/(float)mTexture.getHeight() );
      
    }
    
  } // draw()

} // class FloxelPainter

/*
 *  FloxelPainter.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// class for drawing different types of floxels
public class FloxelPainter {

  // details of different colour shades for floxel faces
  static private final float  kFaceEdgeMaxWhiteness = 0.9f,
                              kFaceFillMinBlackness = 0.0f;
  static private final int    kFacesFileMinNum      = 8,
                              kFacesFileMaxNum      = 22;
  static private final String kFacesImageFile       = "Faces";
  
  // images of floxels when splatted
  static private final String kSplatImageFile  = "Splat";
  static private final int    kSplatFileMinNum = 8,
                              kSplatFileMaxNum = 22;

  // size of a floxel face image in pixels and texels
  private final int mFacePixSize,
                    mFaceTexSize;
  
  // number of texels int border around face image to avoid bleeding if scaled
  private final int mFaceTexPadding;
  
  // size of a splat image in pixels and texels
  private final int mSplatPixSize,
                    mSplatTexSize; 
  
  // how the floxel images are packed in the pixmap/texture
  private final int mFacesPerRow,
                    mNumFaceRows;
  
  // raw image data
  private Pixmap mFloxelPixmap,
                 mSplatPixmap;
  
  // where the floxel images are in the final texture (in texture units)
  private float mTextureFaceU,
                mTextureFaceV,
                mTextureFaceSizeU,
                mTextureFaceSizeV,
                mTextureFacePadU,
                mTextureFacePadV;
  
  // where the splat images are in the final texture (in texture units)
  private float mTextureSplatU,
                mTextureSplatV,
                mTextureSplatSizeU,
                mTextureSplatSizeV,
                mTextureSplatShiftU;
  
  // reference to the texture data
  private Texture mTexture;
  
  // constructor
  public FloxelPainter(int targetSize) {

    // prepare the faces

    mFacePixSize = targetSize;
    mFaceTexSize = Math.min(mFacePixSize, kFacesFileMaxNum);
    mFaceTexPadding = (mFacePixSize > mFaceTexSize) ? 1 : 0;

    Env.debug("Floxel size: " + mFacePixSize + " pixels");
    
    Pixmap facesImage = null;
    if ( mFaceTexSize >= kFacesFileMinNum ) {
      String fname = kFacesImageFile + mFaceTexSize + ".png";
      facesImage = new Pixmap( Gdx.files.internal(fname) );
      assert( facesImage.getHeight() == mFaceTexSize );
      assert( facesImage.getWidth() == 7*mFaceTexSize );
    }

    final int totalFaces = ColourScheme.num() * Floxel.NUM_SHADES
                           * Floxel.NUM_NORMAL_FACES;
    final int faceSizePadded = mFaceTexSize + 2*mFaceTexPadding;
    final int textureWidth = 1024;
    mFacesPerRow = textureWidth/faceSizePadded;
    mNumFaceRows = (int)Math.ceil(totalFaces/(float)mFacesPerRow);

    mFloxelPixmap = new Pixmap(mFacesPerRow*faceSizePadded, 
                               mNumFaceRows*faceSizePadded, 
                               Format.RGBA8888);
    
    float edgeWidth = (mFaceTexSize < 13) ? 1.0f : mFaceTexSize/11.0f;
    
    int ix = 0,
        iy = 0;
    for ( int iCol = 0 ; iCol < ColourScheme.num() ; iCol++ ) {
      for ( int iShade = 0 ; iShade < Floxel.NUM_SHADES ; iShade++ ) {
        for ( int iFace = 0 ; iFace < Floxel.NUM_NORMAL_FACES ; iFace++ ) {
          prepareFace(iCol, iShade, iFace, 
                      ix*faceSizePadded, iy*faceSizePadded,
                      edgeWidth, facesImage);
          if ( ++ix >= mFacesPerRow ) {
            ix = 0;
            iy += 1;
          }
        }
      }
    }

    if ( facesImage != null ) facesImage.dispose();
    
    // prepare the splats
    
    int splatSize = Math.max(kSplatFileMinNum, 
                             Math.min(kSplatFileMaxNum, targetSize));
    final String splatFname = kSplatImageFile + splatSize + ".png";
    Pixmap splatImage = new Pixmap( Gdx.files.internal(splatFname) );
    assert( splatImage.getWidth() == splatImage.getHeight() );

    mSplatTexSize = splatImage.getHeight();
    mSplatPixSize = Math.round(mSplatTexSize*targetSize/(float)splatSize);
    
    mSplatPixmap = new Pixmap(ColourScheme.num()*(mSplatTexSize+2), 
                              (mSplatTexSize+2), 
                              Format.RGBA8888);
    
    for ( int iCol = 0 ; iCol < ColourScheme.num() ; iCol++ ) {
      int x = iCol*(mSplatTexSize+2);
      prepareSplat(iCol, x+1, 1, splatImage);
    }

    splatImage.dispose();

  } // constructor

  // create the pixels for one floxel face, including padding
  private void prepareFace(int colInd, int shadeInd, int faceInd,
                           int x, int y, float edgeWidth,
                           Pixmap facesImage) {
    
    int rgb[] = ColourScheme.colour(colInd);
    float r = rgb[0]/255.0f,
          g = rgb[1]/255.0f,
          b = rgb[2]/255.0f;
    
    final float shade = shadeInd/(Floxel.NUM_SHADES-1.0f);
    final float shade2 = shade*shade;
    
    final float hE = shade2*kFaceEdgeMaxWhiteness;
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

    mFloxelPixmap.setColor(rE, gE, bE, 1.0f);
    mFloxelPixmap.fillRectangle(x, y, faceSizePadded, faceSizePadded);

    int edge = (int)edgeWidth;
    mFloxelPixmap.setColor(rF, gF, bF, 1.0f);
    mFloxelPixmap.fillRectangle(x1+edge, y1+edge, 
                                mFaceTexSize-2*edge, mFaceTexSize-2*edge);
    
    if ( edge < edgeWidth ) {
      float e = edgeWidth - edge;
      mFloxelPixmap.setColor((1-e)*rF+e*rE, (1-e)*gF+e*gE, (1-e)*bF+e*bE, 1.0f);
      mFloxelPixmap.drawRectangle(x1+edge, y1+edge, 
                                  mFaceTexSize-2*edge, mFaceTexSize-2*edge);
    }
    
    if ( facesImage != null ) {
      final int srcSize = facesImage.getHeight();
      mFloxelPixmap.drawPixmap(facesImage,
                               faceInd*srcSize, 0, srcSize, srcSize,
                               x1, y1, mFaceTexSize, mFaceTexSize);
    }
    
  } // prepareFace()
  
  // create the pixels for one splat
  private void prepareSplat(int colInd, int x, int y, Pixmap splatImage) {
    
    Pixmap.Blending oldMode = Pixmap.getBlending();
    Pixmap.setBlending(Pixmap.Blending.None);
    
    mSplatPixmap.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    mSplatPixmap.fillRectangle(x-1, y-1, 
                               splatImage.getWidth()+2,
                               splatImage.getHeight()+2);

    int rgb[] = ColourScheme.colour(colInd);
    colourCopyPixmap(mSplatPixmap,
                     x, y, splatImage, 
                     rgb[0]/255.0f, rgb[1]/255.0f, rgb[2]/255.0f);
    
    Pixmap.setBlending(oldMode);
    
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
  
  // access to the image data
  public Pixmap floxelPixmap() { return mFloxelPixmap; }
  public Pixmap splatPixmap()  { return mSplatPixmap; }
  
  // pixel width of floxel
  public int targetSize() { return mFacePixSize; }
  
  // update now that the pixmaps have been embedded in a texture
  public void setTexture(Texture texture, 
                         int xFloxels, int yFloxels,
                         int xSplats,  int ySplats) {
    
    assert( mTexture == null );
    mTexture = texture;
    
    mTextureFaceU = xFloxels/(float)mTexture.getWidth();
    mTextureFaceV = yFloxels/(float)mTexture.getHeight();

    final int faceSizePadded = mFaceTexSize + 2*mFaceTexPadding;
    mTextureFaceSizeU = faceSizePadded/(float)mTexture.getWidth();
    mTextureFaceSizeV = faceSizePadded/(float)mTexture.getHeight();
    
    mTextureFacePadU = mFaceTexPadding/(float)mTexture.getWidth();
    mTextureFacePadV = mFaceTexPadding/(float)mTexture.getWidth();
        
    mTextureSplatU = (xSplats+1)/(float)mTexture.getWidth();
    mTextureSplatV = (ySplats+1)/(float)mTexture.getHeight();
    
    mTextureSplatSizeU = mSplatTexSize/(float)mTexture.getWidth();
    mTextureSplatSizeV = mSplatTexSize/(float)mTexture.getHeight();

    mTextureSplatShiftU = (mSplatTexSize+2)/(float)mTexture.getWidth();
    
  } // setTexure()

  // replace the texture (following game pause/resume)
  public void resetTexture(Texture texture) { mTexture = texture; }
  
  // display a floxel
  void draw(SpriteBatch batch, Floxel floxel, int colour) {

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
                  mTextureFaceU + iu*mTextureFaceSizeU + mTextureFacePadU,
                  mTextureFaceV + (iv+1)*mTextureFaceSizeV - mTextureFacePadV, 
                  mTextureFaceU + (iu+1)*mTextureFaceSizeU - mTextureFacePadU, 
                  mTextureFaceV + iv*mTextureFaceSizeV + mTextureFacePadV );
      
    } else {

      assert( floxel.mFace == Floxel.SPLAT_FACE );
      
      final float u0 = mTextureSplatU + colour*mTextureSplatShiftU,
                  u1 = u0 + mTextureSplatSizeU,
                  v0 = mTextureSplatV,
                  v1 = v0 + mTextureSplatSizeV;
      
      batch.draw( mTexture,
                  x-mSplatPixSize/2, y-mSplatPixSize/2,
                  mSplatPixSize, mSplatPixSize,
                  u0, v1, u1, v0 );
      
    }
    
  } // draw()
  
} // class FloxelPainter

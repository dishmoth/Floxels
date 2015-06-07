/*
 *  FloxelPainter.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

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

  // details of different colour shades
  static private final float kEdgeMaxWhiteness = 0.6f,
                             kFillMinBlackness = 0.05f;
  
  // different facial expressions (uncoloured)
  static private final String kFacesImageFile = "Faces.png";
  static private Pixmap       kFacesImage     = null;
  
  // different facial expressions (uncoloured)
  static private final String kSplatImageFile = "Splat.png";
  static private Pixmap       kSplatImage     = null;
  
  // size of a floxel image in texels
  static private final int kTargetSize = 11;
  
  // size of a floxel image in screen pixels
  static private int kScreenSize = 0;

  // size of a floxel image in texture units
  static private float kTextureSizeU,
                       kTextureSizeV;
  
  // tweak to texture coordinates to avoid bleed between neighbours
  static private float kTextureDelta;
  
  // how the floxel images are packed in the texture
  static private int kFacesPerRow,
                     kNumFaceRows,
                     kSplatBaseY;
  
  // images of floxels when hit
  static private final int   kSplatImageSize  = 19,
                             kSplatStarPoints = 8;
  static private final float kSplatMaxRadius  = 9.0f,
                             kSplatMinRadius  = 6.0f;
  static private final float kSplatBlackness  = 0.2f;

  // raw image data
  static private Pixmap kPixmap;
  
  // texture holding packed floxel images
  static private Texture kTexture;
  
  // colour of the first floxel type
  static private int kColourOffset = 0;
  
  // number of different colour sets
  public static int numColours() { return kColours.length; }
  
  // load resources, etc.
  static public void initialize() {

    if ( kTexture != null ) return;

    kFacesImage = new Pixmap( Gdx.files.internal(kFacesImageFile) );

    kSplatImage = new Pixmap( Gdx.files.internal(kSplatImageFile) );
    
    int totalFaces = kColours.length*Floxel.NUM_SHADES*Floxel.NUM_NORMAL_FACES;
    kFacesPerRow = 1024/kTargetSize;
    kNumFaceRows = (int)Math.ceil(totalFaces/(float)kFacesPerRow);
    kSplatBaseY = kTargetSize*kNumFaceRows;
    
    int textureMinWidth = kTargetSize*kFacesPerRow,
        textureMinHeight = kSplatBaseY + kSplatImage.getHeight()+2;
    kPixmap = new Pixmap(MathUtils.nextPowerOfTwo(textureMinWidth), 
                         MathUtils.nextPowerOfTwo(textureMinHeight),
                         Format.RGBA8888);
    
    int ix = 0,
        iy = 0;
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      for ( int iShade = 0 ; iShade < Floxel.NUM_SHADES ; iShade++ ) {
        for ( int iFace = 0 ; iFace < Floxel.NUM_NORMAL_FACES ; iFace++ ) {
          prepareFace(iCol, iShade, iFace, 
                      kPixmap, ix*kTargetSize, iy*kTargetSize);
          if ( ++ix >= kFacesPerRow ) {
            ix = 0;
            iy += 1;
          }
        }
      }
    }
    
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      int x = iCol*(kSplatImage.getWidth()+2);
      prepareSplat(iCol, kPixmap, x+1, kSplatBaseY+1);
    }

    //PixmapIO.writePNG(Gdx.files.external("pixmap.png"), kPixmap);
    
    kTexture = new Texture( kPixmap, false );
    kTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
    kScreenSize = Math.round( Env.tileWidth()*11.0f/58.0f );
    
    kTextureSizeU = kTargetSize/(float)kPixmap.getWidth();
    kTextureSizeV = kTargetSize/(float)kPixmap.getHeight();

    if ( kScreenSize > kTargetSize ) {
      // (1-2*Delta)*0.5/ScreenSize + Delta = 0.5/TargetSize
      // => 0.5/ScreenSize + Delta*(1-1/ScreenSize) = 0.5/TargetSize
      // => Delta = 0.5*(1/TargetSize - 1/ScreenSize) / (1 - 1/ScreenSize)
      //          = 0.5*(ScreenSize/TargetSize - 1)/(ScreenSize - 1)
      kTextureDelta = 0.5f*(kScreenSize/(float)kTargetSize - 1.0f)
                          /(kScreenSize - 1.0f);
    } else {
      kTextureDelta = 0.0f;
    }
    
  } // initialize()

  // create the pixels for one face
  static private void prepareFace(int colInd, int shadeInd, int faceInd,
                                  Pixmap pixmap, int x, int y) {
    
    float r = kColours[colInd][0]/255.0f,
          g = kColours[colInd][1]/255.0f,
          b = kColours[colInd][2]/255.0f;
    
    final float shade = shadeInd/(Floxel.NUM_SHADES-1.0f);
    final float hE = shade*kEdgeMaxWhiteness;
    final float rE = (1-hE)*r + hE,
                gE = (1-hE)*g + hE,
                bE = (1-hE)*b + hE;

    final float hF = (1.0f-shade)*kFillMinBlackness + shade;
    final float rF = hF*r,
                gF = hF*g,
                bF = hF*b;

    pixmap.setColor(rF, gF, bF, 1.0f);
    pixmap.fillRectangle(x, y, kTargetSize, kTargetSize);
    
    pixmap.setColor(rE, gE, bE, 1.0f);
    pixmap.drawRectangle(x, y, kTargetSize, kTargetSize);

    final int srcSize = kFacesImage.getHeight();
    pixmap.drawPixmap(kFacesImage,
                      faceInd*srcSize, 0, srcSize, srcSize,
                      x, y, kTargetSize, kTargetSize);
    
  } // prepareFace()
  
  // create the pixels for one splat
  static private void prepareSplat(int colInd, Pixmap pixmap, int x, int y) {
    
    pixmap.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    pixmap.fillRectangle(x-1, y-1, 
                         kSplatImage.getWidth()+2,
                         kSplatImage.getHeight()+2);
    
    colourPixmap(pixmap,
                 x, y,
                 kSplatImage, 
                 kColours[colInd][0]/255.0f,
                 kColours[colInd][1]/255.0f,
                 kColours[colInd][2]/255.0f);
    
  } // prepareSplat()
  
  //
  static private void colourPixmap(Pixmap pixmap, int x0, int y0,
                                   Pixmap source, float r0, float g0, float b0) {
    
    for ( int iy = 0 ; iy < source.getHeight() ; iy++ ) {
      for ( int ix = 0 ; ix < source.getWidth() ; ix++ ) {
        int rgba = source.getPixel(ix, iy);
        int r = Math.round( ((rgba>>24)&0xFF) * r0 ),
            g = Math.round( ((rgba>>16)&0xFF) * g0 ),
            b = Math.round( ((rgba>> 8)&0xFF) * b0 ),
            a = (rgba&0xFF);
        pixmap.drawPixel( x0+ix, y0+iy, ((r<<24)|(g<<16)|(b<<8)|a) );
      }
    }
    
  } // colourPixmap()
  
  // change the base floxel colour
  static void advanceColourIndex() {
    
    kColourOffset = ( kColourOffset + 1 ) % kColours.length;
    
  } // advanceColourIndex()
  
  // display a floxel
  static void draw(SpriteBatch batch, Floxel floxel) {

    final int colour = (floxel.mType + kColourOffset) % kColours.length;
    
    final float xScale = Env.gameWidth() / (float)Env.numTilesX(),
                yScale = Env.gameHeight() / (float)Env.numTilesY();

    int x = (int)(floxel.mX * xScale) + Env.gameOffsetX(),
        y = (int)(floxel.mY * yScale) + Env.gameOffsetY();
      
    if ( floxel.mFace < Floxel.NUM_NORMAL_FACES ) {
      
      final int index = ( colour*Floxel.NUM_SHADES 
                          + floxel.mShade )*Floxel.NUM_NORMAL_FACES
                        + floxel.mFace;
  
      final int iu = index % kFacesPerRow,
                iv = index / kFacesPerRow;
      
      batch.draw( kTexture,
                  x-kScreenSize/2, y-kScreenSize/2, kScreenSize, kScreenSize,
                  (iu+kTextureDelta)*kTextureSizeU,
                  (iv+1-kTextureDelta)*kTextureSizeV, 
                  (iu+1-kTextureDelta)*kTextureSizeU, 
                  (iv+kTextureDelta)*kTextureSizeV );
      
    } else {

      assert( floxel.mFace == Floxel.SPLAT_FACE );
      
      float scale = kScreenSize/(float)kTargetSize;
      int size = Math.round( kSplatImage.getWidth() * scale );
      
      int iu0 = colour*(kSplatImage.getWidth()+2) + 1,
          iu1 = iu0 + kSplatImage.getWidth(),
          iv0 = kSplatBaseY + 1,
          iv1 = iv0 + kSplatImage.getHeight();
      
      batch.draw( kTexture,
                  x-size/2, y-size/2, size, size,
                  iu0/(float)kTexture.getWidth(),
                  iv1/(float)kTexture.getHeight(), 
                  iu1/(float)kTexture.getWidth(),
                  iv0/(float)kTexture.getHeight() );
      
    }
    
  } // draw()

  /*
  // write an image file for a splatted face
  static private void generateSplatImage() {

    Path2D splatShapes[] = new Path2D[2];
    
    for ( int version = 0 ; version <= 1 ; version++ ) {
      final float inset = (version==0) ? 0.0f : 2.0f;
      
      Path2D star = new Path2D.Float();    
      for ( int k = 0 ; k < kSplatStarPoints ; k++ ) {
        final float theta1 = (2*k+0.5f)*(float)Math.PI/kSplatStarPoints,
                    theta2 = (2*k+1.5f)*(float)Math.PI/kSplatStarPoints;
        final float r1 = kSplatMaxRadius - inset,
                    r2 = kSplatMinRadius - inset;
        final float x1 = r1*(float)Math.cos(theta1),
                    y1 = r1*(float)Math.sin(theta1),
                    x2 = r2*(float)Math.cos(theta2),
                    y2 = r2*(float)Math.sin(theta2);
  
        if ( k == 0 ) star.moveTo(x1, y1);
        else          star.lineTo(x1, y1);
        star.lineTo(x2, y2);
      }
      star.closePath();

      splatShapes[version] = star;
    }

    Color edgeColour = new Color(255, 255, 255); 
    
    final int fill = Math.round(255*kSplatBlackness);
    Color fillColour = new Color(fill, fill, fill); 

    Color blankColour = new Color(255,255,255,0);
    
    BufferedImage im = new BufferedImage(kSplatImageSize, kSplatImageSize,
                                         BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = im.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, kSplatImageSize, kSplatImageSize);
    g2.translate(0.5f*kSplatImageSize, 0.5f*kSplatImageSize);

    g2.setColor(edgeColour);
    g2.fill(splatShapes[0]);
    g2.setColor(fillColour);
    g2.fill(splatShapes[1]);

    g2.dispose();
  
    try {
      ImageIO.write(im, "png", new File(kSplatImageFile));
      Env.debug("Created image file: " + kSplatImageFile);
    } catch (Exception ex) {
      Env.debug(ex.getMessage());
    }
    
  } // prepareSplatImage()
  */
  
} // class FloxelPainter

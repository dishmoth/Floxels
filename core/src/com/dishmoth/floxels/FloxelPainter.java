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

// class that draws different types of floxels
public class FloxelPainter {

  // different colours
  static private final Color kColours[] = { 
                                  new Color(240, 185,   0), // orange
                                  new Color(120, 120, 255), // blue
                                  new Color(240, 100, 240), // pink
                                  new Color(100, 220, 220), // cyan
                                  new Color(255, 120, 110), // red
                                  new Color( 80, 240,  80), // green
                                  new Color(230, 220,   0), // yellow
                                  new Color(110, 170, 230), // light blue 
                                  new Color(250, 110, 180), // cherry
                                  new Color( 90, 230, 150), // turquoise
                                  new Color(255, 170,  50), // copper
                                  new Color(150,  80, 250)  // purple 
                                };

  // details of different shades
  static private final float kEdgeMaxWhiteness = 0.6f,
                             kFillMinBlackness = 0.05f,
                             kSplatWhiteness   = 0.5f,
                             kSplatBlackness   = 0.2f;
  
  // images of different face combinations  
  static private final int     kImageSize    = 11;
  static private BufferedImage kImages[][][] = null;

  // images of floxels when hit
  static private final int     kSplatImageSize  = 19,
                               kSplatStarPoints = 8;
  static private final float   kSplatMaxRadius  = 9.0f,
                               kSplatMinRadius  = 6.0f;
  static private Path2D        kSplatShapes[]   = null;
  static private BufferedImage kSplatImages[]   = null;

  // colour of the first floxel type
  static private int kColourOffset = 0;
  
  // number of different colour sets
  public static int numColours() { return kColours.length; }
  
  // load resources, etc.
  static public void initialize() {

    if ( kImages != null ) return;
    
    BufferedImage faces = Env.resources().loadImage("Faces.png");
    assert( faces.getWidth() == Floxel.numNormalFaces()*kImageSize );
    assert( faces.getHeight() == kImageSize );
    
    kImages = new BufferedImage[kColours.length]
                               [Floxel.numShades()]
                               [Floxel.numNormalFaces()];    
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      for ( int iShade = 0 ; iShade < Floxel.numShades() ; iShade++ ) {
        for ( int iFace = 0 ; iFace < Floxel.numNormalFaces() ; iFace++ ) {
          kImages[iCol][iShade][iFace] = 
                           prepareImage(iCol, iShade, faces, iFace);
        }
      }
    }
  
    kSplatImages = new BufferedImage[kColours.length];
    makeSplatShapes();
    for ( int iCol = 0 ; iCol < kColours.length ; iCol++ ) {
      kSplatImages[iCol] = prepareSplatImage(iCol);
    }
    
  } // initialize()

  // create a buffered image for one face
  static private BufferedImage prepareImage(int colInd, int shadeInd,
                                            BufferedImage faces, int faceInd) {

    Color colour = kColours[colInd];

    final float shade = shadeInd/(Floxel.numShades()-1.0f);
    final float hE = shade*kEdgeMaxWhiteness;
    final int rE = Math.round((1-hE)*colour.getRed() + hE*255),
              gE = Math.round((1-hE)*colour.getGreen() + hE*255),
              bE = Math.round((1-hE)*colour.getBlue() + hE*255);
    Color edgeColour = new Color(rE, gE, bE); 
    
    final float hF = (1.0f-shade)*kFillMinBlackness + shade;
    final int rF = Math.round(hF*colour.getRed()),
              gF = Math.round(hF*colour.getGreen()),
              bF = Math.round(hF*colour.getBlue());
    Color fillColour = new Color(rF, gF, bF); 
    
    BufferedImage im1 = Env.createOpaqueImage(kImageSize, kImageSize);
    Graphics2D g2 = im1.createGraphics();
    g2.setColor(edgeColour);
    g2.fillRect(0, 0, kImageSize, kImageSize);
    g2.setColor(fillColour);
    g2.fillRect(1, 1, kImageSize-2, kImageSize-2);
    g2.drawImage(faces, -faceInd*kImageSize, 0, null);
    g2.dispose();

    BufferedImage im2 = Env.createOpaqueImage(kImageSize, kImageSize);
    g2 = im2.createGraphics();
    g2.drawImage(im1, 0, 0, null);
    g2.dispose();
    
    return im2;
    
  } // prepareImage()

  // construct star shapes
  static private void makeSplatShapes() {
    
    kSplatShapes = new Path2D[2];
    
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

      kSplatShapes[version] = star;
    }
    
  } // makeSplatShapes()
  
  // create a buffered image for one splatted face
  static private BufferedImage prepareSplatImage(int colInd) {

    Color colour = kColours[colInd];

    final float hE = kSplatWhiteness;
    final int rE = Math.round((1-hE)*colour.getRed() + hE*255),
              gE = Math.round((1-hE)*colour.getGreen() + hE*255),
              bE = Math.round((1-hE)*colour.getBlue() + hE*255);
    Color edgeColour = new Color(rE, gE, bE); 
    
    final float hF = kSplatBlackness;
    final int rF = Math.round(hF*colour.getRed()),
              gF = Math.round(hF*colour.getGreen()),
              bF = Math.round(hF*colour.getBlue());
    Color fillColour = new Color(rF, gF, bF); 

    Color blankColour = new Color(0,0,0,0);
    
    BufferedImage im1 = Env.createTranslucentImage(kSplatImageSize, 
                                                   kSplatImageSize);
    Graphics2D g2 = im1.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, kSplatImageSize, kSplatImageSize);
    g2.translate(0.5f*kSplatImageSize, 0.5f*kSplatImageSize);

    g2.setColor(edgeColour);
    g2.fill(kSplatShapes[0]);
    g2.setColor(fillColour);
    g2.fill(kSplatShapes[1]);

    g2.dispose();
    
    BufferedImage im2 = Env.createTranslucentImage(kSplatImageSize, 
                                                   kSplatImageSize);
    g2 = im2.createGraphics();
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, kSplatImageSize, kSplatImageSize);
    g2.drawImage(im1, 0, 0, null);
    g2.dispose();
    
    return im2;
  
  } // prepareSplatImage()

  //
  static void advanceColourIndex() {
    
    kColourOffset = ( kColourOffset + 1 ) % kColours.length;
    
  } // advanceColourIndex()
  
  // display a particle
  static void draw(Graphics2D g2, Floxel floxel) {

    initialize();
    
    final int colour = (floxel.mType + kColourOffset) % kColours.length;
    BufferedImage image;
    if ( floxel.mFace < Floxel.numNormalFaces() ) {
      image = kImages[colour][floxel.mShade][floxel.mFace];
    } else {
      assert( floxel.mFace == Floxel.splatFace() );
      image = kSplatImages[colour];
    }
    
    final float xScale = Env.gameWidth() / (float)Env.numTilesX(),
                yScale = Env.gameHeight() / (float)Env.numTilesY();

    int x = (int)Math.floor( floxel.mX * xScale )
            + Env.gameOffsetX() - image.getWidth()/2,
        y = (int)Math.floor( floxel.mY * yScale )
            + Env.gameOffsetY() - image.getHeight()/2;

    g2.drawImage(image, x, y, null);
    
  } // drawFloxel()
  
} // class FloxelPainter

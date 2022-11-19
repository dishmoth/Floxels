/*
 *  MakeSplatImages.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

// create splat image files
public class MakeSplatImages {

  static private final String kSplatImageFile  = "Splat";
  static private final int    kSplatImageMin   = 8,
                              kSplatImageMax   = 22;
  
  static private final int    kSplatStarPoints = 8;
  static private final float  kSplatMaxRadius  = 9.0f,
                              kSplatMinRadius  = 6.0f;
  static private final float  kSplatBlackness  = 0.2f;
  
  // main
  public static void main(String[] arg) {
  
    for ( int size = kSplatImageMin ; size <= kSplatImageMax ; size++ ) {
      BufferedImage im = makeSplatImage(size);
      String fname = kSplatImageFile + size + ".png";
      try {
        ImageIO.write(im, "png", new File(fname));
        System.out.println("Created image file: " + fname);
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    }
      
  } // main()
  
  // make one image
  static private BufferedImage makeSplatImage(int size) {

    final float scale = size/11.0f;
    
    Path2D splatShapes[] = new Path2D[2];
    
    for ( int version = 0 ; version <= 1 ; version++ ) {
      final float inset = (version==0) ? 0.0f : 2.0f;
      
      Path2D star = new Path2D.Float();    
      for ( int k = 0 ; k < kSplatStarPoints ; k++ ) {
        final float theta1 = (2*k+0.5f)*(float)Math.PI/kSplatStarPoints,
                    theta2 = (2*k+1.5f)*(float)Math.PI/kSplatStarPoints;
        final float r1 = scale*( kSplatMaxRadius - inset ),
                    r2 = scale*( kSplatMinRadius - inset );
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

    final int imageSize = 2*(int)Math.ceil(scale*kSplatMaxRadius+0.5f) + 1;
    
    BufferedImage im = new BufferedImage(imageSize, imageSize,
                                         BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = im.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setBackground(blankColour);
    g2.clearRect(0, 0, imageSize, imageSize);
    g2.translate(0.5f*imageSize, 0.5f*imageSize);

    g2.setColor(edgeColour);
    g2.fill(splatShapes[0]);
    g2.setColor(fillColour);
    g2.fill(splatShapes[1]);

    g2.dispose();

    return im;

  } // makeSplatImage()
  
} // class MakeSplatImages

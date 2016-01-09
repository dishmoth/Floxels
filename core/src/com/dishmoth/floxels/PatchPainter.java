/*
 *  PatchPainter.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// class for drawing a stretchable patch behind text
public class PatchPainter {

  // size of the patch border in the pixmap
  private static final int kBorderPixels = 5;
  
  // size of the patch border on screen (relative to tile size)
  private static final float kBorderScale = 0.15f;
  
  // raw image data
  private Pixmap mPixmap;
  
  // size of the patch border on screen
  private float mBorderSize;
  
  // the final patch object
  private NinePatch mPatch;
  
  // constructor
  public PatchPainter(int tileWidth) {
    
    final int size = 3 + 2*kBorderPixels;
    mPixmap = new Pixmap(size, size, Format.RGBA8888);

    Pixmap.Blending oldMode = Pixmap.getBlending();
    Pixmap.setBlending(Pixmap.Blending.None);
    for ( int iy = 0 ; iy < size ; iy++ ) {
       for ( int ix = 0 ; ix < size ; ix++ ) {
         int dx = Math.max(0, Math.abs( ix - size/2 ) - 1),
             dy = Math.max(0, Math.abs( iy - size/2 ) - 1);
         float d = (float)Math.sqrt(dx*dx + dy*dy)/(kBorderPixels - 1);
         float h = 1.0f - Math.min(Math.max(d, 0.0f), 1.0f);
         int white = 255;
         int alpha = Math.round(255*h);
         int colour = ((white<<24)|(white<<16)|(white<<8)|alpha);
         mPixmap.drawPixel(ix, iy, colour);
       }
    }
    Pixmap.setBlending(oldMode);    
    
    mBorderSize = kBorderScale*tileWidth;
    
  } // constructor
  
  // access to the patch pixmap
  public Pixmap pixmap() { return mPixmap; }
  
  // update now that the pixmap has been embedded in a texture
  public void setTexture(Texture texture, int x, int y) {
    
    TextureRegion texRegion = new TextureRegion(texture, 
                                                x+1, 
                                                y+1, 
                                                mPixmap.getWidth()-2, 
                                                mPixmap.getHeight()-2);
    mPatch = new NinePatch(texRegion, 
                           kBorderPixels, kBorderPixels, 
                           kBorderPixels, kBorderPixels);
    mPatch.setLeftWidth(mBorderSize);
    mPatch.setRightWidth(mBorderSize);
    mPatch.setTopHeight(mBorderSize);
    mPatch.setBottomHeight(mBorderSize);
    
  } // setTexture()
  
  // access to the patch object
  public NinePatch patch() { return mPatch; }
  
} // class PatchPainter

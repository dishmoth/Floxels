/*
 *  MazePainter.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// class that draws the wall of a maze
public class MazePainter {

  // range of wall widths used for the texture data
  private static final int kMinPixmapSize = 5,
                           kMaxPixmapSize = 22;
  
  // how tightly walls turn at corners
  private static final float kBendRadius = 0.5f;

  // allow for nice cornering by fiddly the maths some distance from the wall
  private static final float kSmoothRadius = 0.4f;
  
  // how white the walls are
  private static final float kWhiteness = 0.85f;
  
  // number of corner sections (including all flips/rotations)
  private static final int kNumCorners = 15;
  
  // width of the walls in the texture data (including 1-texel boundary)
  private final int mPixmapSize;
  
  // distances (0.0 to 1.0) from the wall at which the colours change 
  private final float mDistWhite,
                      mDistBlack;
  
  // tweak to the end position of a wall 
  private final float mEndShift;
  
  // width of the walls drawn on the screen
  private final int mSize;
  
  // the raw image data
  private Pixmap mPixmap;
  
  // reference to the texture data
  private Texture mTexture;
  
  // reference texture position (if we need to reset the texture) 
  private int mTexBaseX,
              mTexBaseY;
  
  // images for each type of join between wall sections
  // ordered according to: index = (wall goes right) + 2*(wall goes up) 
  //                             + 4*(wall goes left) + 8*(wall goes down) - 1
  private TextureRegion mCorners[];
  
  // two different wall section images
  private TextureRegion mWallHoriz,
                        mWallVert;
  
  // constructor
  public MazePainter(int targetSize) {
    
    assert( targetSize > 0 );
    
    assert( kBendRadius > 0.0f && kBendRadius < 1.0f );
    assert( kSmoothRadius >= 0.0f && kSmoothRadius < kBendRadius );

    targetSize = Math.max(targetSize, kMinPixmapSize);
    final int coreWidth = Math.round(Math.min(targetSize,kMaxPixmapSize)/5.0f);
    mSize = targetSize - ((coreWidth+targetSize)%2);
    
    mPixmapSize = Math.max(Math.min(mSize, kMaxPixmapSize), kMinPixmapSize)+2;
    mPixmap = new Pixmap(8*mPixmapSize, mPixmapSize, Format.RGBA8888);

    final int numWhite = (coreWidth+1)/2;
    final int numBlack = 1;
    mDistWhite = (numWhite-0.5f*(1+mPixmapSize%2))/(0.5f*(mPixmapSize-1));
    mDistBlack = mDistWhite + numBlack/(0.5f*(mPixmapSize-1)); 

    mEndShift = (mPixmapSize%2==0) ? 0.5f*(mDistWhite+mDistBlack) : mDistBlack;
    
    Pixmap.Blending oldMode = Pixmap.getBlending();
    Pixmap.setBlending(Pixmap.Blending.None);
    for ( int k = 0 ; k < 8 ; k++ ) makeCornerImage(k);
    Pixmap.setBlending(oldMode);
    
    //PixmapIO.writePNG(Gdx.files.external("pixmap.png"), mPixmap);

  } // constructor
  
  // draw a corner image on the pixmap
  private void makeCornerImage(int index) {
    
    final int x0 = index*mPixmapSize;
    for ( int iy = 0 ; iy < mPixmapSize ; iy++ ) {
      float y = 2.0f*iy/(mPixmapSize-1.0f) - 1.0f;
      for ( int ix = 0 ; ix < mPixmapSize ; ix++ ) {
        float x = 2.0f*ix/(mPixmapSize-1.0f) - 1.0f;
        
        float d = 0.0f;
        switch (index) {
          case 0: {
            float dx = Math.min(0.0f, x-mEndShift);
            d = (float)Math.sqrt(dx*dx + y*y);
          } break;
          case 1: {
            float dy = Math.min(0.0f, y-mEndShift);
            d = (float)Math.sqrt(x*x + dy*dy);
          } break;
          case 2: {
            d = Math.abs(y);
          } break;
          case 3: {
            d = Math.abs(x);            
          } break;
          case 4: {
            d = Math.abs( calcBend(x,y) );
          } break;
          case 5: {
            d = (x<=0) ? Math.abs(x)
                       : Math.max(0.0f, calcBend(x, Math.abs(y)));
          } break;
          case 6: {
            d = (y<=0) ? Math.abs(y)
                       : Math.max(0.0f, calcBend(Math.abs(x), y));
          } break;
          case 7: {
            d = Math.max(0.0f, calcBend(Math.abs(x), Math.abs(y)));
          } break;
          default: {
            assert(false);
          }
        }
        
        float w = (d <= mDistWhite) ? 1.0f
                : (d >= mDistBlack) ? 0.0f
                                    : (mDistBlack-d)/(mDistBlack-mDistWhite);
        int white = Math.round(w*255*kWhiteness);
        
        float a = (d <= mDistBlack) ? 1.0f
                : (d >= 1.0f)       ? 0.0f
                                    : (1.0f-d)/(1.0f-mDistBlack);
        a = a*a;
        int alpha = Math.round(a*255);
        
        int colour = ((white<<24)|(white<<16)|(white<<8)|alpha);
        mPixmap.drawPixel(x0+ix, iy, colour);
      }
    }
    
  } // makeCornerImage()
  
  // distance values around a simple corner bend
  // (+ve for inside the blend, -ve for outside the bend)
  private float calcBend(float x, float y) {

    if ( x >= kBendRadius && y <= 0.0f ) return y;
    if ( x <= 0.0f && y >= kBendRadius ) return x;
    
    float dx = x - kBendRadius,
          dy = y - kBendRadius;
    if ( dx <= 0.0f && dy <= 0.0f ) {
      float d2 = dx*dx + dy*dy;
      if ( d2 > kBendRadius*kBendRadius ) {
        return ( kBendRadius - (float)Math.sqrt(d2) );
      }
    }

    float u = 1.0f - x,
          v = 1.0f - y;
    if ( u*u + v*v <= kSmoothRadius*kSmoothRadius ) {
      float d = 1.0f - (float)Math.sqrt(u*u+v*v);
      return d;
    }
    
    float t = kSmoothRadius*(kBendRadius-1)/(kSmoothRadius-kBendRadius);
    float p = (u+t)/(1+t),
          q = (v+t)/(1+t);
    float s = kBendRadius/(1+t);
    
    if ( p <= q*(1-s) ) return y;
    if ( q <= p*(1-s) ) return x;

    // solve: ||alpha*(p,q) - (1-s)*(1,1)|| = s
    // alpha=1 => on the bend (distance 0)
    // alpha=inf => in the far corner (distance 1)
    float A = p*p + q*q,
          B = -2*(1-s)*(p+q),
          C = s*s - 4*s + 2;
    float alpha = (-B + (float)Math.sqrt(B*B-4*A*C))/(2*A);
    float d = (1 - 1/alpha)*(1+t);
    
    return d;
    
  } // calcBend()
  
  // access to the walls pixmap
  public Pixmap pixmap() { return mPixmap; }
  
  // update now that the pixmap has been embedded in a texture
  public void setTexture(Texture texture, int x0, int y0) {
    
    assert( mTexture == null );
    mTexture = texture;
    
    mTexBaseX = x0;
    mTexBaseY = y0;
    
    mCorners = new TextureRegion[kNumCorners];
    mCorners[0]  = texRegion(mTexture, 0, false, false); // R
    mCorners[1]  = texRegion(mTexture, 1, false, false); // U
    mCorners[2]  = texRegion(mTexture, 4, false, false); // UR
    mCorners[3]  = texRegion(mTexture, 0, true,  false); // L
    mCorners[4]  = texRegion(mTexture, 2, false, false); // LR
    mCorners[5]  = texRegion(mTexture, 4, true,  false); // LU
    mCorners[6]  = texRegion(mTexture, 6, false, false); // LUR
    mCorners[7]  = texRegion(mTexture, 1, false, true);  // D
    mCorners[8]  = texRegion(mTexture, 4, false, true);  // DR
    mCorners[9]  = texRegion(mTexture, 3, false, false); // DU
    mCorners[10] = texRegion(mTexture, 5, false, false); // DUR
    mCorners[11] = texRegion(mTexture, 4, true,  true);  // DL
    mCorners[12] = texRegion(mTexture, 6, false, true);  // DLR
    mCorners[13] = texRegion(mTexture, 5, true,  false); // DLU
    mCorners[14] = texRegion(mTexture, 7, false, false); // DLUR

    mWallHoriz = mCorners[4];
    mWallVert  = mCorners[9];
    
  } // setTexture()
  
  // replace the texture (following game pause/resume)
  public void resetTexture(Texture texture) { 

    mTexture = null;
    setTexture(texture, mTexBaseX, mTexBaseY);
    
  } // resetTexture()
  
  // extract part of the texture for one corner
  private TextureRegion texRegion(Texture texture, int index, 
                                  boolean flipX, boolean flipY) {
    
    assert( index >= 0 && index < 8 );
    
    int x  = (mTexBaseX+1) + index*mPixmapSize,
        y  = (mTexBaseY+1),
        dx = mPixmapSize - 2,
        dy = mPixmapSize - 2;
    
    if ( flipX ) {
      x += dx;
      dx = -dx;
    }
    if ( flipY ) {
      y += dy;
      dy = -dy;
    }
    
    return new TextureRegion(mTexture, x, y, dx, dy);
    
  } // texRegion()
  
  // return a reference to a wall texture
  public TextureRegion wallTexture(boolean vertical) {
    
    assert( mTexture != null );
    return ( vertical ? mWallVert : mWallHoriz );
    
  } // wallTexture()
  
  // display the maze walls
  public void draw(SpriteBatch batch, MazeData maze) {

    final int delta = Env.tileWidth();
    
    final int xStart = Env.gameOffsetX() - mSize/2,
              yStart = Env.gameOffsetY() - mSize/2;
    
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
        if ( index >= 0 ) batch.draw(mCorners[index], x,y, mSize,mSize);
        if ( right )      batch.draw(mWallHoriz, x+mSize,y, delta-mSize,mSize);
        if ( down )       batch.draw(mWallVert, x,y+mSize, mSize,delta-mSize);
        x += delta;
      }
      y += delta;
    }
    
  } // draw()
  
} // class MazePainter

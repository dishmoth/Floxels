/*
 *  Text.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

// object holding some text to display on the screen
public class Text {

  // the main text object
  private BitmapFontCache mText;
  
  // nominal top-left position of text (screen coords)
  private float mXPos,
                mYPos;
  
  // true bounds of the final text (screen coords, based on mesh vertices)
  private float mXMin,
                mYMin,
                mXMax,
                mYMax;
  
  // for debug
  private static final boolean kDrawBackground = false;
  private static ShapeRenderer kShapeRenderer = kDrawBackground 
                                                ? new ShapeRenderer()
                                                : null;
  
  // constructor
  public Text(String string, int x, int y) {
    
    mText = new BitmapFontCache( Env.painter().fonts().smallFont() );
    
    mXPos = x;
    mYPos = y;
    
    set(string);
    
  } // constructor
  
  // set the text string
  public void set(String string) {
    
    mText.setText(string, mXPos, mYPos);
    updateBounds();
    
  } // set()

  // calculate the actual bounds of the text
  private void updateBounds() {
    
    mXMin = mYMin = +Float.MAX_VALUE;
    mXMax = mYMax = -Float.MAX_VALUE;

    int verticesSize = 0;
    
    Array<GlyphLayout> layouts = mText.getLayouts();
    for ( int n = 0 ; n < layouts.size ; n++ ) {
      GlyphLayout layout = layouts.get(n);
      for ( int k = 0 ; k < layout.runs.size ; k++ ) {
        Array<Glyph> glyphs = layout.runs.get(k).glyphs;
        for (int ii = 0, nn = glyphs.size; ii < nn; ii++) {
          verticesSize += 20;
        }
      }
    }

    float vertices[] = mText.getVertices();
    for ( int i = 0 ; i < verticesSize ; i += 5 ) {
      float x = vertices[i];
      float y = vertices[i+1];
      mXMin = Math.min(mXMin, x);
      mYMin = Math.min(mYMin, y);
      mXMax = Math.max(mXMax, x);
      mYMax = Math.max(mYMax, y);
    }
    
  } // updateBounds()
  
  // access to position and size
  public float xPos() { return mXPos; }
  public float yPos() { return mYPos; }
  public float xMin() { return mXMin; }
  public float yMin() { return mYMin; }
  public float xMax() { return mXMax; }
  public float yMax() { return mYMax; }
  
  // move the text
  public void translate(float dx, float dy) {
    
    mText.translate(dx, dy);
    mXPos += dx;
    mYPos += dy;
    mXMin += dx;
    mYMin += dy;
    mXMax += dx;
    mYMax += dy;
    
  } // translate()
  
  // set the colour (red, green, blue, 0.0 to 1.0)
  public void setColour(float r, float g, float b, float alpha) {
    
    mText.setColor(r, g, b, alpha);
    mText.setColors(r, g, b, alpha);
    
  } // setColour()
  
  // display the text
  public void draw(SpriteBatch batch) {
    
    if ( kDrawBackground ) {
      batch.end();
      kShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
      kShapeRenderer.setColor(1, 0, 1, 1);
      kShapeRenderer.rect(mXMin, mYMin, mXMax-mXMin, mYMax-mYMin);
      kShapeRenderer.end();
      batch.begin();
    }

    mText.draw(batch);
    
  } // draw()
  
} // class Text

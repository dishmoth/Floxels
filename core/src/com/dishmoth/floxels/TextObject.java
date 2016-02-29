/*
 *  TextObject.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// game object for info text
public class TextObject extends Sprite {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 75; 

  // how long the fade takes (seconds)
  static private final float kAlphaInTime  = 0.6f,
                             kFadeOutDelay = 0.3f,
                             kAlphaOutTime = 0.3f;

  // size of the patch border around the text (tile units)
  static private final float kPatchBorder = 0.35f;
  
  // transparency of the background patch
  static private final float kPatchRed   = 189/255.0f,
                             kPatchGreen = 188/255.0f,
                             kPatchBlue  = 141/255.0f, 
                             kPatchAlpha = 0.85f;
  
  // object for drawing the text
  private Text mText;

  // colour the background behind the text
  private boolean mUseBackground;
  
  // range of the background patch
  private float mXMin,
                mXMax,
                mYMin,
                mYMax;
  
  // colour of the background patch
  private Color mBackgroundColour;
  
  // image appearing or disappearing
  private boolean mFadeIn;
  
  // time until the image appears or disappears (or zero)
  private float mFadeTimer;
  
  // current blend
  private float mAlpha;
  
  // constructor (position in tile units)
  public TextObject(String string, float yPos, 
                    boolean useBackground, float fadeInDelay) {
    
    super(kScreenLayer);
    
    mText = new Text(string, 0, 0, Text.TextSize.MEDIUM);
    
    float x = Env.gameOffsetX() + Math.round( 0.5f*Env.gameWidth() ),
          y = Env.gameOffsetY() + Math.round( yPos*Env.tileWidth() );
    
    mText.translate( x - 0.5f*(mText.xMin()+mText.xMax()),
                     y - 0.5f*(mText.yMin()+mText.yMax()) );

    if ( fadeInDelay > 0.0f ) {
      mFadeIn = true;
      mFadeTimer = fadeInDelay + kAlphaInTime;
      mAlpha = 0.0f;
    } else {
      mFadeIn = false;
      mFadeTimer = 0.0f;
      mAlpha = 1.0f;
    }
    
    mText.setColour(0.0f, 0.0f, 0.0f, mAlpha);

    mUseBackground = useBackground;

    mBackgroundColour = new Color(kPatchRed, kPatchGreen, 
                                  kPatchBlue, kPatchAlpha);
    
    final float border = kPatchBorder*Env.tileWidth();
    mXMin = mText.xMin() - border;
    mXMax = mText.xMax() + border;
    mYMin = mText.yMin() - border;
    mYMax = mText.yMax() + border;
    
  } // constructor

  // access to position (tile units)
  public float xMin() { return mXMin/Env.tileWidth(); }
  public float xMax() { return mXMax/Env.tileWidth(); }
  public float yMin() { return mYMin/Env.tileWidth(); }
  public float yMax() { return mYMax/Env.tileWidth(); }
  
  // move the text (tile units)
  public void translate(float dx, float dy) {
    
    dx *= Env.tileWidth();
    dy *= Env.tileWidth();
    mText.translate(dx, dy);
    mXMin += dx;
    mYMin += dy;
    mXMax += dx;
    mYMax += dy;
    
  } // translate()
  
  // make the image go away
  public void fade() {

    assert( !mFadeIn );
    assert( mFadeTimer == 0.0f );
    mFadeTimer = kFadeOutDelay + kAlphaOutTime;
    
  } // fade()
  
  // whether the image is currently fading out
  public boolean fading() { return (!mFadeIn && mFadeTimer > 0.0f); }
  
  // fade the text in or out
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    if ( mFadeTimer > 0.0f ) {
      mFadeTimer -= Env.TICK_TIME;

      if ( mFadeIn ) {
        if ( mFadeTimer <= 0.0f ) {
          mFadeTimer = 0.0f;
          mFadeIn = false;
        }
        if ( mFadeTimer < kAlphaInTime ) {
          mAlpha = 1.0f - mFadeTimer/kAlphaInTime;
          mText.setColour(0.0f, 0.0f, 0.0f, mAlpha);
        }
      } else {
        if ( mFadeTimer <= 0.0f ) {
          mFadeTimer = 0.0f;
          killTheseSprites.add(this);
        } 
        if ( mFadeTimer < kAlphaOutTime ) {
          mAlpha = mFadeTimer/kAlphaOutTime;
          mText.setColour(0.0f, 0.0f, 0.0f, mAlpha);
        }
      }
      
    }
    
  } // Sprite.advance()

  // display the text
  @Override
  public void draw(SpriteBatch batch) {

    if ( mUseBackground ) {
      NinePatch patch = Env.painter().patchPainter().patch();
      mBackgroundColour.a = kPatchAlpha*mAlpha;
      patch.setColor(mBackgroundColour);
      patch.draw(batch, mXMin, mYMin, mXMax-mXMin, mYMax-mYMin);
    }
    
    mText.draw(batch);
    
  } // Sprite.draw()

} // class TextObject

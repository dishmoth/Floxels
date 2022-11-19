/*
 *  TitleImage.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// image and text for the title screen
public class TitleImage extends Sprite {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 80; 

  // scale of the title (in tile units)
  static private final float kSize = 3.7f;
  
  // tweak the brightness of the image
  static private final float kTintShade = 0.85f;

  // how long the fade takes (seconds)
  static private final float kFadeTime  = 0.6f,
                             kAlphaTime = 0.3f;
  
  // size and position of the image
  private int mImageXPos,
              mImageYPos,
              mImageWidth,
              mImageHeight;
  
  // the title image texture data
  private TextureRegion mImage;
  
  // narcissistic author credit
  private Text mText;
  
  // time until the image vanishes (or zero)
  private float mFadeTimer;
  
  // current blend
  private float mAlpha;
  
  // constructor
  public TitleImage() {
    
    super(kScreenLayer);
    
    mText = new Text("by dishmoth", 50, 50, Text.TextSize.SMALL);
    mText.setColour(0.0f, 0.0f, 0.0f, 1.0f);
    
    mImageWidth = Math.round( kSize*Env.tileWidth() );
    mImage = Env.painter().titlePainter().region(mImageWidth);
    
    float delta = Math.abs(mImageWidth - mImage.getRegionWidth());
    if ( delta/mImageWidth < 0.05f ) mImageWidth = mImage.getRegionWidth(); 
    
    float scale = mImageWidth/(float)mImage.getRegionWidth();
    mImageHeight = Math.round( scale*mImage.getRegionHeight() );

    int totalHeight = mImageHeight + Math.round(mText.yMax() - mText.yMin());
    
    int xMid = Env.gameOffsetX() + Env.gameWidth()/2,
        yMid = Env.gameOffsetY() + Env.gameHeight()/2;
    int yImage = yMid + totalHeight/2 - mImageHeight/2,
        yText = yMid - totalHeight/2 + Math.round(mText.yMax()-mText.yMin())/2;

    mImageXPos = xMid - mImageWidth/2; 
    mImageYPos = yImage - mImageHeight/2; 

    mText.translate(xMid - (mText.xMin()+mText.xMax())/2, 
                    yText - (mText.yMin()+mText.yMax())/2);
    
    mFadeTimer = 0.0f;
    mAlpha = 1.0f;
    
  } // constructor
  
  // make the image go away
  public void fade() {
    
    assert( mFadeTimer == 0.0f );
    mFadeTimer = kFadeTime;
    
  } // fade()
  
  // animate the image
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    if ( mFadeTimer > 0.0f ) {
      mFadeTimer -= Env.TICK_TIME;
      if ( mFadeTimer < kAlphaTime ) {
        mAlpha = mFadeTimer/kAlphaTime;
        mText.setColour(0.0f, 0.0f, 0.0f, mAlpha);
      }
      if ( mFadeTimer <= 0.0f ) {
        killTheseSprites.add(this);
      }
    }
    
  } // Sprite.advance()

  // display the image
  @Override
  public void draw(SpriteBatch batch) {

    batch.setColor(kTintShade, kTintShade, kTintShade, mAlpha);
    batch.draw(mImage, mImageXPos, mImageYPos, mImageWidth, mImageHeight);
    batch.setColor(Color.WHITE);

    mText.draw(batch);

  } // Sprite.draw()

} // class TitleImage

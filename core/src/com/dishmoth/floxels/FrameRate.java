/*
 *  FrameRate.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// track and display the frame rate
public class FrameRate extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 10;
  
  // position of the text (from bottom-right, relative to tile width)
  private static final float kOffsetX = 0.09f,
                             kOffsetY = 0.09f;
  
  // the message
  private Text mText;
  
  // frame rate as percentage of target
  private int mPercentage;
  
  // constructor
  public FrameRate() {
    
    super(kScreenLayer);
    
    mPercentage = 0;
    
    int dx = Math.round(kOffsetX*Env.tileWidth()),
        dy = Math.round(kOffsetY*Env.tileWidth());
    int x0 = Env.gameOffsetX()+Env.gameWidth()-dx,
        y0 = Env.gameOffsetY()+dy;
    
    mText = new Text("Slow: 55%", x0, y0, Text.TextSize.SMALL);
    mText.translate(Math.round(x0-mText.xMax()), 
                    Math.round(y0-mText.yMin()));
    
    mText.setColour(0.0f, 0.0f, 0.0f, 1.0f);
    
  } // constructor

  // update the text
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    mPercentage = Env.frameRate();
    mText.set("Slow: " + mPercentage + "%");

  } // Sprite.advance()

  // display some numbers
  @Override
  public void draw(SpriteBatch batch) {

    if ( mPercentage <= 0 || mPercentage >= 100 ) return;
    mText.draw(batch);

  } // Sprite.draw()

} // class FrameRate

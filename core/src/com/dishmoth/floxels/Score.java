/*
 *  Score.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// track and display the current score
public class Score extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 10;
  
  // position of the text (from top-left/top-right, relative to tile width)
  private static final float kOffsetX = 0.09f,
                             kOffsetY = 0.09f;
  
  // time (seconds) for the score to be banked
  private static final float kTransitionDelay = 0.6f,
                             kTransitionTime  = 0.7f;
  
  // score (banked value and current value)
  private int mBankValue,
              mValue;

  // the numbers to display (banked and current)
  private Text mBankText,
               mText;
  
  // time remaining for the transition (or zero)
  private float mTransitionTimer;
  
  // constructor
  public Score() {
    
    super(kScreenLayer);
    
    int dx = Math.round(kOffsetX*Env.tileWidth()),
        dy = Math.round(kOffsetY*Env.tileWidth());
    int x1 = Env.gameOffsetX()+dx,
        x2 = Env.gameOffsetX()+Env.gameWidth()-dx, 
        y  = Env.gameOffsetY()+Env.gameHeight()-dy;
    
    mText = new Text("12345", x1, y);
    mText.translate(Math.round(x1-mText.xMin()), 
                    Math.round(y-mText.yMax()));
    mText.setColour(0.0f, 0.0f, 0.0f, 1.0f);
    
    mBankText = new Text("12345", x2, y);
    mBankText.translate(Math.round(x2-mBankText.xMax()), 
                        Math.round(y-mBankText.yMax()));
    mBankText.setColour(0.0f, 0.0f, 0.0f, 1.0f);
    
    reset();

  } // constructor

  // set the score to zero
  public void reset() { 
    
    mBankValue = mValue = 0;
    mTransitionTimer = 0.0f;
    update(); 
    
  } // reset()
  
  // change the text
  private void update() {
    
    mText.set(Integer.toString(mValue));
    
    float xOld = mBankText.xMax();
    mBankText.set(Integer.toString(mBankValue));
    mBankText.translate(Math.round(xOld-mBankText.xMax()), 0.0f);
    
  } // update()
  
  // add the current value to the base value
  public void bank() {
    
    mTransitionTimer = kTransitionDelay + kTransitionTime;
    
  } // bank()
  
  // specify the current value
  public void set(int v) {
    
    if ( mValue != v ) {
      assert( mTransitionTimer == 0.0f );
      mValue = v;
      update(); 
    } 
    
  } // set()
  
  // gradually move the score to the bank
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    if ( mTransitionTimer > 0.0f ) {
      float dt = Env.TICK_TIME;
      mTransitionTimer -= dt;
      if ( mTransitionTimer < kTransitionTime ) {
        int n;
        if ( mTransitionTimer <= 0.0f ) {
          mTransitionTimer = 0.0f;
          n = mValue;
        } else {
          n = Math.round(mValue*dt/(mTransitionTimer+dt));  
        }
        mValue -= n;
        mBankValue += n;
        update();
      }
    }
    
  } // Sprite.advance()

  // display some numbers
  @Override
  public void draw(SpriteBatch batch) {
    
    if ( mBankValue > 0 ) mBankText.draw(batch);
    if ( mValue > 0 )     mText.draw(batch);

  } // Sprite.draw()

} // class Score

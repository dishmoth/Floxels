/*
 *  Score.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// track and display the current score
public class Score extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 10;
  
  // images of numbers
  private static final int     kImageOffsets[] = {  0,  9, 18, 27, 35,
                                                   45, 53, 62, 71, 80 },
                               kImageWidths[]  = {  6,  6,  6,  5,  7, 
                                                    5,  6,  6,  6,  6 };
  private static int           kImageHeight    = 0;
//  private static BufferedImage kImages[]       = null;
  
  // position of the score (relative to bottom-left of game area)
  private static final int kOffsetX = 6,
                           kOffsetY = 7;
  
  // score (base value plus current value)
  private int mBaseValue,
              mValue;

//  // prepare text images
//  static public void initialize() {
//
//    if ( kImages != null ) return;
//
//    BufferedImage sourceImage = Env.resources().loadImage("Numbers.png");
//    kImageHeight = sourceImage.getHeight();
//    
//    assert( kImageOffsets.length == kImageWidths.length );
//    Color blankColour = new Color(0, 0, 0, 0);
//    
//    kImages = new BufferedImage[kImageOffsets.length];
//    for ( int k = 0 ; k < kImages.length ; k++ ) {
//      BufferedImage im = Env.createTranslucentImage(kImageWidths[k], 
//                                                    kImageHeight);
//      Graphics2D g2 = im.createGraphics();
//      g2.setBackground(blankColour);
//      g2.clearRect(0, 0, im.getWidth(), im.getHeight());
//      g2.drawImage(sourceImage, -kImageOffsets[k], 0, null);
//      g2.dispose();
//      kImages[k] = im;
//    }
//    
//  } // initialize()
  
  // constructor
  public Score() {
    
    super(kScreenLayer);
    
//    initialize();

    reset();
    
  } // constructor

  // set the score to zero
  public void reset() { mBaseValue = mValue = 0; }
  
  // add the current value to the base value
  public void fixBaseValue() { mBaseValue += mValue; mValue = 0; }
  
  // specify the current value
  public void setCurrentValue(int v) { mValue = v; } 
  
  // nothing to do here
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
  } // Sprite.advance()

  // display some numbers
  @Override
  public void draw(SpriteBatch batch) {

    final int value = mBaseValue + mValue;
    assert( value >= 0 );

    final int x0 = Env.gameOffsetX() + kOffsetX,
              y0 = Env.gameOffsetY() + Env.gameHeight() 
                   - kImageHeight - kOffsetY;

    int base = 10;
    while ( base <= value ) base *= 10;
    
    int x = x0;
    while ( base > 1 ) {
      base /= 10;
      final int n = (value/base) % 10;
      //g2.drawImage(kImages[n], x, y0, null);
      x += kImageWidths[n];
    }
    
  } // Sprite.draw()

} // class Score

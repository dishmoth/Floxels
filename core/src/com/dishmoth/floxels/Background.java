/*
 *  Background.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// static background image 
public class Background extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 0;
  
  // how much the noise is stretched to cover one tile
  private static final float kNoiseRepeats = 0.23f;
  
  // constructor
  public Background() {
    
    super(kScreenLayer);
    
  } // constructor
  
  // nothing to do
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
  } // Sprite.advance()

  // display the image
  @Override
  public void draw(SpriteBatch batch) {

    Texture tex = Env.painter().backgroundPainter().texture();
    batch.draw(tex,
               Env.gameOffsetX(), Env.gameOffsetY(),
               Env.gameWidth(), Env.gameHeight(),
               0.0f, 0.0f,
               Env.numTilesX()*kNoiseRepeats, Env.numTilesY()*kNoiseRepeats);

  } // Sprite.draw()
  
} // class Background

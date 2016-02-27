/*
 *  Background.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// static background image 
public class Background extends Sprite {

  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 0;
  
  // how much the noise is stretched to cover the game screen
  private static final float kNoiseRepeats = 2.3f;
  
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
               kNoiseRepeats, kNoiseRepeats);

  } // Sprite.draw()
  
} // class Background

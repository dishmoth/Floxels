/*
 *  SpriteManager.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.*;
import java.util.*;

// all of the active Sprites in the game are under the care of this manager
// the SpriteManager's two main functions are to update all of Sprites
// and to draw them all in the correct order
// the SpriteManager itself is under the control of the GameManager
public class SpriteManager {

  // the Sprites we are managing
  // Sprites are ordered according to their "screen layer" values
  private LinkedList<Sprite> mSpriteList;
  
  // objects local to advance() (create here to reduce garbage collection)
  private LinkedList<Sprite> mAddTheseSprites  = new LinkedList<Sprite>(),
                             mKillTheseSprites = new LinkedList<Sprite>();
  private LinkedList<StoryEvent> mNewStoryEvents = new LinkedList<StoryEvent>();
  
  // constructor
  public SpriteManager() {
    
    mSpriteList = new LinkedList<Sprite>();
    
  } // constructor

  // allow access to the Sprite list (the list itself should not be modified)
  public LinkedList<Sprite> list() { return mSpriteList; }

  // add a new Sprite to the list we are managing
  // order within the list is maintained
  // Sprites already in the list are alerted to the presence of the new Sprite
  public void addSprite(Sprite newSprite) {
    
    if ( mSpriteList.isEmpty() ) {
      mSpriteList.add(newSprite);
      return;
    }

    // register new sprite with all existing sprites
    for ( Sprite s : mSpriteList ) {
      s.observeArrival(newSprite);
      newSprite.observeArrival(s);
    }
    
    // sprites in list are ordered with screenLayer increasing
    // most recently added comes later in case of a tie
    final int newLayer = newSprite.screenLayer();
    int i;
    for ( i = mSpriteList.size() ; i > 0 ; i-- ) {
      if ( mSpriteList.get(i-1).screenLayer() <= newLayer ) {
        break;
      }
    }
    mSpriteList.add(i, newSprite);
  
  } // addSprite()

  // add multiple Sprites to our list (simply calls addSprite() above)
  public void addSprites(LinkedList<Sprite> newSprites) {
    
    if ( newSprites == null ) return;
    for ( Sprite s : newSprites ) addSprite(s);
    
  } // addSprites()

  // we are no longer managing a Sprite
  // inform the remaining Sprites of this
  public void removeSprite(Sprite deadSprite) {
    
    mSpriteList.remove(deadSprite);
    for ( Sprite s : mSpriteList ) {
      s.observeDeparture(deadSprite);
      deadSprite.observeDeparture(s);
    }
    
  } // removeSprite()
  
  // remove multiple Sprites from our list (simply calls removeSprite() above)
  public void removeSprites(LinkedList<Sprite> deadSprites) {

    for ( Sprite s : deadSprites ) removeSprite(s);
    
  } // removeSprites()

  // all existing Sprites are removed from the game
  public void removeAllSprites() {
    
    mSpriteList.clear();
    
  } // removeAll()

  // return a Sprite of the specified type, or null if none present
  // Sprite returned is the first in the manager's list
  public Sprite findSpriteOfType(Class<?> cl) {

    for ( Sprite s : mSpriteList ) if ( s.getClass() == cl ) return s;
    return null;
        
  } // findSpriteOfType()

  // set or reset the "advance" behaviour for all Sprites
  public void disableAdvanceForAll() { setAdvanceDisabledFlag(true); }
  public void enableAdvanceForAll() { setAdvanceDisabledFlag(false); }
  protected void setAdvanceDisabledFlag(boolean val) {
    for ( Sprite s : mSpriteList ) s.mAdvanceDisabled = val;
  }
  
  // set or reset the "draw" behaviour for all Sprites
  public void disableDrawForAll() { setDrawDisabledFlag(true); }
  public void enableDrawForAll() { setDrawDisabledFlag(false); }
  protected void setDrawDisabledFlag(boolean val) {
    for ( Sprite s : mSpriteList ) s.mDrawDisabled = val;
  }

  // update all of the Sprites in our list
  // there are three stage to this: advance, interact, aftermath
  // if a Sprite's screen layer value has changed then the list is reordered
  public void advance(LinkedList<StoryEvent> addedStoryEvents) {

    int     prevScreenLayer = Integer.MIN_VALUE;
    boolean orderHasChanged = false;

    // stage 1: advance all sprites
    mAddTheseSprites.clear();
    mKillTheseSprites.clear();
    mNewStoryEvents.clear();
    for ( Sprite s : mSpriteList ) {
      if ( s.mAdvanceDisabled ) continue;
      s.advance(mAddTheseSprites, mKillTheseSprites, mNewStoryEvents);
    }
    addSprites(mAddTheseSprites);
    removeSprites(mKillTheseSprites);
    addedStoryEvents.addAll(mNewStoryEvents);
    
    // stage 2: allow sprites to interact with each other
    for ( Sprite s : mSpriteList ) {
      if ( s.mAdvanceDisabled ) continue;
      s.interact();
    }

    // stage 3: aftermath of the interactions
    mAddTheseSprites.clear();
    mKillTheseSprites.clear();
    mNewStoryEvents.clear();
    for ( Sprite s : mSpriteList ) {
      if ( s.screenLayer() < prevScreenLayer ) orderHasChanged = true;
      prevScreenLayer = s.screenLayer();
      if ( s.mAdvanceDisabled ) continue;
      s.aftermath(mAddTheseSprites, mKillTheseSprites, mNewStoryEvents);
    }
    addSprites(mAddTheseSprites);
    removeSprites(mKillTheseSprites);    
    addedStoryEvents.addAll(mNewStoryEvents);

    // tidy up: reorder the list if any "screen layer" value has changed
    if ( orderHasChanged ) Collections.sort(mSpriteList);
    
  } // advance()
  
  // display the Sprites
  // the drawing order is based on the Sprites' "screen layer" values
  // if the Sprite has a SpriteWindow then that clips the drawing
  public void draw(Graphics2D g2) {
    
    for ( Sprite s : mSpriteList ) {
      if ( s.mDrawDisabled ) continue;
      s.draw(g2);
    }
    
  } // draw()
    
} // class SpriteManager

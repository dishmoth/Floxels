/*
 *  QuitStory.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

// game loop while confirming whether or not the player wants to quit
public class QuitStory extends Story {

  // which story we've come from (and may return to)
  private Story mOldStory;
  
  // check whether the 'back' button has been pressed
  private boolean mQuitTriggered;  
  
  // check whether a mouse button has been pressed
  private boolean mMouseTriggered;

  // text objects
  private TextObject mText[];
  
  // delay continuing the story by one frame
  private boolean mFinished;
  
  // which sprites we've frozen
  private ArrayList<Sprite> mAdvanceDisabledSprites = new ArrayList<Sprite>(),
                            mDrawDisabledSprites = new ArrayList<Sprite>();
  
  // constructor
  public QuitStory(Story oldStory) {

    mOldStory = oldStory;
    mFinished = false;
    
  } // constructor
  
  // process events and advance
  @Override
  public Story advance(LinkedList<StoryEvent> storyEvents,
                       SpriteManager spriteManager) {
    
    if ( mFinished ) return mOldStory;
    
    // process the story event list
    for ( Iterator<StoryEvent> it = storyEvents.iterator() ; it.hasNext() ; ) {
      StoryEvent event = it.next();

      if ( event instanceof Story.EventStoryBegins ) {
        // first frame of the story, so set everything up
        freezeSprites(spriteManager);
        addText(spriteManager);
        mQuitTriggered = mMouseTriggered = true;
        Env.sounds().stopAll();
        it.remove();
      } // Story.EventStoryBegins

      else {
        //Env.debug("event ignored: " + event.getClass());
        //it.remove();
      }
      
    } // for each story event
    
    // check the 'back' button
    if ( Env.quitButton() ) {
      if ( !mQuitTriggered ) {
        mFinished = true;
      }
      mQuitTriggered = true;
    } else {
      mQuitTriggered = false;
    }

    // check the mouse
    Env.mouse().updateState();
    MouseMonitor.State state = Env.mouse().getState();
    if ( state.b ) {
      if ( !mMouseTriggered ) {
        final float x = (state.x - Env.gameOffsetX())/(float)Env.tileWidth(),
                    y = (state.y - Env.gameOffsetY())/(float)Env.tileWidth();
        if ( x >= mText[1].xMin() && x <= mText[1].xMax() &&
             y >= mText[1].yMin() && y <= mText[1].yMax() ) {
          Env.exit();
        } else if ( x >= 0 && x < Env.numTilesX() && 
                    y >= 0 && y < Env.numTilesY() ) {
          mFinished = true;
        }
      }
      mMouseTriggered = true;
    } else {
      mMouseTriggered = false;
    }

    // tidy up, we're leaving soon
    if ( mFinished ) {
      Env.mouse().disableMouse( Env.TICKS_PER_SEC/2 );
      unfreezeSprites();
      removeText(spriteManager);
    }
    
    // no change of story
    return null;
    
  } // Story.advance()

  // display some text
  private void addText(SpriteManager spriteManager) {
    
    float y0 = 0.5f*Env.numTilesY(),
          dy = 0.75f,
          dx = 1.0f;
    
    mText = new TextObject[]{ 
                      new TextObject("Quit?", y0+dy, true, 0.0f),
                      new TextObject(" Yes ", y0-dy, true, 0.0f),
                      new TextObject(" No ", y0-dy, true, 0.0f)
                      };
    mText[1].translate(-dx, 0.0f);
    mText[2].translate(+dx, 0.0f);
    
    for ( TextObject tx : mText ) spriteManager.addSprite(tx);
    
  } // addInstructions()

  // remove the text objects (also clean up any fading text)
  private void removeText(SpriteManager spriteManager) {

    for ( TextObject tx : mText ) spriteManager.removeSprite(tx);

    ArrayList<Sprite> spritesToDelete = new ArrayList<Sprite>();
    for ( Sprite sp : spriteManager.list() ) {
      if ( sp instanceof TextObject ) {
        TextObject tx = (TextObject)sp;
        if ( tx.fading() ) spritesToDelete.add(tx);
      }
    }
    for ( Sprite sp : spritesToDelete ) spriteManager.removeSprite(sp);
    
  } // removeText()
  
  // disable advance and draw for all sprites
  private void freezeSprites(SpriteManager spriteManager) {

    for ( Sprite sp : spriteManager.list() ) {
      if ( sp.mAdvanceDisabled == false) {
        sp.mAdvanceDisabled = true;
        mAdvanceDisabledSprites.add(sp);
      }
    }
    for ( Sprite sp : spriteManager.list() ) {
      if ( sp instanceof TextObject ||
           sp instanceof TitleImage ) {
        if ( sp.mDrawDisabled == false) {
          sp.mDrawDisabled = true;
          mDrawDisabledSprites.add(sp);
        }
      }
    }
    
  } // freezeSprites()
  
  // re-enable advance and draw for all sprites
  private void unfreezeSprites() {
    
    for ( Sprite sp : mAdvanceDisabledSprites ) {
      sp.mAdvanceDisabled = false;
    }
    for ( Sprite sp : mDrawDisabledSprites ) {
      sp.mDrawDisabled = false;
    }
    
  } // unfreezeSprites()
  
} // class QuitStory

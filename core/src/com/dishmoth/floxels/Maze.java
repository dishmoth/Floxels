/*
 *  Maze.java
 *  Copyright Simon Hern 2015
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// sprite object for animating and displaying the maze data
public class Maze extends Sprite {

  // story event: the maze walls have changed
  public static class EventMazeChanged extends StoryEvent {
  } // Maze.EventMazeChanged
  
  // how this sprite is drawn relative to others
  private static final int kScreenLayer = 5;

  // seconds until the next maze change
  private static final float kChangeDelay      = 0.015f,
                             kChangeFirstDelay = 1.0f;
  
  // current maze number (-1 for title screen)
  private int mMazeNum;
  
  // the underlying maze structure
  private MazeData mMazeData;
  
  // when changing the maze, these are the changes remaining to be made
  private LinkedList<MazeData.Delta> mDeltas;
  
  // seconds until the next maze alteration (or zero)
  private float mChangeTimer;
  
  // constructor
  public Maze() {

    super(kScreenLayer);

    mMazeNum = -1;
    
    mMazeData = Mazes.get(mMazeNum);

    mDeltas = new LinkedList<MazeData.Delta>();
    mChangeTimer = 0.0f;
    
  } // constructor

  // access to the maze data
  public MazeData data() { return mMazeData; }
  
  // start the transformation to the next maze
  public void changeToNext() {
  
    mMazeNum += 1;

    assert( mDeltas.isEmpty() );
    MazeData nextMaze = Mazes.get(mMazeNum);
    mMazeData.collectDifferences(nextMaze, mDeltas);
    mChangeTimer = kChangeFirstDelay;
        
  } // changeToNext()

  // whether the maze is currently changing
  public boolean changing() { return !mDeltas.isEmpty(); }

  // modify the maze for a new level
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    if ( mChangeTimer > 0.0f ) {
      mChangeTimer -= Env.TICK_TIME;
      boolean changed = false;
      while ( mChangeTimer <= 0.0f ) {
        mMazeData.applyDifference(mDeltas.pop());
        changed = true;
        if ( mDeltas.isEmpty() ) {
          mChangeTimer = 0.0f;
          break;
        } else {
          mChangeTimer += kChangeDelay;
        }
      }
      if ( changed ) newStoryEvents.add( new EventMazeChanged() );
    }
    
  } // Sprite.advance()

  // display the maze
  @Override
  public void draw(SpriteBatch batch) {

    Env.painter().mazePainter().draw(batch, mMazeData);
    
  } // Sprite.draw()
  
} // class Maze

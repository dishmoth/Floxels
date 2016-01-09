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

  // seconds until the change starts, and how long it takes
  private static final float kChangeInitialDelay = 1.0f,
                             kChangeMorphDelay   = 1.0f;
  
  // current maze number (-1 for title screen)
  private int mMazeNum;
  
  // the underlying maze structure
  private MazeData mMazeData;
  
  // when changing the maze, these are the changes remaining to be made
  private LinkedList<MazeData.Delta> mDeltas;

  // total number of changes that need to be made
  private int mNumDeltas;
  
  // seconds until the maze changes start (or zero)
  private float mChangeTimer;
  
  // time (seconds) for the maze to change
  static public float changeTime() {
    
    return (kChangeInitialDelay + kChangeMorphDelay);
    
  } // Maze.changeTime()
  
  // constructor
  public Maze() {

    super(kScreenLayer);

    mMazeNum = -1;
    
    mMazeData = Mazes.get(mMazeNum);

    mDeltas = new LinkedList<MazeData.Delta>();
    mNumDeltas = 0;
    mChangeTimer = 0.0f;
    
  } // constructor

  // access to the maze data
  public MazeData data() { return mMazeData; }
  
  // start the transformation to the next maze
  public void changeToNext() {
  
    assert( mDeltas.isEmpty() );
    assert( mChangeTimer == 0.0f );

    mMazeNum += 1;

    MazeData nextMaze = Mazes.get(mMazeNum);
    mMazeData.collectDifferences(nextMaze, mDeltas);
    mNumDeltas = mDeltas.size();
    mChangeTimer = kChangeInitialDelay + kChangeMorphDelay;
    
  } // changeToNext()

  // whether the maze is currently changing
  public boolean changing() { return !mDeltas.isEmpty(); }

  // modify the maze for a new level
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    if ( mChangeTimer > 0.0f ) {

      mChangeTimer = Math.max( mChangeTimer-Env.TICK_TIME, 0.0f );
      
      if ( mChangeTimer < kChangeMorphDelay ) {
        int numLeft = Math.round( mNumDeltas*mChangeTimer/kChangeMorphDelay );
        while ( mDeltas.size() > numLeft ) {
          mMazeData.applyDifference(mDeltas.pop());
        }
        if ( mChangeTimer + Env.TICK_TIME >= kChangeMorphDelay ) {
          Env.sounds().play(Sounds.MAZE_MORPH);
        }
        newStoryEvents.add( new EventMazeChanged() );
      }

    }
    
  } // Sprite.advance()

  // display the maze
  @Override
  public void draw(SpriteBatch batch) {

    Env.painter().mazePainter().draw(batch, mMazeData);
    
  } // Sprite.draw()
  
} // class Maze

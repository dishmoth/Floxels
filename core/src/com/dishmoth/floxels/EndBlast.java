/*
 *  EndBlast.java
 *  Copyright Simon Hern 2016
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// stun the floxels and end the game
public class EndBlast extends Sprite implements SourceTerm {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70;

  // how long the blast remains for
  static private final float kLifeTimeSeconds = 0.8f;
  
  // time during which blast's strength fades
  static private final float kFadeTimeSeconds = 0.4f;
  
  // how strongly the blast repels floxels (sum of point strengths)
  static private final float kRepulsionStrength = 10000.0f;
  
  // the repulsion is spread across an area
  static private final float kRepulsionDistance = 0.15f;

  // how fast the blast spreads (tile units per second)
  static private final float kStunSpeed = 20.0f;

  // position (base grid units)
  private final float mXPos,
                      mYPos;

  // reference to the floxels
  private Floxels mFloxels;
  
  // how far out the blast has reached
  private float mStunRadius;
  
  // how much longer the blast stays for
  private float mLifeSeconds;

  // constructor
  public EndBlast(float x, float y, Floxels floxels) {
    
    super(kScreenLayer);
  
    assert( x > 0 && x < Env.numTilesX() );
    assert( y > 0 && y < Env.numTilesY() );
    
    mXPos = x;
    mYPos = y;
    
    mFloxels = floxels;
    
    mStunRadius = 0.0f;
    mLifeSeconds = kLifeTimeSeconds;

  } // constructor
  
  // expand the blast
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final float dt = Env.TICK_TIME;
    mLifeSeconds -= dt;
    if ( mLifeSeconds <= 1.0e-3f ) {
      killTheseSprites.add(this);
      newStoryEvents.add(new FloxelsStory.EventExitGame());
    }

    final float dr = kStunSpeed*dt;
    mStunRadius += dr;
    
    mFloxels.stunFloxels(mXPos, mYPos, 
                         Math.max(0.0f, mStunRadius-3.0f*dr),
                         mStunRadius, 
                         -1);    
    //Env.sounds().playDeathSounds(new int[]{0,num});
    
  } // Sprite.advance()

  // add repulsion to the source terms
  public void addToSource(int floxelType, float source[][], int refineFactor) {

    final float fade = Math.min(1.0f, mLifeSeconds/kFadeTimeSeconds);
    final float strength = fade*kRepulsionStrength/16;

    final float d1 = kRepulsionDistance,
                d2 = kRepulsionDistance/(float)Math.sqrt(2); 
    
    addToSource(source, refineFactor, mXPos,    mYPos,    8*strength);
    
    addToSource(source, refineFactor, mXPos+d1, mYPos,    strength);
    addToSource(source, refineFactor, mXPos,    mYPos+d1, strength);
    addToSource(source, refineFactor, mXPos-d1, mYPos,    strength);
    addToSource(source, refineFactor, mXPos,    mYPos-d1, strength);
    
    addToSource(source, refineFactor, mXPos+d2, mYPos+d2, strength);
    addToSource(source, refineFactor, mXPos-d2, mYPos+d2, strength);
    addToSource(source, refineFactor, mXPos+d2, mYPos-d2, strength);
    addToSource(source, refineFactor, mXPos-d2, mYPos-d2, strength);
    
  } // SourceTerm.addToSource()

  // add a single point to the source
  private void addToSource(float source[][], int refineFactor, 
                           float x, float y, float strength) {
    
    int ix = (int)Math.floor( x*refineFactor ),
        iy = (int)Math.floor( y*refineFactor );
    
    iy = Math.max(0, Math.min(source.length-1, iy));
    ix = Math.max(0, Math.min(source[iy].length-1, ix));
    
    source[iy][ix] += strength;
    
  } // addToSource()
  
  // display the blast radius
  @Override
  public void draw(SpriteBatch batch) {

    float alpha = (mLifeSeconds-(kLifeTimeSeconds-kFadeTimeSeconds))
                  / kFadeTimeSeconds;
    if ( alpha <= 0.0f ) return;
    
    Env.painter().hoopPainter().drawHoop(batch, mXPos, mYPos, 
                                         mStunRadius, alpha);
    
  } // Sprite.draw()

} // class EndBlast

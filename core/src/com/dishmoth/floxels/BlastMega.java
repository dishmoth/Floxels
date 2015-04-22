/*
 *  BlastMega.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// a big blast that stuns enemy floxels
public class BlastMega extends Sprite implements SourceTerm {

  // story event: a blast is triggered
  public static class EventUnleashed extends StoryEvent {}
  
  // structure for holding details of a spark from an explosion
  private class Spark {
    public float mXPos, mYPos, mXVel, mYVel;
    public Spark(float x, float y, float vx, float vy) {
      mXPos=x; mYPos=y; mXVel=vx; mYVel=vy;
    }
  } // class BlastMega.Spark
  
  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70;

  // how long the beacon remains for
  static private final float kLifeTimeSeconds = 0.8f;
  
  // time during which blast's strength fades
  static private final float kFadeTimeSeconds = 0.3f;
  
  // how strongly the blast repels floxels (sum of point strengths)
  static private final float kRepulsionStrength = 10000.0f;
  
  // the repulsion is spread across an area
  static private final float kRepulsionDistance = 0.15f;

  // how fast the stun blast spreads (tile units per second)
  static private final float kStunSpeed = 20.0f;

  // details of explosion fragments
  static private final int   kNumSparks     = 80;
  static private final float kSparkMinSpeed = 0.8f;
  
  // position (base grid units)
  private final float mXPos,
                      mYPos;

  // which floxels are affected by the blast
  private Floxels mFloxels;
  private int mFloxelType;
  
  // how far out the blast has reached
  private float mStunRadius;
  
  // how much longer the blast stays for
  private float mLifeSeconds;

  // which set of images to show
  private int mVariation;
  
  // fragments of an explosion
  private Spark mSparks[];
  
  // constructor
  public BlastMega(float x, float y, Floxels floxels, int type) {
    
    super(kScreenLayer);
  
//    Blast.initialize();
//    Bouncer.initialize();
    
    assert( x > 0 && x < Env.numTilesX() );
    assert( y > 0 && y < Env.numTilesY() );
    
    mXPos = x;
    mYPos = y;
    
    mFloxels = floxels;
    mFloxelType = type;
    mStunRadius = 0.0f;
    
    mLifeSeconds = kLifeTimeSeconds;

    mVariation = Env.randomInt(Blast.kNumVariations);

    mSparks = new Spark[kNumSparks];
    for ( int k = 0 ; k < kNumSparks ; k++ ) {
      final float theta = (2.0f*(float)Math.PI*k)/kNumSparks;
      final float dx    = (float)Math.cos(theta),
                  dy    = (float)Math.sin(theta);
      final float h     = ( (k%4 < 3) ? 0.5f*(k%4) : 0.5f ),
                  v     = ((1-h)*kSparkMinSpeed + h)*kStunSpeed;
      mSparks[k] = new Spark(mXPos, mYPos, v*dx, v*dy);
    }
    
  } // constructor
  
  // count down the blast's lifetime
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    if ( mLifeSeconds == kLifeTimeSeconds ) {
      newStoryEvents.add(new EventUnleashed());
    }
    
    final float dt = Env.TICK_TIME;
    mLifeSeconds -= dt;
    if ( mLifeSeconds <= 1.0e-3f ) {
      killTheseSprites.add(this);
    }

    final float dr = kStunSpeed*dt;
    mStunRadius += dr;
    mFloxels.stunFloxels(mXPos, mYPos, 
                         Math.max(0.0f, mStunRadius-1.2f*dr), mStunRadius, 
                         mFloxelType);

    for ( int k = 0 ; k < kNumSparks ; k++ ) {
      Spark spark = mSparks[k];
      if ( spark == null ) continue;
      spark.mXPos += dt*spark.mXVel;
      spark.mYPos += dt*spark.mYVel;
      if ( spark.mXPos < 0.0f || spark.mXPos > Env.numTilesX() ||
           spark.mYPos < 0.0f || spark.mYPos > Env.numTilesY() ) {
        mSparks[k] = null;
      }
    }
    
  } // Sprite.advance()

  // add repulsion to the source terms
  public void addToSource(int floxelType, float source[][], int refineFactor) {

    if ( floxelType != 0 ) return;
    
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
  
  // display the blast image 
  // (reuses drawing code from Blast and Bouncer classes)
  @Override
  public void draw(SpriteBatch batch) {

    float life = mLifeSeconds - (kLifeTimeSeconds - Blast.kLifeTimeSeconds);
    if ( life <= 0.0f ) return;
    //Blast.draw(g2, mXPos, mYPos, life, mVariation);

    for ( Spark spark : mSparks ) {
      if ( spark == null ) continue;
      final int x = (int)Math.floor(spark.mXPos * Env.tileWidth()),
                y = (int)Math.floor(spark.mYPos * Env.tileWidth());
      //Bouncer.drawSpark(g2, x, y);
    }
    
  } // Sprite.draw()

} // class BlastMega

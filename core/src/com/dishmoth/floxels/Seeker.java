/*
 *  Seeker.java
 *  Copyright Simon Hern 2011
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.Stroke;
//import java.awt.geom.Path2D;
//import java.awt.geom.RoundRectangle2D;
//import java.awt.image.BufferedImage;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//
public class Seeker extends Sprite {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70; 

  // speed range (tile units per second)
  static private final float kMaxSpeed = 1.5f,
                             kMinSpeed = 0.3f;
  
  // keep the bouncer at its maximum speed whenever possible
  static private final float kBackgroundAccel = 1.0f;

  // force factors when bouncing off walls
  static private final float kBounceDistance = 0.25f,
                             kBounceForce    = 20.0f;
  
  // size for checking mouse clicks
  static private final float kHitRadius = 0.4f;

  // how strongly the object repels floxels
  static private final float kRepulsionStrength = 200.0f;

  // the repulsion is spread across an area
  static private final float kRepulsionDistance = 0.3f;
  
  // how fast the hoop grows or shrinks (scale per second)
  static private final float kGrowthRate = 2.0f; 
  
  // appearance of the circular hoop
  private static final float   kHoopRadius    = 6.3f;
  private static final int     kHoopWidth     = 29;
//  private static final Stroke  kLineStrokes[] = { new BasicStroke(5.5f),
//                                                  new BasicStroke(3.5f),
//                                                  new BasicStroke(1.5f) };
//  private static final Color   kLineColours[] = { new Color(0,0,0, 100),
//                                                  new Color(0,0,0),
//                                                  new Color(200,200,200) };
//  static private final Color   kBlankColour   = new Color(0,0,0,0);
//  private static BufferedImage kHoopImage     = null;

  // reference to the maze object
  private MazeData mMaze;
  
  // reference to the floxels
  private Floxels mFloxels;

  // reference to the target flow
  private Flow mFlow;
  
  // position (tile units)
  private float mXPos,
                mYPos;
  
  // velocity (tile units per second)
  private float mXVel,
                mYVel;

  // true if the bouncer has been triggered
  private boolean mDetonate;

  // true if the bouncer is fading away
  private boolean mDisappear;

  // scale factor when the bouncer is growing or shrinking
  private float mSize;
  
//  public static void initialize() {
//
//    if ( kHoopImage != null ) return;
//
//    kHoopImage = Env.createTranslucentImage(kHoopWidth, kHoopWidth);
//    Graphics2D g2 = kHoopImage.createGraphics();
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//    
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, kHoopWidth, kHoopWidth);
//
//    drawHoop(g2, kHoopWidth/2, kHoopWidth/2, 1.0f);
//    
//    g2.dispose();
//    
//  } // initialize()
  
  // render a circle
//  private static void drawHoop(Graphics2D g2, int x, int y, float scale) {
//    
//    final float r = kHoopRadius*scale;
//    RoundRectangle2D hoop = new RoundRectangle2D.Float(x+0.5f-r, y+0.5f-r, 
//                                                       2*r, 2*r, 2*r, 2*r);
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//    for ( int k = 0 ; k < kLineColours.length ; k++ ) {
//      g2.setColor(kLineColours[k]);
//      g2.setStroke(kLineStrokes[k]);
//      g2.draw(hoop);
//    }
//    
//  } // drawHoop()
  
  // constructor
  public Seeker(MazeData maze, Floxels floxels, Flow flow) {

    super(kScreenLayer);
    
//    initialize();
    
    assert( maze != null );
    mMaze = maze;
    mFloxels = floxels;
    mFlow = flow;
    
    mXPos = Env.randomInt( Env.numTilesX() ) + 0.5f;
    mYPos = Env.randomInt( Env.numTilesY() ) + 0.5f;
    
    final float theta = 2.0f*(float)Math.PI*Env.randomFloat();
    mXVel = kMaxSpeed*(float)Math.cos(theta);
    mYVel = kMaxSpeed*(float)Math.sin(theta);
    
    mDetonate = false;
  
    mDisappear = false;
    mSize = 0.0f;

  } // constructor

  // move the ball
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    final int   ix = (int)Math.floor(mXPos),
                iy = (int)Math.floor(mYPos);
    final float dx = mXPos - ix,
                dy = mYPos - iy;
    
    float xForce = 0.0f,
          yForce = 0.0f;
    final boolean wallLeft  = mMaze.vertWall(ix, iy),
                  wallRight = mMaze.vertWall(ix+1, iy),
                  wallUp    = mMaze.horizWall(ix, iy),
                  wallDown  = mMaze.horizWall(ix, iy+1);
    if ( wallLeft )  xForce += force(dx);
    if ( wallRight ) xForce -= force(1-dx);
    if ( wallUp )    yForce += force(dy);
    if ( wallDown )  yForce -= force(1-dy);
    
    if ( dx < 0.5f && dy < 0.5f && !wallLeft && !wallUp ) {
      if ( mMaze.vertWall(ix, iy-1) || mMaze.horizWall(ix-1, iy) ) {
        final float d2 = dx*dx + dy*dy,
                    d  = Math.max(0.001f, (float)Math.sqrt(d2)),
                    f  = force(d);
        xForce += f*dx/d;
        yForce += f*dy/d;
      }
    }
    
    if ( dx > 0.5f && dy < 0.5f && !wallRight && !wallUp ) {
      if ( mMaze.vertWall(ix+1, iy-1) || mMaze.horizWall(ix+1, iy) ) {
        final float d2 = (1-dx)*(1-dx) + dy*dy,
                    d  = Math.max(0.001f, (float)Math.sqrt(d2)),
                    f  = force(d);
        xForce -= f*(1-dx)/d;
        yForce += f*dy/d;
      }
    }
    
    if ( dx < 0.5f && dy > 0.5f && !wallLeft && !wallDown ) {
      if ( mMaze.vertWall(ix, iy+1) || mMaze.horizWall(ix-1, iy+1) ) {
        final float d2 = dx*dx + (1-dy)*(1-dy),
                    d  = Math.max(0.001f, (float)Math.sqrt(d2)),
                    f  = force(d);
        xForce += f*dx/d;
        yForce -= f*(1-dy)/d;
      }
    }
    
    if ( dx > 0.5f && dy > 0.5f && !wallRight && !wallDown ) {
      if ( mMaze.vertWall(ix+1, iy+1) || mMaze.horizWall(ix+1, iy+1) ) {
        final float d2 = (1-dx)*(1-dx) + (1-dy)*(1-dy),
                    d  = Math.max(0.001f, (float)Math.sqrt(d2)),
                    f  = force(d);
        xForce -= f*(1-dx)/d;
        yForce -= f*(1-dy)/d;
      }
    }

    Flow.Vel vel = new Flow.Vel();
    mFlow.getVelocity(mXPos, mYPos, vel);
    final float alpha = 1.5f;
    xForce += alpha*vel.x;
    yForce += alpha*vel.y;
    
    final float dt = Env.TICK_TIME;

    mXVel += dt*xForce*kBounceForce;
    mYVel += dt*yForce*kBounceForce;
    
    if ( xForce == 0.0 && yForce == 0.0 ) {
      if ( Math.abs(mXVel) < kMinSpeed ) {
        mXVel += dt*kBackgroundAccel*((mXVel > 0) ? +1 : -1);
      } else if ( Math.abs(mYVel) < kMinSpeed ) {
        mYVel += dt*kBackgroundAccel*((mYVel > 0) ? +1 : -1);
      } else {
        final float v2   = mXVel*mXVel + mYVel*mYVel;
        final float vOld = Math.max(0.001f, (float)Math.sqrt(v2)),
                    vNew = vOld + dt*kBackgroundAccel;
        mXVel *= vNew/vOld;
        mYVel *= vNew/vOld;
      }
    }
      
    final float v2 = mXVel*mXVel + mYVel*mYVel;
    if ( v2 > kMaxSpeed*kMaxSpeed ) {
      final float v = (float)Math.sqrt(v2);
      mXVel *= kMaxSpeed/v;
      mYVel *= kMaxSpeed/v;
    }
    
    mXPos += dt*mXVel;
    mYPos += dt*mYVel;
  
    if ( mDisappear ) {
      mSize = Math.max(0.0f, mSize - dt*kGrowthRate);
      if ( mSize == 0.0f ) killTheseSprites.add(this);
    } else {
      mSize = Math.min(1.0f, mSize + dt*kGrowthRate);
    }
    
  } // Sprite.advance()

  // repulsive force from an obstacle 
  private float force(float distance) {
    
    assert( distance >= 0.0f );
    
    final float d = distance/kBounceDistance;
    if ( d >= 1.0f ) return 0.0f;
    
    return 1-d*d;
    
  } // force()

  // display the object
  @Override
  public void draw(SpriteBatch batch) {

    final int x0 = (int)Math.floor(mXPos*Env.tileWidth()) + Env.gameOffsetX(),
              y0 = (int)Math.floor(mYPos*Env.tileWidth()) + Env.gameOffsetY();

    if ( mSize == 1.0f ) {
      //g2.drawImage(kHoopImage, x0-kHoopWidth/2, y0-kHoopWidth/2, null);
    } else {
      //drawHoop(batch, x0, y0, mSize);
    }

  } // Sprite.draw()

} // class Seeker

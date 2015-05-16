/*
 *  Summoner.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.Stroke;
//import java.awt.geom.Path2D;
//import java.awt.image.BufferedImage;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// summon one floxel population to a location
public class Summoner extends Sprite implements SourceTerm {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70; 

  // how strong the summons is
  static private final float kSummonsStrength = 3.0f;
  
  // time (seconds) that the summoner remains for
  static private final float kLifeTime = 2.0f,
                             kFadeTime = 0.7f;

  // tweak the source terms to focus the summons
  static private final int   kFocusRange    = 7;
  static private final float kFocusStrength = 2.5f;
  
  // image details
  static private final int     kImageWidth    = 3,
                               kImageMargin   = 5;
//  static private final Stroke  kLineStrokes[] = { new BasicStroke(5.0f),
//                                                  new BasicStroke(3.0f),
//                                                  new BasicStroke(1.5f) };
//  static private final Color   kLineColours[] = { new Color(0,0,0, 100),
//                                                  new Color(0,0,0),
//                                                  new Color(200,200,200) };
//  static private final Color   kBlankColour   = new Color(0,0,0,0);
//  static private BufferedImage kImages[]      = null;

  // animation details
  static private final int   kCrossMin  = -1,
                             kCrossMax  = 3;
  static private final float kCrossRate = 15.0f,
                             kCrossPeak = 2.5f;
  
  // target position (tile units)
  private float mXPos,
                mYPos;
  
  // time (seconds) until the summoner disappears
  private float mLifeTime;

  // time (seconds) since creation
  private float mTime;
  
  // which floxel population is affected
  private int mFloxelType;

  // prepare images, etc.
//  static public void initialize() {
//
//    if ( kImages != null ) return;
//    
//    kImages = new BufferedImage[4];
//
//    final int h = kImageWidth + 2*kImageMargin,
//              w = (2*kImageWidth-1) + 2*kImageMargin; 
//    
//    kImages[Env.kDirectionUp] = Env.createTranslucentImage(w, h);
//    Graphics2D g2 = kImages[Env.kDirectionUp].createGraphics();
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, w, h);
//    Path2D shape = new Path2D.Float();
//    shape.moveTo(0.5f*w-kImageWidth, kImageMargin);
//    shape.lineTo(0.5f*w, h-kImageMargin);
//    shape.lineTo(0.5f*w+kImageWidth, kImageMargin);
//    for ( int k = 0 ; k < kLineColours.length ; k++ ) {
//      g2.setColor(kLineColours[k]);
//      g2.setStroke(kLineStrokes[k]);
//      g2.draw(shape);
//    }
//    g2.dispose();
//    
//    kImages[Env.kDirectionDown] = Env.createTranslucentImage(w, h);
//    g2 = kImages[Env.kDirectionDown].createGraphics();
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, w, h);
//    g2.rotate(Math.PI, 0.5f*w, 0.5f*h);
//    g2.drawImage(kImages[Env.kDirectionUp], 0, 0, null);
//    g2.dispose();
//    
//    kImages[Env.kDirectionRight] = Env.createTranslucentImage(h, w);
//    g2 = kImages[Env.kDirectionRight].createGraphics();
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, h, w);
//    g2.rotate(0.5f*Math.PI);
//    g2.translate(0, -h);
//    g2.drawImage(kImages[Env.kDirectionUp], 0, 0, null);
//    g2.dispose();
//    
//    kImages[Env.kDirectionLeft] = Env.createTranslucentImage(h, w);
//    g2 = kImages[Env.kDirectionLeft].createGraphics();
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, h, w);
//    g2.rotate(Math.PI, 0.5f*h, 0.5f*w);
//    g2.drawImage(kImages[Env.kDirectionRight], 0, 0, null);
//    g2.dispose();
//    
//  } // initialize()
  
  // constructor (position specified)
  public Summoner(float xPos, float yPos, int floxelType) {
    
    super(kScreenLayer);
  
//    initialize();
    
    mXPos = xPos; 
    mYPos = yPos;
    mLifeTime = kLifeTime;
    mTime = 0.0f;
    
    mFloxelType = floxelType;
    
  } // constructor

  // accessors
  public int floxelType() { return mFloxelType; }
  public float xPos() { return mXPos; }
  public float yPos() { return mYPos; }
  
  //
  public void cancel(boolean immediate) { 
    
    if ( immediate ) mLifeTime = 0.0f;
    else             mLifeTime = Math.min(mLifeTime, kFadeTime);
    
  } // cancel()
  
  //
  public boolean isDead() { return (mLifeTime <= 0.0f); } 
  
  // current strength of summons
  public float strength() {

    assert( mXPos >= 0 && mYPos >= 0 );
    return kSummonsStrength*Math.min(1.0f, mLifeTime/kFadeTime);
    
  } // strength()
  
  // change the floxel population
  public void switchFloxelType() { mFloxelType = 1 - mFloxelType; }
  
  // advance timers, etc.
  @Override
  public void advance(LinkedList<Sprite> addTheseSprites,
                      LinkedList<Sprite> killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {

    assert( mXPos >= 0 && mYPos >= 0 );

    final float dt = Env.TICK_TIME;
    
    mLifeTime -= dt;
    if ( mLifeTime < 1.0e-4f ) {
      mLifeTime = 0.0f;
      killTheseSprites.add(this);
    }
    
    mTime += dt;
    
  } // Sprite.advance()

  // animation of the cursor
  private int crossSize() {
    
    float t = mTime*kCrossRate - kCrossPeak;
    
    float amp;
    if ( t >= 0.0f ) {
      amp = -(float)Math.sin(t);
    } else {
      amp = -t;
    }
    
    amp = 0.5f*(amp+1);
    return Math.round( amp*kCrossMax + (1-amp)*kCrossMin );
  
  } // crossSize()

  // attract floxels
  @Override
  public void addToSource(int floxelType, float[][] source, int refineFactor) {
    
    if ( floxelType != mFloxelType ) return;
    
    final float strength = kFocusStrength*Math.min(1.0f, mLifeTime/kFadeTime);
    final int dist2 = (kFocusRange+1)*(kFocusRange+1);
    
    int ix = (int)Math.floor(mXPos*refineFactor),
        iy = (int)Math.floor(mYPos*refineFactor);
    ix = Math.max(kFocusRange, Math.min(source[0].length-1-kFocusRange, ix));
    iy = Math.max(kFocusRange, Math.min(source.length-1-kFocusRange, iy));
    
    for ( int dy = -kFocusRange ; dy <= +kFocusRange ; dy++ ) {
      for ( int dx = -kFocusRange ; dx <= +kFocusRange ; dx++ ) {
        final float r2 = dx*dx + dy*dy;
        if ( r2 < dist2 ) {
          source[iy+dy][ix+dx] -= strength*(1.0f - r2/dist2);
        }
      }
    }

  } // SourceTerm.addToSource()

  // display the target position
  @Override
  public void draw(SpriteBatch batch) {

    assert( mXPos >= 0 && mYPos >= 0 );
    assert( !isDead() );
    
    final int ix = Env.gameOffsetX() + Math.round(mXPos*Env.tileWidth()),
              iy = Env.gameOffsetY() + Math.round(mYPos*Env.tileWidth());

    int delta = crossSize();

    /*
    g2.drawImage(kImages[Env.kDirectionUp], 
                 ix - kImages[Env.kDirectionUp].getWidth()/2, 
                 iy - kImages[Env.kDirectionUp].getHeight() - delta,
                 null);
    g2.drawImage(kImages[Env.kDirectionDown], 
                 ix - kImages[Env.kDirectionDown].getWidth()/2, 
                 iy + 1 + delta,
                 null);
    g2.drawImage(kImages[Env.kDirectionLeft], 
                 ix - kImages[Env.kDirectionLeft].getWidth() - delta, 
                 iy - kImages[Env.kDirectionLeft].getHeight()/2,
                 null);
    g2.drawImage(kImages[Env.kDirectionRight], 
                 ix + 1 + delta,
                 iy - kImages[Env.kDirectionRight].getHeight()/2,
                 null);
    */
    
  } // Sprite.draw()

} // class Summoner

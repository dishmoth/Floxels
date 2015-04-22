/*
 *  Blast.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.Shape;
//import java.awt.Stroke;
//import java.awt.geom.Area;
//import java.awt.geom.Path2D;
//import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// a blast that temporarily repels floxels
public class Blast extends Sprite implements SourceTerm {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 70;

  // details of the blast image
  static private final int    kStarPoints     = 9;
  static private final float  kStarMinRadius  = 14.0f,
                              kStarMaxRadius  = 24.0f,
                              kStarRandShrink = 0.9f,
                              kStarMinSize    = 2.0f,
                              kStarInnerScale = 0.3f;
//  static private final Color  kBlankColour    = new Color(0,0,0,0);
//  private static final Color  kSolidColour    = Color.WHITE;
//  private static final Stroke kLineStrokes[]  = { new BasicStroke(3.2f),
//                                                  new BasicStroke(1.3f) };
//  private static final Color  kLineColours[]  = { new Color(0,0,0, 100),
//                                                  new Color(0,0,0, 255) };
  
  // sequence of images (smallest to largest, some may be null)
  static public final int      kNumVariations = 2;
  static private final int     kNumImages     = 10;
//  static private BufferedImage kImages[][]    = null;

  // how long the beacon remains for
  static public final float kLifeTimeSeconds = 0.7f;
  
  // time during which blast's strength fades
  static private final float kFadeTimeSeconds = 0.5f;
  
  // how strongly the blast repels floxels (sum of point strengths)
  static private final float kRepulsionStrength = 1000.0f;
  
  // the repulsion is spread across an area
  static private final float kRepulsionDistance = 0.15f;
  
  // position (base grid units)
  private final float mXPos,
                      mYPos;

  // how much longer the blast stays for
  private float mLifeSeconds;

  // which set of images to show
  private int mVariation;
  
  // prepare image data
//  public static void initialize() {
//
//    if ( kImages != null ) return;
//
//    kImages = new BufferedImage[kNumVariations][kNumImages];
//    
//    Random rand = new Random(0);
//    
//    for ( int n = 0 ; n < kNumVariations ; n++ ) {
//      for ( int k = 0 ; k < kNumImages ; k++ ) {
//        final float h = k/(kNumImages-1.0f);
//        final float scale = h*h;
//  
//        if ( scale*kStarMaxRadius < kStarMinSize ) continue;
//        
//        BufferedImage image = makeStar(scale, ((k+n)%2 == 0), rand);
//        kImages[n][k] = Env.createTranslucentImage(image.getWidth(), 
//                                                   image.getHeight());
//        
//        Graphics2D g2 = kImages[n][k].createGraphics();
//        g2.setBackground(kBlankColour);
//        g2.clearRect(0, 0, image.getWidth(), image.getHeight());
//        g2.drawImage(image, 0, 0, null);
//        g2.dispose();
//      }
//    }
//    
//  } // initialize()

//  // return a star image with the required size and orientation
//  private static BufferedImage makeStar(float scale, boolean rotate,
//                                        Random rand) {
//
//    final int imageRadius = (int)Math.ceil(scale*kStarMaxRadius) + 1,
//              imageSize   = 2*imageRadius + 1;
//    
//    BufferedImage image = Env.createTranslucentImage(imageSize, imageSize);
//    Graphics2D g2 = image.createGraphics();
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//    
//    g2.setBackground(kBlankColour);
//    g2.clearRect(0, 0, imageSize, imageSize);
// 
//    float radii[] = new float[2*kStarPoints];
//    for ( int k = 0 ; k < radii.length ; k++ ) {
//      final boolean big = ( (rotate ? (k+1) : k) % 2 == 0);
//      final float shrink = kStarRandShrink
//                         + rand.nextFloat()*(1-kStarRandShrink); 
//      radii[k] = scale * shrink * (big ? kStarMaxRadius : kStarMinRadius);
//    }
//    
//    g2.translate(0.5f*imageSize, 0.5f*imageSize);
//
//    Shape starOuter = starShape(radii, 1.0f);
//    Area starSolid = new Area(starOuter);
//
//    final float innerScale = kStarInnerScale*(2.0f*scale - 1.0f); 
//    Shape starInner = null;
//    if ( innerScale > 0.0f ) {
//      starInner = starShape(radii, innerScale);
//      starSolid.subtract(new Area(starInner));
//    }
//    
//    g2.setColor(kSolidColour);
//    g2.fill(starSolid);
//    
//    for ( int k = 0 ; k < kLineColours.length ; k++ ) {
//      g2.setColor(kLineColours[k]);
//      g2.setStroke(kLineStrokes[k]);
//      g2.draw(starOuter);
//      if ( starInner != null ) g2.draw(starInner);
//    }
//    
//    g2.dispose();
//    
//    return image;
//    
//  } // makeStar()
  
//  // return a shape object for the star
//  private static Path2D starShape(float radii[], float scale) {
//
//    final float dTheta = 2.0f*(float)Math.PI/radii.length;
//
//    Path2D star = new Path2D.Float();
//    
//    for ( int k = 0 ; k < radii.length ; k++ ) {
//      final float theta = (k+0.5f)*dTheta,
//                  r     = Math.max(0.0f, radii[k]*scale);
//      final float x     = r*(float)Math.cos(theta),
//                  y     = r*(float)Math.sin(theta);
//
//      if ( k == 0 ) star.moveTo(x, y);
//      else          star.lineTo(x, y);
//    }
//    star.closePath();
//    
//    return star;
//    
//  } // starShape()
  
  // constructor
  public Blast(float x, float y) {
    
    super(kScreenLayer);
  
    assert( x > 0 && x < Env.numTilesX() );
    assert( y > 0 && y < Env.numTilesY() );
    
    mXPos = x;
    mYPos = y;
    
    mLifeSeconds = kLifeTimeSeconds;

    mVariation = Env.randomInt(kNumVariations);
    
//    initialize();
    
  } // constructor
  
  // count down the blast's lifetime
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    mLifeSeconds -= Env.TICK_TIME;
    if ( mLifeSeconds <= 1.0e-3f ) {
      killTheseSprites.add(this);
    }
    
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
  
  // display the blast image
  @Override
  public void draw(SpriteBatch batch) {

    //draw(g2, mXPos, mYPos, mLifeSeconds, mVariation);

  } // Sprite.draw()

  // display a blast image
//  static public void draw(Graphics2D g2, float xPos, float yPos,
//                          float life, int variation) {
//
//    final float h = life/kLifeTimeSeconds;
//    final int index = (int)Math.ceil(kNumImages*h) - 1;
//
//    BufferedImage image = kImages[variation % kNumVariations][index];
//    if ( image == null ) return;
//    
//    final int x = (int)Math.floor(xPos*Env.tileWidth()) + Env.gameOffsetX(),
//              y = (int)Math.floor(yPos*Env.tileWidth()) + Env.gameOffsetY();
//    
//    g2.drawImage(image, 
//                 x - image.getWidth()/2, 
//                 y - image.getHeight()/2, 
//                 null);
//    
//  } // draw()
  
} // class Blast

/*
 *  BlastMini.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;

// a blast that temporarily repels floxels
public class BlastMini extends Sprite implements SourceTerm {

  // how sprite is displayed relative to others
  static private final int kScreenLayer = 40;

  // details of the beacon image
  static private final int   kStarPoints     = 7;
  static private final float kStarMinRadius  = 6.0f,
                             kStarMaxRadius  = 10.0f,
                             kStarRandShrink = 0.8f,
                             kStarMinSize    = 1.0f;
  static private final Color kBlankColour    = new Color(0,0,0,0);
  static private final Color kStarColours[]  = { new Color(0,0,0,127),
                                                 new Color(0,0,0),
                                                 new Color(127,127,127),
                                                 new Color(250,250,250) };
  static private final float kStarInsets[]   = { 0.0f, 1.0f, 2.0f, 2.5f };
  
  // sequence of images (smallest to largest, some may be null)
  static private final int     kNumVariations = 2,
                               kNumImages     = 15;
  static private BufferedImage kImages[][]    = null;

  // number of repetitions of each star variation
  static private final int kVariationRepeats = 3;
  
  // how long the beacon remains for
  static private final float kLifeTimeSeconds = 1.0f;
  
  // time during which blast's strength fades
  static private final float kFadeTimeSeconds = 0.3f;
  
  // how strongly the blast repels floxels
  static private final float kRepulsionStrength = 55.0f;
  
  // position (base grid units)
  private final float mXPos,
                      mYPos;

  // how much longer the blast stays for
  private float mLifeSeconds;

  // which set of images to show
  private static int kVariationCount = 0;
  private int        mVariation;
  
  // prepare image data
  public static void initialize() {

    if ( kImages != null ) return;

    kImages = new BufferedImage[kNumVariations][kNumImages];
    
    Random rand = new Random(0);
    
    for ( int n = 0 ; n < kNumVariations ; n++ ) {
      for ( int k = 0 ; k < kNumImages ; k++ ) {
        final float h = k/(kNumImages-1.0f);
        final float scale = h*h;
  
        if ( scale*kStarMaxRadius < kStarMinSize ) continue;
        
        BufferedImage image = makeStar(scale, ((k+n)%2 == 0), rand);
        kImages[n][k] = Env.createTranslucentImage(image.getWidth(), 
                                                   image.getHeight());
        
        Graphics2D g2 = kImages[n][k].createGraphics();
        g2.setBackground(kBlankColour);
        g2.clearRect(0, 0, image.getWidth(), image.getHeight());
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
      }
    }
    
  } // initialize()

  // return a star image with the required size and orientation
  private static BufferedImage makeStar(float scale, boolean rotate,
                                        Random rand) {

    final int imageRadius = (int)Math.ceil(scale*kStarMaxRadius) + 1,
              imageSize   = 2*imageRadius + 1;
    
    BufferedImage image = Env.createTranslucentImage(imageSize, imageSize);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
    
    g2.setBackground(kBlankColour);
    g2.clearRect(0, 0, imageSize, imageSize);
 
    float radii[] = new float[2*kStarPoints];
    for ( int k = 0 ; k < radii.length ; k++ ) {
      final boolean big = ( (rotate ? (k+1) : k) % 2 == 0);
      final float shrink = kStarRandShrink
                         + rand.nextFloat()*(1-kStarRandShrink); 
      radii[k] = scale * shrink * (big ? kStarMaxRadius : kStarMinRadius);
    }
    
    g2.translate(0.5f*imageSize, 0.5f*imageSize);
    
    for ( int k = 0 ; k < kStarColours.length ; k++ ) {
      g2.setColor( kStarColours[k] );
      g2.fill(starShape(radii, kStarInsets[k]));
    }
    
    g2.dispose();
    
    return image;
    
  } // makeStar()
  
  // return a shape object for the star
  private static Path2D starShape(float radii[], float inset) {

    final float dTheta = 2.0f*(float)Math.PI/radii.length;

    Path2D star = new Path2D.Float();
    
    for ( int k = 0 ; k < radii.length ; k++ ) {
      final float theta = (k+0.5f)*dTheta,
                  r     = Math.max(0.0f, radii[k] - inset);
      final float x     = r*(float)Math.cos(theta),
                  y     = r*(float)Math.sin(theta);

      if ( k == 0 ) star.moveTo(x, y);
      else          star.lineTo(x, y);
    }
    star.closePath();
    
    return star;
    
  } // starShape()
  
  // constructor
  public BlastMini(float x, float y) {
    
    super(kScreenLayer);
  
    assert( x > 0 && x < Env.numTilesX() );
    assert( y > 0 && y < Env.numTilesY() );
    
    mXPos = x;
    mYPos = y;
    
    mLifeSeconds = kLifeTimeSeconds;

    mVariation = kVariationCount/kVariationRepeats;
    kVariationCount = (kVariationCount+1) % (kNumVariations*kVariationRepeats);
    
    initialize();
    
  } // constructor
  
  // count down the blast's lifetime
  @Override
  public void advance(LinkedList<Sprite>     addTheseSprites,
                      LinkedList<Sprite>     killTheseSprites,
                      LinkedList<StoryEvent> newStoryEvents) {
    
    mLifeSeconds -= 1.0f/Env.ticksPerSecond();
    if ( mLifeSeconds <= 1.0e-3f ) {
      killTheseSprites.add(this);
    }
    
  } // Sprite.advance()

  // add repulsion to the source terms
  @Override
  public void addToSource(int floxelType, float source[][], int refineFactor) {

    final int x = (int)Math.floor(mXPos*refineFactor),
              y = (int)Math.floor(mYPos*refineFactor);
    final float fade = Math.min(1.0f, mLifeSeconds/kFadeTimeSeconds);
    source[y][x] += kRepulsionStrength*fade;
    
  } // SourceTerm.addToSource()
  
  // display the blast image
  @Override
  public void draw(Graphics2D g2) {

    final double h = mLifeSeconds/kLifeTimeSeconds;
    final int index = (int)Math.ceil(kNumImages*h) - 1;
    BufferedImage image = kImages[mVariation][index];
    if ( image == null ) return;
    
    final int x = (int)Math.floor(mXPos*Env.tileWidth()) + Env.gameOffsetX(),
              y = (int)Math.floor(mYPos*Env.tileWidth()) + Env.gameOffsetY();
    
    g2.drawImage(image, 
                 x - image.getWidth()/2, 
                 y - image.getHeight()/2, 
                 null);

  } // Sprite.draw()

} // class BlastMini

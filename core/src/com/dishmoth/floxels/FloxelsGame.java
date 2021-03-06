/*
 *  FloxelsGame.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// main application class
public class FloxelsGame implements ApplicationListener {

  // camera controlling screen coordinates
  private OrthographicCamera mCamera;

  // object for drawing sprites
  private SpriteBatch mSpriteBatch;
  
  // the main game controller
  private GameManager mGameManager;

  // the game is interrupted (Android) or minimized (Desktop)
  private boolean mPaused;
  
  // seconds since the last update
  private double mTimeSince;

  // debug timing measurements
  private TimingStats mTimingStats;
  
  // Called when the application is first created.
	public void create() {

    Env.debug("Floxels (v1.0.0, 21st December 2016)");
    Env.debug("Contact: dishmoth@yahoo.co.uk, www.dishmoth.com");
    
	  Env.debug("create()");
	  
    Env.initialize();
    Env.sounds().initialize();

    Gdx.input.setCatchBackKey(true);

    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();

    mCamera = new OrthographicCamera(width, height);
    mCamera.translate(width/2.0f, height/2.0f);
    mCamera.update();
    
    mSpriteBatch = new SpriteBatch(1500);
    mSpriteBatch.setProjectionMatrix(mCamera.combined);

    chooseTileDimensions(width, height);
    
    mGameManager = new GameManager(new FloxelsStory());

    mPaused = false;
    
    mTimeSince = 0.0f;

    mTimingStats = new TimingStats();
    
	} // ApplicationListener.create()

	// decide the number of tiles and the tile size
	private void chooseTileDimensions(int width, int height) {
	  
    final float aspect = Math.max(width,height)/(float)Math.min(width,height);

    final float aspect0 = 10.0f/10.0f,
                aspect1 = 11.0f/9.0f,
                aspect2 = 12.0f/8.0f;
    final float weight  = 0.7f;
    
    int nx, ny;
    if ( aspect < (1-weight)*aspect0 + weight*aspect1 ) {
      nx = 10;
      ny = 10;
    } else if ( aspect < (1-weight)*aspect1 + weight*aspect2 ) {
      nx = 11;
      ny = 9;
    } else {
      nx = 12;
      ny = 8;
    }
    if ( width < height ) {
      int swap = nx;
      nx = ny;
      ny = swap;
    }
    
    Env.debug("Maze size: " + nx + "x" + ny);
    Env.setTilesXY(nx, ny);
    
    float extendedBorder = (Env.touchScreen() ? 0.05f : 0.0f);
    float wx = width/(Env.numTilesX()+extendedBorder),
          wy = height/(Env.numTilesY()+extendedBorder);
    int tile = (int)Math.floor(Math.min(wx, wy));
    Env.debug("Tile size: " + tile + " pixels");
    Env.setTileWidth(tile);
    
	} // chooseTileDimensions()
	
  // Called when the application is resized. This can happen at any point
  // during a non-paused state but will never happen before a call to create().
  public void resize(int width, int height) {

    Env.debug("resize(" + width + "," + height + ")");

  } // ApplicationListener.resize()

  // Called when the application is paused, usually when it's not active or 
  // visible on screen.  An application is also paused before it is destroyed.
  public void pause() {
    
    Env.debug("pause()");
    mPaused = true;
    Env.sounds().stopAll();
    
  } // ApplicationListener.pause()

  // Called when the application is resumed from a paused state, 
  // usually when it regains focus.
  public void resume() {
    
    Env.debug("resume()");
    mPaused = false;
    Env.painter().resetTextures();
    
  } // ApplicationListener.resume()

  // Called when the application is destroyed. Preceded by a call to pause().
  public void dispose () {
    
    Env.debug("dispose()");
    
  } // ApplicationListener.dispose()

  // Called when the application should render itself.
  public void render() {

    if ( mPaused ) return;
    
    final float deltaTime = Gdx.graphics.getRawDeltaTime();
    mTimingStats.update(deltaTime);
    
    final float tick = Env.TICK_TIME;
    final float maxStep = tick;
    
    mTimeSince += Math.min(deltaTime, maxStep);
    while ( mTimeSince > 0.99f*tick ) {
      update();
      mTimeSince -= tick;
      mTimeSince = Math.max(0.0f, mTimeSince);
    }
    
    draw();
        
  } // ApplicationListener.render()

  // update the game logic
  private void update() {

    mGameManager.advance();
    
  } // update()

  // display the game screen
  private void draw() {

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
    mSpriteBatch.begin();
    mGameManager.draw(mSpriteBatch);
    mTimingStats.display(mSpriteBatch);
    mSpriteBatch.end();
    
  } // draw()

} // class FloxelsGame

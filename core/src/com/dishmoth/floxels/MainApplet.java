/*
 *  MainApplet.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

// the game applet
public class MainApplet extends Applet 
                        implements Runnable, FocusListener, MouseListener {

  private static final long serialVersionUID = 1L;
  
  // assorted objects
  private Toolkit          mToolkit        = null;
  private Thread           mMainLoop       = null; 
  private Canvas           mGameCanvas     = null;
  private BufferStrategy   mBufferStrategy = null;
  private GameManager      mGameManager    = null;
  private TimingControl    mTimingControl  = null;

  // current applet state
  private volatile boolean mRunning;

  // constructor
  public MainApplet() {
    
    Env.setVerbose();
    
    setLayout(new BorderLayout());
    
  } // constructor

  // initialize the applet
  @Override
  public void init() {

    if ( Env.debugMode() ) {
      System.out.println("Floxels (v0.4, 1st January 2011)");
      System.out.println("Contact: dishmoth@yahoo.co.uk");
      System.out.println("");
    }

    if ( Env.debugMode() ) {
      System.out.println("Applet.init()");
      System.out.println("Screen size: " + getWidth() + " x " + getHeight());
    }
    
    mGameCanvas = new Canvas();
    mGameCanvas.setFocusable(true);
    mGameCanvas.setIgnoreRepaint(true);
    mGameCanvas.addFocusListener(this);
    mGameCanvas.addMouseListener(this);
    //mGameCanvas.setSize(Env.screenWidth(), Env.screenHeight());
    
    add(mGameCanvas, BorderLayout.CENTER);
    
    Env.initialize(this, getWidth(), getHeight());
    //Storage.initializeCookies(this);
    
    //Env.keys().monitor(mGameCanvas);
    Env.mouse().monitor(mGameCanvas);
    
    //setSize(Env.screenWidth(), Env.screenHeight());

    mToolkit = Toolkit.getDefaultToolkit();

    mGameManager = new GameManager(new FloxelStory());
    
    mTimingControl = new TimingControl();
    
    mRunning = true;
    
    Env.sounds().initialize();
    
    //setCursor(mToolkit.createCustomCursor(
    //                      new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
    //                      new Point(0,0), ""));
    
    mMainLoop = new Thread(this);
    mMainLoop.start();

  } // Applet.init()
  
  // start running
  @Override
  public void start() {

    if ( Env.debugMode() ) System.out.println("Applet.start()");
    mRunning = true;
    
  } // Applet.start()
  
  // stop running (pause)
  @Override
  public void stop() {

    if ( Env.debugMode() ) System.out.println("Applet.stop()");
    mRunning = false;
    
  } // Applet.stop()

  // discard resources and exit
  @Override
  public void destroy() {
    
    if ( Env.debugMode() ) System.out.println("Applet.destroy()");
    
    Env.sounds().stop();
    
    super.destroy();
    Thread thread = mMainLoop;
    mMainLoop = null;
    mRunning = false;
    if ( thread != null ) {
      try {
        thread.join();
      } catch (InterruptedException e) {
      }
    }
    
  } // Applet.destroy()
  
  // change of focus
  public void focusGained(FocusEvent event) { 

    if ( Env.debugMode() ) System.out.println("Applet.focusGained()");
    
  } // FocusListener.focusGained()
  
  // loss of focus
  public void focusLost(FocusEvent event) { 
    
    if ( Env.debugMode() ) System.out.println("Applet.focusLost()");
    
  } // FocusListener.focusLost()

  // click in window for focus
  public void mouseClicked(MouseEvent e) {
    
    mGameCanvas.requestFocusInWindow();
    
  } // MouseListener.mouseClicked()

  // MouseListener interface
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
 
  // execute the game
  public void run() {
    
    while ( mMainLoop != null ) {
      if ( mRunning ) runMain();
      else            runInactive();
    }
    
  } // Runnable.run()

  // run the main game loop
  private void runMain() {

    if ( Env.debugMode() ) System.out.println("Running");
    //Env.keys().reset();
    mTimingControl.reset();
    
    while ( mMainLoop != null && mRunning ) {
    
      // advance
      mGameManager.advance();
      long nanosAfterAdvance = System.nanoTime();

      // draw
      boolean skipDraw = mTimingControl.gameRunningSlow();
      if ( !skipDraw ) drawGameScreen();
      long nanosAfterDraw = System.nanoTime();

      // wait
      mTimingControl.tick(nanosAfterAdvance, nanosAfterDraw, skipDraw);
        
    }
    
    mTimingControl.report();
    
  } // runMain()
  
  // wait until the applet restarts
  private void runInactive() {

    if ( Env.debugMode() ) System.out.println("Stopped");

    Env.sounds().stop();
    
    while ( mMainLoop != null && !mRunning ) {
      try { 
        Thread.sleep(250);
        Thread.yield(); 
      } catch (InterruptedException ex) { return; }
    }
    
  } // runInactive()
  
  // draw the game screen
  private void drawGameScreen() {

    if ( mBufferStrategy == null ) {
      mGameCanvas.createBufferStrategy(2);
      mBufferStrategy = mGameCanvas.getBufferStrategy();
    }
    Graphics g = mBufferStrategy.getDrawGraphics();
    assert( g instanceof Graphics2D );

    //g.setClip(0, 0, Env.screenWidth(), Env.screenHeight());
    mGameManager.draw((Graphics2D)g);
    
    g.dispose();
    mToolkit.sync();
    if ( !mBufferStrategy.contentsLost() ) mBufferStrategy.show();

  } // drawGameScreen()

} // class MainApplet

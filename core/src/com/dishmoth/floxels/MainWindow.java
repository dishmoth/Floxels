/*
 *  MainWindow.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

// the main game window
public class MainWindow extends Frame implements Runnable {

  // main method
  public static void main(String args[]) {

    Env.setVerbose();
    
    boolean unknownArg = false;
    for ( int k = 0 ; k < args.length ; k++ ) {
      String arg = args[k].toLowerCase();
      if ( arg.equals("debug") ) Env.setVerbose();
      else                       unknownArg = true;
    }
    if ( unknownArg ) {
      System.out.println("java -jar <file>.jar [game] [options]");
      System.out.println("options: debug");
      System.exit(0);
    }
    
    //if ( Env.debugMode() ) {
    //  System.setProperty("sun.java2d.trace","count");
    //}
    
    new MainWindow();

  } // main()

  private static final long serialVersionUID = 1L;

  // window size
  private static final int kScreenWidth  = 600,
                           kScreenHeight = 600;
  
  // assorted objects
  private Toolkit        mToolkit        = null;
  private Thread         mMainLoop       = null; 
  private Canvas         mGameCanvas     = null;
  private BufferStrategy mBufferStrategy = null;
  private GameManager    mGameManager    = null;
  private TimingControl  mTimingControl  = null;

  // constructor
  public MainWindow() {

    Env.initialize(this, kScreenWidth, kScreenHeight);
    Env.reportAcceleratedMemory();

    mToolkit = Toolkit.getDefaultToolkit();
      
    mGameCanvas = new Canvas();
    Dimension canvasDim = new Dimension(Env.screenWidth(), 
                                        Env.screenHeight());
    mGameCanvas.setSize(canvasDim);
    mGameCanvas.setPreferredSize(canvasDim);
    //mGameCanvas.setMinimumSize(canvasDim);
    //mGameCanvas.setMaximumSize(canvasDim);
    
    //Env.keys().monitor(mGameCanvas);
    Env.mouse().monitor(mGameCanvas);
    Env.sounds().initialize();
    
    //getContentPane().removeAll();
    //getContentPane().setLayout(new BorderLayout());
    //getContentPane().add(mGameCanvas, BorderLayout.CENTER);
    add(mGameCanvas);
    
    Dimension screenDim = mToolkit.getScreenSize();
    setLocation((int)(0.4*(screenDim.width - Env.screenWidth())), 
                (int)(0.4*(screenDim.height - Env.screenHeight())));
    
    setIgnoreRepaint(true);
    setResizable(false);
    pack();   
    
    addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) { exit(); }
        public void windowDeiconified(WindowEvent e) { start(); }
        public void windowIconified(WindowEvent e) { stop(); }
      }
    );

    addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
      }
    );
    
    if ( mGameCanvas != null ) mGameCanvas.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
      }
    );
    
    requestFocusInWindow();
    validate();
    setVisible(true);

    mTimingControl = new TimingControl();
    
    prepareBufferStrategy();

    mGameManager = new GameManager(new FloxelStory());
    setTitle("Floxels");      

    //setCursor(mToolkit.createCustomCursor(
    //                      new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
    //                      new Point(0,0), ""));
      
    start();
        
  } // constructor
  
  // start the game loop in a new thread
  public void start() {

    if ( mMainLoop == null ) {
      mMainLoop = new Thread(this);
      mMainLoop.start();
    }
    requestFocusInWindow();

  } // start()
  
  // interrupt the game thread
  public void stop() {
    
    if ( mMainLoop != null ) {
      mMainLoop.interrupt();
    }
    mMainLoop = null;

    //Env.keys().reset();
    Env.sounds().stop();
    //Storage.flushCache();
    
    mTimingControl.report();
    
  } // stop()

  // end the game
  public void exit() {
 
    stop();
    System.exit(0);
    
  } // exit()
  
  // the game loop (mostly concerned with counting nanoseconds)
  public void run() {

    requestFocus();

    mTimingControl.reset();

    while ( mMainLoop == Thread.currentThread() ) {
      
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
    
    mMainLoop = null;
    
  } // Runnable.run()

  // black magic to try and get buffer strategy working
  private void prepareBufferStrategy() {
  
    try {
      EventQueue.invokeAndWait(new Runnable() { 
        public void run() {mGameCanvas.createBufferStrategy(2);} 
      });
    } catch (Exception ex) {
      if ( Env.debugMode() ) System.out.println("BufferStrategy: " + ex);
    }
    
    try {
      Thread.sleep(500);
    } catch ( InterruptedException ex ) {}

    mBufferStrategy = mGameCanvas.getBufferStrategy();
    
  } // prepareBufferStrategy()
  
  // draw the game image onto the off-screen buffer, then flip to the screen
  private void drawGameScreen() {
    
    Graphics g = mBufferStrategy.getDrawGraphics();
    assert( g instanceof Graphics2D );
    
    mGameManager.draw((Graphics2D)g);

    g.dispose();
    mToolkit.sync();
    if ( !mBufferStrategy.contentsLost() ) mBufferStrategy.show();
    
  } // drawGameScreen()

} // class MainWindow
  
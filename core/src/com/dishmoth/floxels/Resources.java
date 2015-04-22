/*
 *  Resources.java
 *  Copyright Simon Hern 2010
 *  Contact: dishmoth@yahoo.co.uk, www.dishmoth.com
 */

package com.dishmoth.floxels;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

// manager for graphics, sounds, etc.
public class Resources {

  // path for files
  static protected final String kRootPackageName = "floxels/";
  
  // constructor
  public Resources() {
    
  } // constructor
  
  // open a stream to a resource file (return null if error)
  // (file is assumed to be within resources package)
  public InputStream openResourceStream(String fileName) {

    try {
      String resourceName = kRootPackageName + fileName;
      URL url = Resources.class.getClassLoader().getResource(resourceName);
      return url.openStream();
    } catch (Exception ex) {
      throw new RuntimeException("Resources.openResourceStream(): bad resource " 
                                 + fileName + ": " + ex.getMessage());
    }
  
  } // openResourceStream()

  // return an image from file (exception if not known)
  public BufferedImage loadImage(String resourceName) {
    
    BufferedImage image = null;
    
    try {

      InputStream inS = openResourceStream(resourceName);
      BufferedInputStream inB = new BufferedInputStream(inS);
        
      image = ImageIO.read(inB);
      
      inB.close();
        
    } catch (Exception ex) {
      throw new RuntimeException("Resources.loadImage(): "
                                 + "error reading \"" + resourceName 
                                 + "\": " + ex.getClass().getName() 
                                 + " " + ex.getMessage());
    }
    
    return image;
    
  } // loadImage()
  
  /*
  // return the contents of a sound file as a Clip object
  public Clip loadSoundClip(String fileName) throws IOException {
    
    Clip clip = null;

    try {
      
      String resourceName = kRootPackageName + fileName;
      URL url = Resources.class.getClassLoader().getResource(resourceName);
      
      if ( fileName.endsWith(".ogg") ) {

        InputStream inStream = url.openStream();
        clip = OggDecode.toClip(inStream);
        inStream.close();

      } else {

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        audioStream.close();
        
      }
      
    } catch ( Exception ex ) {
      throw new IOException("Resources.loadSoundClip(): bad clip " 
                            + fileName + ": " + ex.getMessage());
    }
    
    return clip;
    
  } // loadSoundClip()
  */

  // return the contents of a sound file as a SoundEffect object
  public SoundEffect loadSoundEffect(String fileName) throws IOException {
    
    SoundEffect effect = null;
    
    try {
      
      String resourceName = kRootPackageName + fileName;
      URL url = Resources.class.getClassLoader().getResource(resourceName);
      
      if ( fileName.endsWith(".ogg") ) {

        InputStream inStream = url.openStream();
        effect = OggDecode.toSoundEffect(inStream);
        inStream.close();

      } else {

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        audioStream.close();
        effect = new SoundEffect(clip);
        
      }
      
    } catch ( Exception ex ) {
      throw new IOException("Resources.loadSoundEffect(): could not read " 
                            + fileName + ": " + ex.getMessage());
    }
    
    return effect;
    
  } // loadSoundEffect()
  
} // class Resources

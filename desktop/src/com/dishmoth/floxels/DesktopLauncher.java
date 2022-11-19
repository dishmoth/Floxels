/*
 *  DesktopLauncher.java
 *  Copyright (c) 2016 Simon Hern
 *  Contact: dishmoth@yahoo.co.uk, dishmoth.com, github.com/dishmoth
 */

package com.dishmoth.floxels;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
        DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode();
        System.out.println("Screen size: " + dm.width + "x"+ dm.height);
        final float scale = 0.85f;
        final int size = 10*Math.round(scale*Math.min(dm.width,dm.height)/10);
	  
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Floxels");
        config.setWindowedMode(size, size);
        config.setResizable(false);
        config.setWindowIcon(FileType.Internal, "DesktopIcon128.png",
                  "DesktopIcon32.png", "DesktopIcon16.png");
        new Lwjgl3Application(new FloxelsGame(), config);
	}
}

package com.dishmoth.floxels.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.dishmoth.floxels.FloxelsGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
	  DisplayMode dm = LwjglApplicationConfiguration.getDesktopDisplayMode();
	  System.out.println("Screen size: " + dm.width + "x"+ dm.height);
	  final float scale = 0.85f;
	  final int size = 10*Math.round(scale*Math.min(dm.width,dm.height)/10);
	  
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.width = size;
    config.height = size;
    config.resizable = false;
    config.addIcon("DesktopIcon128.png", FileType.Internal);
    config.addIcon("DesktopIcon32.png", FileType.Internal);
    config.addIcon("DesktopIcon16.png", FileType.Internal);
		new LwjglApplication(new FloxelsGame(), config);
	}
}

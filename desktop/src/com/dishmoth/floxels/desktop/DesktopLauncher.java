package com.dishmoth.floxels.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.dishmoth.floxels.FloxelsGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.width = 600;
    config.height = 600;
		new LwjglApplication(new FloxelsGame(), config);
	}
}

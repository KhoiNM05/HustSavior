package io.github.HustSavior.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;

public class ResolutionManager {
    private static ResolutionManager instance;
    private int currentWidth;
    private int currentHeight;
    private boolean isFullscreen;

    private ResolutionManager() {
        currentWidth = Gdx.graphics.getWidth();
        currentHeight = Gdx.graphics.getHeight();
        isFullscreen = Gdx.graphics.isFullscreen();
    }

    public static ResolutionManager getInstance() {
        if (instance == null) {
            instance = new ResolutionManager();
        }
        return instance;
    }

    public void setResolution(String resolution) {
        String[] dimensions = resolution.split("x");
        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);
        
        if (isFullscreen) {
            DisplayMode displayMode = findClosestDisplayMode(width, height);
            Gdx.graphics.setFullscreenMode(displayMode);
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
        
        currentWidth = width;
        currentHeight = height;
    }

    private DisplayMode findClosestDisplayMode(int targetWidth, int targetHeight) {
        DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();
        DisplayMode closest = displayModes[0];
        
        for (DisplayMode mode : displayModes) {
            if (mode.width == targetWidth && mode.height == targetHeight) {
                return mode;
            }
        }
        
        return closest;
    }

    public void toggleFullscreen() {
        if (isFullscreen) {
            Gdx.graphics.setWindowedMode(currentWidth, currentHeight);
        } else {
            DisplayMode displayMode = findClosestDisplayMode(currentWidth, currentHeight);
            Gdx.graphics.setFullscreenMode(displayMode);
        }
        isFullscreen = !isFullscreen;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public String getCurrentResolution() {
        return currentWidth + "x" + currentHeight;
    }
}

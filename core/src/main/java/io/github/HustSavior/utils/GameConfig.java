package io.github.HustSavior.utils;

public class GameConfig { // Pixels per meter for Box2D
    
    // Game dimensions
    public static final float GAME_WIDTH = 800f;
    public static final float GAME_HEIGHT = 800f;
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 16f;
    public static final float PPM = 100f;
    public static final short BIT_GROUND = 1;
    public static final short BIT_PLAYER = 2;
    public static final short BIT_MONSTER = 4;
    public static final short BIT_PLAYER_SENSOR = 8;
    public static final float MAP_WIDTH = 3200f;  // Set this to your map width in pixels
    public static final float MAP_HEIGHT = 3200f; // Set this to your map height in pixels
}

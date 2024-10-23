package main;

import javax.swing.JPanel; // Panel
import java.awt.*;
import entity.Player;


public class GamePanel extends JPanel implements Runnable {
    // Screen Settings
    final int originalTitleSize =16; // size of title
    final double scale = 3; // set scale

    public final int tileSize = (int)(originalTitleSize * scale);
    public final int maxScreenCol = 16; // 16 cols with width = tileSize
    public final int maxScreenRow = 12; // 12 rows with height = tileSize


    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;
    // FPS
    int FPS = 60;

    // tile


    // default position
    int playerX = 100;
    int playerY = 200;

    int playerSpeed =4;
    Thread GameThread;
    KeyHandler keyH = new KeyHandler();
    Player player = new Player(this, keyH);

    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void dstartGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run(){
        double drawInterval = 1000_000_000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int deltaCount = 0;
        while(gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime)/drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;
            if(delta >= 1){
                update();
                repaint();
                delta--;
            }
            if(timer >= drawInterval){
                System.out.println("FPS: " + FPS);
                deltaCount ++;
                timer = 0;
            }
        }
    }


    // game state update
    public void update(){
        player.update();
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        tileManager.draw(g2);
        player.draw(g2);
        g2.dispose();
    }
}

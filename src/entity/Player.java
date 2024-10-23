package entity;
import main.GamePanel;
import main.KeyHandler;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import main.GamePanel;
import main.KeyHandler;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        this.speed = 4;
        this.direction = "down";
    }
    public void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
    }
    public void getPlayerImage(){
        try{
            up1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            up2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            down1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            down2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            left1= ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            left2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            right1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
            right2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/char/")));
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    public void update() {
        if (keyH.upPressed == true || keyH.downPressed == true || keyH.leftPressed == true || keyH.rightPressed == true) {
            if (keyH.upPressed == true) {
                direction = "up";
                y -= speed;
            } else if (keyH.downPressed == true) {
                direction = "down";
                y += speed;
            } else if (keyH.leftPressed == true) {
                direction = "left";
                x -= speed;
            } else if (keyH.rightPressed == true) {
                direction = "right";
                x += speed;
            }
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }
    }
    public void draw(Graphics2D g2){
        // g2.setColor(Color.white);
        //g2.fillRect(x,y, gp.tileSize , gp.tileSize);
        //g2.dispose();

//        BufferedImage image = switch (direction) {
//            case "up" -> up1;
//            case "down" -> down1;
//            case "left" -> left1;
//            case "right" -> right1;
//            default -> null;
//        };
        BufferedImage image = null;
        switch(direction){
            case "up":
                if(spriteNum == 1){
                    image = up1;

                }
                if(spriteNum == 2){
                    image = up2;
                }
                break;
            case "down":
                if(spriteNum == 1){
                    image = down1;

                }
                if(spriteNum == 2){
                    image = down2;
                }
                break;
            case "left":
                if(spriteNum == 1){
                    image = left1;

                }
                if(spriteNum == 2){
                    image = left2;
                }
                break;
            case "right":
                if(spriteNum == 1){
                    image = right1;

                }
                if(spriteNum == 2){
                    image = right2;
                }
                break;
        }
        g2.drawImage(image, x, y, 8, 8, null);
    }
}

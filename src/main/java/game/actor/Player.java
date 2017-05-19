package game.actor;

import game.Game;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

public class Player extends Actor {
    public Player(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
    
    @Override
    public void logic() {
        super.logic();
    }
    
    @Override
    public void draw(Graphics2D g) {
        Point p = Game.CAMERA.getRenderPosition(this.x, this.y);
        
        g.setColor(Color.BLACK);
        g.drawOval(p.x, p.y, (int)Game.PLAYER.w, (int)Game.PLAYER.h);
    }
    
    public void keyPressed(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_A) {
            this.left = true;
        } else if(ke.getKeyCode() == KeyEvent.VK_D) {
            this.right = true;
        } else if(ke.getKeyCode() == KeyEvent.VK_W) {
            this.jump();
        } else if(ke.getKeyCode() == KeyEvent.VK_S) {
            
        }
    }
    
    public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_A) {
            this.left = false;
        } else if(ke.getKeyCode() == KeyEvent.VK_D) {
            this.right = false;
        } else if(ke.getKeyCode() == KeyEvent.VK_W) {
            
        } else if(ke.getKeyCode() == KeyEvent.VK_S) {
            
        }
    }
}

package game.camera;

import game.Game;
import game.actor.Actor;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.geom.Line2D;

public class Camera extends Rectangle {
    private Actor target;
    private final double trackFactor = .1;
    public int renderCount = 0;
    
    public Camera(int x, int y, int w, int h) {
        super(x, y, w, h);
    }
    
    public void logic() {
        this.trackTarget();
        
        this.checkBounds();
    }
    
    public void draw(Graphics2D g) {
        this.renderCount = 0;
        
        for(Actor a : Game.GOBS) {
            // Don't render objects that are off screen (ie. outside the camera's view).
            if(!this.intersects(a.getRect())) {
                continue;
            }
            
            a.draw(g);
            this.renderCount++;
        }
        
        // Draw walls.
        g.setColor(Color.BLACK);
        for(Line2D line : Game.WALLS) {
            if(!line.intersects(this)) {
                continue;
            }
            
            Point p1 = getRenderPosition(line.getX1(), line.getY1());
            Point p2 = getRenderPosition(line.getX2(), line.getY2());
            
            g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            this.renderCount++;
        }
        
        // Draw player.
        if(this.intersects(Game.PLAYER.getRect())) {
            Game.PLAYER.draw(g);
            this.renderCount++;
        }
    }
    
    private void trackTarget() {
        if(this.target == null) {
            return;
        }
        
        double diffX = this.target.getCenterX() - this.getCenterX();
        double diffY = this.target.getCenterY() - this.getCenterY();
        
        this.translate((int)(diffX * this.trackFactor), (int)(diffY * this.trackFactor));
    }
    
    private void checkBounds() {
        if(this.x < 0) {
            this.x = 0;
        } else if(this.x + this.width > Game.WORLD_WIDTH) {
            this.x = Game.WORLD_WIDTH - this.width;
        }
        
        if(this.y < 0) {
            this.y = 0;
        } else if(this.y + this.height > Game.WORLD_HEIGHT) {
            this.y = Game.WORLD_HEIGHT - this.height;
        }
    }
    
    public void setTarget(Actor a) {
        this.target = a;
    }
    
    /**
     * Get the position of a point as viewed through the camera.
     */
    public Point getRenderPosition(double x, double y) {
        return new Point((int)(x - this.x), (int)(y - this.y));
    }
}
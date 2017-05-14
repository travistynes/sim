package game.actor;

import game.Game;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Actor {
    public double x, y, w, h;
    private Rectangle.Double rect = new Rectangle.Double();
    
    public boolean up, down, left, right;
    private double scale = Game.WINDOW.w;
    private double rotation = Math.PI / 2; // Rotation in radians.
    private double rotationSpeed = (Math.PI / 180) * 6;
    public double speed = scale * .01;
    
    public Actor(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    public void logic() {
        this.move();
    }
    
    public void draw(Graphics2D g) {
        
    }
    
    private void move() {
        if(up) {
            this.y -= this.speed;
        }
        
        if(down) {
            this.y += this.speed;
        }
        
        if(left) {
            this.rotation += this.rotationSpeed;
            if(this.rotation > Math.PI) { this.rotation = Math.PI; }
        }
        
        if(right) {
            this.rotation -= this.rotationSpeed;
            if(this.rotation < 0) { this.rotation = 0; }
        }
        
        if(!left && !right) {
            // Rotate towards 90 to stop left/right movement.
            if(this.rotation > Math.PI / 2) { this.rotation -= this.rotationSpeed; }
            if(this.rotation < Math.PI / 2) { this.rotation += this.rotationSpeed; }
            
            /*
            We'll always get within the rotationSpeed radians to PI / 2, but never hit it exactly.
            If we get within that threshold, then set rotation to PI / 2.
            */
            if(this.rotation <= ((Math.PI / 2) + this.rotationSpeed) && this.rotation >= ((Math.PI / 2) - this.rotationSpeed)) {
                this.rotation = Math.PI / 2;
            }
        }
        
        // Move.
        this.x += Math.cos(this.rotation) * this.speed;
    }
    
    public double getCenterX() {
        return (this.x + this.w) - (this.w / 2);
    }
    
    public double getCenterY() {
        return (this.y + this.h) - (this.h / 2);
    }
    
    public Rectangle.Double getRect() {
        this.rect.x = this.x;
        this.rect.y = this.y;
        this.rect.width = this.w;
        this.rect.height = this.h;
        
        return this.rect;
    }
}

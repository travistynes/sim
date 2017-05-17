package game.actor;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import game.util.MathHelper;

public abstract class Actor {
    public double x, y, w, h;
    private Rectangle.Double rect = new Rectangle.Double();
    
    public boolean up, down, left, right;
    
    // Horizontal movement variables
    private double xRotation = MathHelper.PiOver2; // Initial 90 degrees - No horizontal movement.
    private double xRotationSpeed = (Math.PI / 180) * 6;
    private double xSpeed = 10;
    
    // Vertical movement variables
    private double yRotation = Math.PI; // Initial 180 degress - No vertical movement.
    private double yRotationSpeed = (Math.PI / 180) * 6;
    private double ySpeed = 15;
    private boolean jumping = false;
    
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
        // Horizontal movement
        if(left) {
            this.xRotation += this.xRotationSpeed;
            if(this.xRotation > Math.PI) { this.xRotation = Math.PI; }
        }
        
        if(right) {
            this.xRotation -= this.xRotationSpeed;
            if(this.xRotation < 0) { this.xRotation = 0; }
        }
        
        if(!left && !right) {
            // Rotate towards 90 to stop left/right movement.
            if(this.xRotation > MathHelper.PiOver2) { this.xRotation -= this.xRotationSpeed; }
            if(this.xRotation < MathHelper.PiOver2) { this.xRotation += this.xRotationSpeed; }
            
            /*
            We'll always get within the rotationSpeed radians to PI / 2, but never hit it exactly.
            If we get within that threshold, then set rotation to PI / 2.
            */
            if(this.xRotation <= (MathHelper.PiOver2 + this.xRotationSpeed) && this.xRotation >= (MathHelper.PiOver2 - this.xRotationSpeed)) {
                this.xRotation = MathHelper.PiOver2;
            }
        }
        
        // Move on x.
        this.x += Math.cos(this.xRotation) * this.xSpeed;
        
        // Vertical movement
        if(this.yRotation < MathHelper.ThreePiOver2) {
            this.yRotation += this.yRotationSpeed;
            
            if(this.yRotation > MathHelper.ThreePiOver2) {
                this.yRotation = MathHelper.ThreePiOver2;
            }
        }
        
        // Move on y.
        this.y -= Math.sin(this.yRotation) * this.ySpeed;
        
        // Check for ground collision.
        if(this.y > 400) {
            this.y = 400;
            this.yRotation = Math.PI;
            this.jumping = false;
        }
    }
    
    public void jump() {
        if(!this.jumping && this.yRotation == Math.PI) {
            this.jumping = true;
            this.yRotation = MathHelper.PiOver2;
        }
    }
    
    public double getCenterX() {
        return this.getRect().getCenterX();
    }
    
    public double getCenterY() {
        return this.getRect().getCenterY();
    }
    
    public Rectangle.Double getRect() {
        this.rect.x = this.x;
        this.rect.y = this.y;
        this.rect.width = this.w;
        this.rect.height = this.h;
        
        return this.rect;
    }
}

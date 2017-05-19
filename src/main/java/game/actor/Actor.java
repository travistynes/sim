package game.actor;

import game.Game;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import game.util.MathHelper;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public abstract class Actor {
    public double x, y, w, h;
    private Rectangle.Double rect = new Rectangle.Double();
    
    public boolean up, down, left, right;
    
    // Horizontal movement variables
    private double xRotation = MathHelper.PiOver2; // Initial 90 degrees - No horizontal movement.
    private double xRotationSpeed = (Math.PI / 180) * 6;
    private double xSpeed = 5;
    
    // Vertical movement variables
    private double yRotation = Math.PI; // Initial 180 degress - No vertical movement.
    private double yRotationSpeed = (Math.PI / 180) * 6;
    private double ySpeed = 10;
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
        // Get current position, prior to move.
        Point2D p = new Point2D.Double(this.getCenterX(), this.getCenterY());
        double curX = this.x;
        double curY = this.y;
        
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
        
        // Check for wall collision.
        for(Line2D line : Game.WALLS) {
            if(line.intersectsLine(p.getX(), p.getY(), this.getCenterX(), this.getCenterY())) {
                // Reset position.
                this.x = curX;
                
                /*
                Treat the line as though the left most point were at the origin so we can
                get the angle of the line. If the left point is p1 and the right point is p2,
                the formula is normally (x2 - x1, y2 - y1), but note for y we subtract y1 - y2
                because y increases downward on our canvas.
                */
                Point2D p2 = null;
                if(line.getX2() >= line.getX1()) {
                    p2 = new Point2D.Double(line.getX2() - line.getX1(), line.getY1() - line.getY2());
                } else {
                    p2 = new Point2D.Double(line.getX1() - line.getX2(), line.getY2() - line.getY1());
                }
                
                // Get the angle of the line.
                double theta = Math.atan2(p2.getY(), p2.getX());
                
                if(Math.abs(theta) > 2 * MathHelper.PiOver6 && Math.abs(theta) < 4 * MathHelper.PiOver6) {
                    // Angle is too steep to climb.
                    this.xRotation = MathHelper.PiOver2; // Reset horizontal momentum.
                } else {
                    // Move in x, y with respect to the angle of the line, and by a factor of the current horizontal momentum.
                    this.x += Math.cos(theta) * Math.cos(this.xRotation) * this.xSpeed;
                    this.y -= Math.sin(theta) * Math.cos(this.xRotation) * this.xSpeed;

                    // Check for *another* wall collision after the above movement.
                    for(Line2D line1 : Game.WALLS) {
                        if(line1.intersectsLine(p.getX(), p.getY(), this.getCenterX(), this.getCenterY())) {
                            // Reset position.
                            this.x = curX;
                            this.y = curY;
                            this.xRotation = MathHelper.PiOver2; // Reset horizontal momentum.
                            break;
                        }
                    }

                    // Update current y position for the upcoming vertical movement and collision check.
                    curY = this.y;
                }
                
                break;
            }
        }
        
        // Vertical movement
        if(this.yRotation < MathHelper.ThreePiOver2) {
            this.yRotation += this.yRotationSpeed;
            
            if(this.yRotation > MathHelper.ThreePiOver2) {
                this.yRotation = MathHelper.ThreePiOver2;
            }
        }
        
        // Move on y.
        this.y -= Math.sin(this.yRotation) * this.ySpeed;
        
        // If there was a collision with a wall, reset position.
        for(Line2D line : Game.WALLS) {
            if(line.intersectsLine(p.getX(), p.getY(), this.getCenterX(), this.getCenterY())) {
                this.y = curY;
                this.yRotation = Math.PI;
                this.jumping = false;
                
                break;
            }
        }
    }
    
    public void jump() {
        // Allow a jump if not currently jumping, and if not falling too fast yet.
        if(!this.jumping && this.yRotation < Math.PI + (this.yRotationSpeed * 10)) {
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

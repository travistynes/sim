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
    private double xSpeed = 7;
    
    // Vertical movement variables
    private double yRotation = Math.PI; // Initial 180 degress - No vertical movement.
    private double yRotationSpeed = (Math.PI / 180) * 6;
    private double ySpeed = 10;
    private boolean jumping = false;
    private double steepAngle = MathHelper.degree * 25; // Degress +/- from PI / 2 that constitutes a steep angle.
    
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
                
                double theta = MathHelper.lineAngle(line);
                
                if(Math.abs(theta) > MathHelper.PiOver2 - steepAngle && Math.abs(theta) < MathHelper.PiOver2 + steepAngle) {
                    // Angle is too steep to climb.
                    this.xRotation = MathHelper.PiOver2; // Reset horizontal momentum.
                } else {
                    // Move in x, y with respect to the angle of the line, and by a factor of the current horizontal momentum.
                    this.x += Math.cos(theta) * Math.cos(this.xRotation) * this.xSpeed;
                    
                    /*
                    There are subtle aspects that we need to consider when moving in y. This explanation is mostly
                    to do with why we move by the absolute value of the sine of the angle of the line * x rotation cosine.
                    
                    We don't need to move by the absolute value of the sine of the angle of the line. When we are
                    moving to the right up a slope, the slope has a positive angle, and because we are moving right,
                    we're factoring in the positive cosine of our x rotation. In this case, we move up.
                    
                    When we're moving left up a slope, the slope has a negative angle, but since we are moving left,
                    we factor in the negative cosine of our x rotation and the final result is positive.
                    Again, we move up.
                    
                    This works mathematically, and for the most part it works in practice. But there is a tricky situation
                    that can arise which breaks down the movement: The player can periodically end up at exactly the
                    right position on x, y at a join point of two lines. Imagine moving leftward up the slope and
                    ending up right at that point. The next movement left (negative cosine of our x rotation) will
                    intersect the downward slope (positive angle) and we end up trying to move down on y, instead of up.
                    Since the downward movement results in an intersection with the ground, the position is reset,
                    and we're stuck.
                    
                    The fix is to always move up. This works because even if we're colliding from underneath a slope,
                    we just fail the intersection test and reset the position. We add the additional 1 movement upward
                    to "clear" the tops of slopes and prevent intersecting the other side, just to be safe.
                    */
                    this.y -= Math.abs(Math.sin(theta) * Math.cos(this.xRotation) * this.xSpeed) + 1;

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
        
        // Check for collision.
        for(Line2D line : Game.WALLS) {
            if(line.intersectsLine(p.getX(), p.getY(), this.getCenterX(), this.getCenterY())) {
                if(curY < this.y) {
                    // Ground collision.
                    this.jumping = false;
                    
                    // Reset position on y.
                    this.y = curY;
                    
                    // Slide down slope, if too steep. Get the angle of the line we've landed on.
                    double theta = MathHelper.lineAngle(line);

                    if(Math.abs(theta) > MathHelper.PiOver2 - steepAngle && Math.abs(theta) < MathHelper.PiOver2 + steepAngle) {
                        // Angle is too steep.
                        
                        // Slide down slope.
                        this.x -= Math.cos(theta) * this.ySpeed * (theta / Math.abs(theta));
                        this.y += Math.abs(Math.sin(theta)) * Math.abs(Math.sin(this.yRotation)) * this.ySpeed;

                        // Check for *another* wall collision after the above movement.
                        for(Line2D line1 : Game.WALLS) {
                            if(line1.intersectsLine(p.getX(), p.getY(), this.getCenterX(), this.getCenterY())) {
                                // Reset position.
                                this.x = curX;
                                this.y = curY;
                                this.yRotation = Math.PI; // Reset vertical momentum.
                                
                                break;
                            }
                        }
                    } else {
                        // Angle is not too steep.
                        this.yRotation = Math.PI; // Reset vertical momentum.
                    }
                } else {
                    // Not a ground collision. Reset y position and reset vertical momentum.
                    this.y = curY;
                    this.yRotation = Math.PI;
                }
                
                break;
            }
        }
    }
    
    public void jump() {
        // Allow a jump if not currently jumping, and if not falling too fast yet.
        if(!this.jumping && this.yRotation < Math.PI + (this.yRotationSpeed * 7)) {
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

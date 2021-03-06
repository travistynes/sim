package game;

import game.actor.Actor;
import org.apache.log4j.Logger;
import java.util.Random;
import java.awt.Graphics2D;
import game.camera.Camera;
import game.actor.Player;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.ArrayList;

public class Game {
    private static final Logger log = Logger.getLogger(Game.class);
    
    private static final Random RAN = new Random(0);
    public static Window WINDOW;
    public static int WORLD_WIDTH;
    public static int WORLD_HEIGHT;
    public static Camera CAMERA;
    public static Player PLAYER;
    public static List<Actor> GOBS = new ArrayList<Actor>();
    public static List<Line2D> WALLS = new ArrayList<Line2D>();
    
    public static final int TARGET_FPS = 60; // Target frames per second.
    public static final double TICK = 1000 / TARGET_FPS; // Tick duration in milliseconds needed to hit the target frame rate.
    public static int FPS = 0; // FPS counter.
    public static int SIMFPS = 0; // Simulated FPS counter.
    
    public static boolean RUN = true;
    
    /**
     * Static initializer.
     */
    static {
        try {
            //System.getProperties().list(System.out);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Game.start();
        
        Game.log("Shutdown complete.");
    }
    
    public static void start() throws Exception {
        // Create window.
        int width = 1000;
        int height = 600;
        WINDOW = new Window("sim", width, height);
        
        // Set world size.
        WORLD_WIDTH = WINDOW.w * 5;
        WORLD_HEIGHT = WINDOW.h * 5;
        
        // Create background grid.
        int num = 25;
        for(int a = 0; a < num; a++) {
            for(int b = 0; b < num; b++) {
                int w = WORLD_WIDTH / num;
                int h = WORLD_HEIGHT / num;
                int x = a * w;
                int y = b * h;
                
                GOBS.add(new Actor(x, y, w, h) {
                    public void draw(Graphics2D g) {
                        Point p = Game.CAMERA.getRenderPosition(this.x, this.y);
                        
                        g.setColor(new Color(1, 1, 1, 1f));
                        g.fillRect(p.x, p.y, (int)this.w, (int)this.h);
                        
                        g.setColor(new Color(0, 0, 0, 0.07f));
                        g.drawRect(p.x, p.y, (int)this.w, (int)this.h);
                        
                        if(PLAYER.getRect().intersects(this.getRect())) {
                            //g.setColor(new Color(1, 0, 0, .01f));
                            //g.fillRect(p.x, p.y, (int)this.w, (int)this.h);
                        }
                    }
                });
            }
        }
        
        // Create left, right walls.
        WALLS.add(new Line2D.Double(0, 0, 0, WORLD_HEIGHT));
        WALLS.add(new Line2D.Double(WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT));
        
        // Create random ground line.
        int prevX = 0;
        int prevY = WORLD_HEIGHT / 2;
        for(int a = 0; a < WORLD_WIDTH; a++) {
            int x1 = Game.ran(200) + 10 + prevX;
            int y1 = Game.ran(30);
            double s = Game.ran();
            if(s <= .5) { y1 = -y1; } // Negative
            y1 += prevY;
            
            WALLS.add(new Line2D.Double(prevX, prevY, x1, y1));
            
            prevX = x1;
            prevY = y1;
            
            if(prevX >= WORLD_WIDTH) {
                break;
            }
        }
        
        // Create random lines.
        prevX = 0;
        prevY = (WORLD_HEIGHT / 2) - 20;
        for(int a = 0; a < WORLD_WIDTH; a++) {
            int x1 = Game.ran(300) + 10 + prevX;
            int y1 = Game.ran(30);
            double s = Game.ran();
            if(s <= .5) { y1 = -y1; } // Negative
            y1 += prevY;
            
            if(Game.ran() >= .2) {
                WALLS.add(new Line2D.Double(prevX, prevY, x1, y1));
            }
            
            prevX = x1;
            prevY = y1;
            
            if(prevX >= WORLD_WIDTH) {
                break;
            }
        }
        
        // Steep angle slide demonstration.
        WALLS.add(new Line2D.Double(200, (WORLD_HEIGHT / 2) - 40, 650, (WORLD_HEIGHT / 2) - 500));
        WALLS.add(new Line2D.Double(650, (WORLD_HEIGHT / 2) - 500, 750, (WORLD_HEIGHT / 2) - 500));
        WALLS.add(new Line2D.Double(750, (WORLD_HEIGHT / 2) - 500, 950, (WORLD_HEIGHT / 2) - 40));
        
        // Create player.
        int playersize = 10;
        PLAYER = new Player(0, (WORLD_HEIGHT / 2) - 100, playersize, playersize);
        
        // Create camera.
        CAMERA = new Camera(0, 0, WINDOW.w, WINDOW.h);        
        CAMERA.setTarget(PLAYER);
        
        // Start game loop.
        loop();
    }
    
    /*
    The game loop runs logic at a fixed frame rate. Instead of
    multiplying the time elapsed per frame to all movement, acceleration, etc. (variable framerate)
    we run the physics step multiple times to catch up when running slower than
    the target framerate.
    */
    public static void loop() {
        double tick = Game.TICK;
        double accumulator = 0; // Time counter.
        
        long fpsStart = 0;
        int frames = 0;
        int simframes = 0;
        
        while(Game.RUN) {
            long start = System.currentTimeMillis();
            //Game.log("accum start: " + accumulator);
            
            while(accumulator >= tick) {
                accumulator -= tick;
                
                // Perform game logic.
                Game.logic();
                
                simframes++; // This will count the simulated frame rate, which should try to match the target frame rate.
            }
            
            // Perform drawing.
            Game.WINDOW.draw();
            
            // Count frame rate.
            frames++; // This will count the real frame rate, which can vary.
            if(System.currentTimeMillis() > fpsStart + 1000) {
                //Game.log("real fps: " + FPS + ", sim fps: " + SIMFPS);
                fpsStart = System.currentTimeMillis();
                FPS = frames;
                SIMFPS = simframes;
                frames = 0;
                simframes = 0;
            }
            
            long tickDuration = System.currentTimeMillis() - start;
            long leftover = (long)(tick - tickDuration);
            //Game.log("tick duration: " + tickDuration);
            //Game.log("sleeping: " + leftover);
            
            // Sleep for the remainder of this tick.
            if(leftover > 0) {
                try { Thread.sleep(leftover); } catch(InterruptedException e) {}
            }
            
            //Game.log("adding time: " + (System.currentTimeMillis() - start));
            //Game.log("---------------------");
            accumulator += System.currentTimeMillis() - start;
        }
    }
    
    public static double ran() {
        return RAN.nextDouble();
    }
    
    public static int ran(int max) {
        return RAN.nextInt(max);
    }
    
    public static void log(String msg) {
        System.out.println(msg);
    }
    
    /**
     * Process game logic.
     */
    private static void logic() {
        // Process game objects.
        PLAYER.logic();
        
        // Update camera.
        CAMERA.logic();
    }
    
    protected static void draw(Graphics2D g) {
        if(Game.CAMERA == null) { return; }
        
        Game.CAMERA.draw(g);
    }
}
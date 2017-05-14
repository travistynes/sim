package game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;


public class Window extends JFrame {
    private static final Logger log = Logger.getLogger(Window.class);
    public static final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private final MPanel panel;
    
    public static final int SCREEN_WIDTH = graphicsEnvironment.getDefaultScreenDevice().getDisplayMode().getWidth();
    public static final int SCREEN_HEIGHT = graphicsEnvironment.getDefaultScreenDevice().getDisplayMode().getHeight();
    
    public final int w;
    public final int h;
    
    private static Font FONT_TITLE;
    private static Font FONT_BASIC;
    
    static {
        try {
            // Load fonts.
            loadFonts();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Window(String title, int width) throws Exception {
        super(title);
        
        this.w = width;
        this.h = (int)(w * ((double)SCREEN_HEIGHT / SCREEN_WIDTH)); // Maintain aspect ratio of screen so it looks good if window is maximized.
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(w, h);
        this.setLocation((SCREEN_WIDTH / 2) - (w / 2), (SCREEN_HEIGHT / 2) - (h / 2));
        this.setIconImage(ImageIO.read(Window.class.getClassLoader().getResourceAsStream("images/gameicon.png")));
        
        // Create drawing surface.
        this.panel = new MPanel(w, h);
        this.add(panel);
        this.setVisible(true);
        
        // Register event listeners.
        this.registerKeyboardListener();
    }
    
    public void draw() {
        this.panel.repaint();
    }
    
    private static void loadFonts() throws Exception {
        // Load and register fonts.
        Font f = Font.createFont(Font.TRUETYPE_FONT, Window.class.getClassLoader().getResourceAsStream("fonts/BPreplay.otf"));
        Window.graphicsEnvironment.registerFont(f);
        FONT_TITLE = new Font(f.getFontName(), Font.PLAIN, 40);
        
        f = Font.createFont(Font.TRUETYPE_FONT, Window.class.getClassLoader().getResourceAsStream("fonts/advent-Bd1.otf"));
        Window.graphicsEnvironment.registerFont(f);
        FONT_BASIC = new Font(f.getFontName(), Font.PLAIN, 20);
    }
    
    private static class MPanel extends JPanel {
        private final BufferedImage canvas;
        
        public MPanel(int w, int h) throws Exception {
            super();
            
            this.canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            Graphics2D g = canvas.createGraphics();
            
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear.
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            // Draw game.
            Game.draw(g);
            
            // Draw stats overlay.
            g.setColor(Color.BLACK);
            g.setFont(FONT_BASIC);
            g.drawString("Simulation", 5, 20);
            g.drawString("fps: " + Game.FPS, 5, 50);
            g.drawString("render count: " + (Game.CAMERA != null ? Game.CAMERA.renderCount : 0), 5, 80);
            
            // Draw title.
            g.setColor(Color.BLACK);
            g.setFont(FONT_TITLE);
            String title = "Simulation";
            Rectangle2D r = g.getFontMetrics().getStringBounds(title, g);
            ///g.drawString(title, (this.canvas.getWidth() / 2) - ((int)r.getWidth() / 2), (this.canvas.getHeight() / 2) - ((int)r.getHeight() / 2));
            
            g.dispose();
            
            graphics.drawImage(canvas, 0, 0, this.getBounds().width, this.getBounds().height, null);
        }
    }
    
    private void registerKeyboardListener() {
        KeyListener keys = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
                return;
            }
            
            @Override
            public void keyPressed(KeyEvent ke) {
                Game.PLAYER.keyPressed(ke);
            }
            
            @Override
            public void keyReleased(KeyEvent ke) {
                Game.PLAYER.keyReleased(ke);
            }
        };
        
        this.addKeyListener(keys);
    }
}
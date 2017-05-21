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
    private static Font FONT_MONOSPACED;
    
    static {
        try {
            // Load fonts.
            loadFonts();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create the window with the given width and height.
     * 
     * The following comments are important with regard to graphics scaling.
     * If we specify the window to have width 1000 and height 600, the JFrame (window)
     * will be created at that size including its borders, title, etc. Inside the window
     * we will have a viewable area (the content pane) where we will create our drawing surface (canvas).
     * The content pane will be a little smaller than the window size. It is important to create
     * the canvas using the size of the content pane area, not the window size, otherwise
     * the canvas will be slightly scaled down to fit into the content pane of the window.
     * Due to the scaling down to a non integer ratio of the original size, there will be graphical
     * artifacts. For example, imagine 2 pixels scaled up to fit into 3 pixels, the final image will
     * obviously not look exactly like the original. Worse, this appears as pixel "flickering" as objects
     * move around the screen.
     */
    public Window(String title, int width, int height) throws Exception {
        super(title);
        
        this.w = width;
        this.h = height;
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(w, h);
        this.setLocation((SCREEN_WIDTH / 2) - (w / 2), (SCREEN_HEIGHT / 2) - (h / 2));
        this.setIconImage(ImageIO.read(Window.class.getClassLoader().getResourceAsStream("images/gameicon.png")));
        
        // The window must be visible before creating the canvas in order to get the window's content pane size.
        this.setVisible(true);
        
        // Create drawing surface (canvas).
        this.panel = new MPanel(this.getContentPane().getWidth(), this.getContentPane().getHeight());
        this.add(panel);
        
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
        
        FONT_MONOSPACED = new Font("Monospaced", Font.PLAIN, 20);
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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear.
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            // Draw game.
            Game.draw(g);
            
            // Draw diagnostics overlay.
            g.setColor(Color.BLACK);
            g.setFont(FONT_MONOSPACED);
            g.drawString("Simulation", 5, 20);
            g.drawString("fps: " + Game.FPS, 5, 40);
            g.drawString("render count: " + (Game.CAMERA != null ? Game.CAMERA.renderCount : 0), 5, 60);
            g.drawString("viewport: " + this.getBounds().width + ", " + this.getBounds().height, 5, 80);
            g.drawString("position: " + Game.PLAYER.x + ", " + Game.PLAYER.y, 5, 100);
            
            // Draw title.
            g.setColor(Color.BLACK);
            g.setFont(FONT_TITLE);
            String title = "Simulation";
            Rectangle2D r = g.getFontMetrics().getStringBounds(title, g);
            //g.drawString(title, (this.canvas.getWidth() / 2) - ((int)r.getWidth() / 2), (this.canvas.getHeight() / 2) - ((int)r.getHeight() / 2));
            
            g.dispose();
            
            /*
            Scale the image to fit the window. At the default window size, the
            canvas will be displayed at original size. But the user can resize the window.
            It is possible to resize the canvas so that its size remains an integer
            ratio of its original size: the integer division of the window width / original canvas width
            is the scale factor to multiply the new canvas width and height by. For example,
            
            Window width: 2500
            Original canvas width: 1000
            2500 / 1000 = 2.5 -> integer division = scale factor 2
            
            The width and height of the canvas would be multiplied by 2, giving it an integer
            ratio of its original size and maintaining the aspect ratio.
            
            But the user could just make the window wider.
            Then the canvas height would be too large and go outside of the window.
            It's a lot of trouble to keep the canvas fully inside the window AND maintain the original
            aspect ratio of the canvas. For now, I've decided I would rather squish the canvas to whatever
            size the user wants to make the window.
            */
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
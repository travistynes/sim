package game.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class MathHelper {
    public static final double degree = Math.PI / 180; // One degree in radians.
    public static final double PiOver2 = Math.PI / 2;
    public static final double PiOver8 = Math.PI / 8;
    public static final double ThreePiOver2 = 3 * PiOver2;
    
    public static double lineAngle(Line2D line) {
        /*
        Treat the line as though the left most point were at the origin so we can
        get the angle of the line. If the left point is p1 and the right point is p2,
        the formula is normally (x2 - x1, y2 - y1), but note for y we subtract y1 - y2
        because y increases downward on our canvas.
        
        Note: under these conditions, a slope moving up and right is a positive angle.
        A slope moving down and right is a negative angle.
        */
        Point2D p = null;
        
        if(line.getX2() >= line.getX1()) {
            p = new Point2D.Double(line.getX2() - line.getX1(), line.getY1() - line.getY2());
        } else {
            p = new Point2D.Double(line.getX1() - line.getX2(), line.getY2() - line.getY1());
        }

        // Get the angle of the line.
        double theta = Math.atan2(p.getY(), p.getX());
        
        return theta;
    }
}

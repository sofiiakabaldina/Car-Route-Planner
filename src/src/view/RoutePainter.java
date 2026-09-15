package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Paints a route onto the map using a black outline and colored center line.
 * Converts geographic coordinates into pixel coordinates and renders the
 * route on a JXMapViewer map component.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class RoutePainter implements Painter<JXMapViewer> {
    /** The color used for the visible route line. */
    private Color myColor;

    /** The list of geographic positions that make up the route. */
    private List<GeoPosition> myTrack;

    /**
     * Constructs a route painter with the specified route points and color.
     *
     * @param theTrack the geographic positions that make up the route
     * @param theColor the color used to draw the route
     */
    public RoutePainter(final List<GeoPosition> theTrack, final Color theColor) {

        myTrack = new ArrayList<>(theTrack);
        myColor = theColor;
    }

    /**
     * Paints the route on the map.
     *
     * @param theGraphics the graphics context used for drawing
     * @param theMap the map viewer on which the route is drawn
     * @param theWidth the width of the painting area
     * @param theHeight the height of the painting area
     */
    @Override
    public void paint( Graphics2D theGraphics, final JXMapViewer theMap, final int theWidth, final int theHeight) {
        if (myTrack == null || myTrack.size() < 2) {
            return;
        }

        theGraphics = (Graphics2D) theGraphics.create();

        // Correct way to offset for JXMapViewer viewport
        Rectangle viewportBounds = theMap.getViewportBounds();
        theGraphics.translate(-viewportBounds.getX(), -viewportBounds.getY());

        theGraphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Black outline
        theGraphics.setColor(Color.BLACK);
        theGraphics.setStroke(new BasicStroke(4));
        drawRoute(theGraphics, theMap);

        // Colored line on top
        theGraphics.setColor(myColor);
        theGraphics.setStroke(new BasicStroke(2));
        drawRoute(theGraphics, theMap);

        theGraphics.dispose();
    }

    /**
     * Draws line segments between each geographic position in the route.
     *
     * @param theGraphics the graphics context used to draw the route
     * @param theMap the map viewer used to convert geographic positions to pixels
     */
    private void drawRoute(final Graphics2D theGraphics, final JXMapViewer theMap) {

        int lastX = 0;
        int lastY = 0;

        boolean first = true;

        for (GeoPosition geoPositon : myTrack) {

            //convert geo-coordinate to bitmap pixel
            Point2D pixelPoint = theMap.getTileFactory().geoToPixel(geoPositon, theMap.getZoom());

            if (first) {
                first = false;
            }
            else {
                theGraphics.drawLine(lastX, lastY, (int) pixelPoint.getX(), (int) pixelPoint.getY());
            }

            lastX = (int) pixelPoint.getX();
            lastY = (int) pixelPoint.getY();

        }
    }

}
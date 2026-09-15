package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Paints a route information box on the map displaying estimated travel
 * time and route distance near a specified geographic position.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class RouteInfoPainter implements Painter<JXMapViewer>{
    /** The geographic position where the information box will be displayed. */
    private final GeoPosition myPosition;

    /** The estimated travel time displayed in the information box. */
    private final String myTimeText;

    /** The route distance displayed in the information box. */
    private final String myDistanceText;

    /**
     * Constructs a RouteInfoPainter with the specified location and route
     * information.
     *
     * @param thePosition the geographic position where the information box
     *                    should be displayed
     * @param theTimeText the travel time text to display
     * @param theDistanceText the distance text to display
     */
    public RouteInfoPainter(final GeoPosition thePosition,
                            final String theTimeText,
                            final String theDistanceText) {
        myPosition = thePosition;
        myTimeText = theTimeText;
        myDistanceText = theDistanceText;
    }

    /**
     * Paints the route information box on the map.
     *
     * @param theGraphics the graphics context used for drawing
     * @param theMap the map viewer on which the information box is drawn
     * @param theWidth the width of the drawing area
     * @param theHeight the height of the drawing area
     */
    @Override
    public void paint(final Graphics2D theGraphics, final JXMapViewer theMap, final int theWidth, final int theHeight) {
        Graphics2D graphicsCopy = (Graphics2D) theGraphics.create();

        Rectangle viewport = theMap.getViewportBounds();
        graphicsCopy.translate(-viewport.x, -viewport.y);

        Point2D pixelPoint = theMap.getTileFactory().geoToPixel(myPosition, theMap.getZoom());

        int boxX = (int) pixelPoint.getX() + 15;
        int boxY = (int) pixelPoint.getY() - 45;

        int boxWidth = 125;
        int boxHeight = 50;

        graphicsCopy.setColor(new Color(35, 35, 40));
        graphicsCopy.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 5, 5);

        graphicsCopy.setColor(Color.BLACK);
        graphicsCopy.setStroke(new BasicStroke(2));
        graphicsCopy.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 5, 5);

        graphicsCopy.setColor(Color.WHITE);
        graphicsCopy.setFont(new Font("SansSerif", Font.BOLD, 15));
        graphicsCopy.drawString("🚗 " + myTimeText, boxX + 12, boxY + 18);

        graphicsCopy.setFont(new Font("SansSerif", Font.PLAIN, 12));
        graphicsCopy.drawString(myDistanceText, boxX + 32, boxY + 37);

        graphicsCopy.dispose();
    }
}


package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * A map overlay painter that renders obstacle icons at each blocked road position.
 *
 * @author Sofiia Kabaldina
 * @version Spring 2026
 */
public class ObstaclePainter implements Painter<JXMapViewer> {

    /** Geographic positions where obstacle icons should be drawn. */
    private final List<GeoPosition> myObstaclePositions;
    /** Icon image loaded from resources, or null if unavailable. */
    private BufferedImage myObstacleImage;
    /** Width and height in pixels at which the obstacle icon is rendered on the map. */
    private static final int ICON_SIZE = 12;

    /**
     * Constructs an ObstaclePainter for the given list of obstacle positions.
     * Attempts to load obstacle.png from the classpath. If the image
     * cannot be found or read, a red X fallback will be drawn instead.
     *
     * @param thePositions - the list of geographic positions where obstacles should appear
     */
    public ObstaclePainter(final List<GeoPosition> thePositions) {
        myObstaclePositions = new ArrayList<>(thePositions);

        // Load obstacle image from resources
        try {
            InputStream stream = ObstaclePainter.class
                    .getClassLoader()
                    .getResourceAsStream("obstacle.png");

            if (stream != null) {
                myObstacleImage = ImageIO.read(stream);
            } else {
                System.out.println("obstacle.png not found in resources.");
            }
        } catch (IOException e) {
            System.out.println("Could not load obstacle image: " + e.getMessage());
        }
    }

    /**
     * Paints an obstacle icon at each stored geographic position on the map.
     * Translates the graphics context by the current viewport offset so that
     * icons remain anchored to their map coordinates as the user pans.
     * Draws the loaded PNG icon if available, otherwise draws a red X as a fallback.
     *
     * @param theGraphics - the Graphics2D context to paint onto
     * @param theMap - the JXMapViewer component providing coordinate conversion
     * @param theWidth - the width of the map component in pixels
     * @param theHeight - the height of the map component in pixels
     */
    @Override
    public void paint(final Graphics2D theGraphics,
                      final JXMapViewer theMap,
                      final int theWidth,
                      final int theHeight) {

        if (myObstaclePositions.isEmpty()) return;

        Graphics2D graphicsCopy = (Graphics2D) theGraphics.create();

        Rectangle rect = theMap.getViewportBounds();
        graphicsCopy.translate(-rect.x, -rect.y);

        graphicsCopy.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (GeoPosition position : myObstaclePositions) {
            Point2D point = theMap.getTileFactory()
                    .geoToPixel(position, theMap.getZoom());

            int x = (int) point.getX();
            int y = (int) point.getY();

            if (myObstacleImage != null) {
                // Draw image centered on the position
                graphicsCopy.drawImage(myObstacleImage,
                        x - ICON_SIZE / 2,
                        y - ICON_SIZE / 2,
                        ICON_SIZE, ICON_SIZE, null);
            } else {
                // Fallback — draw a red X if image not found
                graphicsCopy.setColor(Color.RED);
                graphicsCopy.setStroke(new BasicStroke(3));
                graphicsCopy.drawLine(x - 8, y - 8, x + 8, y + 8);
                graphicsCopy.drawLine(x + 8, y - 8, x - 8, y + 8);
            }
        }

        graphicsCopy.dispose();
    }
}
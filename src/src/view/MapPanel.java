package view;

import javax.swing.SwingUtilities;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Displays and manages the interactive map for the route planner.
 * Handles map tiles, zooming, panning, waypoint display, route painting,
 * obstacle painting, and map click events.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class MapPanel extends JPanel {

    /** The map viewer component used to display the OpenStreetMap tiles. */
    private final JXMapViewer myMapViewer;

    /** Painter used to draw the calculated route on the map. */
    private RoutePainter myRoutePainter;

    /** Painter used to draw obstacles on the map. */
    private ObstaclePainter myObstaclePainter;

    /** Painter used to draw route information on the map. */
    private RouteInfoPainter myRouteInfoPainter;

    /** Listener that receives geographic positions when the map is clicked. */
    private Consumer<GeoPosition> myMapClickListener;

    /** Painter used to draw start and end waypoint markers. */
    private final WaypointPainter<DefaultWaypoint> myWaypointPainter = new WaypointPainter<>();

    /** Set of waypoint markers currently displayed on the map. */
    private final Set<DefaultWaypoint> myWaypoints = new HashSet<>();

    /** The selected starting geographic position. */
    private GeoPosition myStartPoint;

    /** The selected ending geographic position. */
    private GeoPosition myEndPoint;

    /**
     * Constructs the map panel, initializes OpenStreetMap tiles,
     * sets the default map location, and enables map panning,
     * zooming, and click handling.
     */
    public MapPanel() {

        setLayout(new BorderLayout());
        myMapViewer = new JXMapViewer();

        TileFactoryInfo tileFactoryInfoinfo = new OSMTileFactoryInfo("OpenStreetMap",
                "https://tile.openstreetmap.org");

        DefaultTileFactory tileFactory = new DefaultTileFactory(tileFactoryInfoinfo);
        myMapViewer.setTileFactory(tileFactory);


        GeoPosition tacomaPosition = new GeoPosition(47.24947, -122.45750);


        setLayout(new BorderLayout());
        add(myMapViewer, BorderLayout.CENTER);

        myMapViewer.setAddressLocation(tacomaPosition);
        myMapViewer.setZoom(5);
        myMapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(myMapViewer));


        MouseInputListener panListener = new PanMouseInputListener(myMapViewer);
        myMapViewer.addMouseListener(panListener);
        myMapViewer.addMouseMotionListener(panListener);


        myMapViewer.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent theEvent) {

                if (SwingUtilities.isLeftMouseButton(theEvent)
                        && theEvent.getClickCount() == 1) {

                    GeoPosition clickedPosition =
                            myMapViewer.convertPointToGeoPosition(theEvent.getPoint());

                    System.out.println("Latitude: "
                            + clickedPosition.getLatitude());

                    System.out.println("Longitude: "
                            + clickedPosition.getLongitude());

                    if (myMapClickListener != null) {
                        myMapClickListener.accept(clickedPosition);
                    }
                }
            }
        });
    }

    /**
     * Sets the start and end waypoints displayed on the map.
     *
     * @param theStart the starting geographic position
     * @param theEnd the ending geographic position
     */
    public void setWaypoints(final GeoPosition theStart, final  GeoPosition theEnd) {
        myStartPoint = theStart;
        myEndPoint = theEnd;

        myWaypoints.clear();

        if (theStart != null) {
            myWaypoints.add(new DefaultWaypoint(theStart));
        }
        if (theEnd != null) {
            myWaypoints.add(new DefaultWaypoint(theEnd));
        }

        myWaypointPainter.setWaypoints(myWaypoints);
        updatePainters();
        myMapViewer.repaint();

    }

    /**
     * Clears all map overlays, including route, route information,
     * start point, end point, and waypoints.
     */
    public void clearAll() {
        myRoutePainter = null;
        myRouteInfoPainter = null;
        myStartPoint = null;
        myEndPoint = null;

        myWaypoints.clear();
        myWaypointPainter.setWaypoints(myWaypoints);
        updatePainters();
        myMapViewer.repaint();

    }

    /**
     * Sets the painter used to draw the calculated route.
     *
     * @param theRoutePainter the route painter to display
     */
    public void setRoutePainter(final RoutePainter theRoutePainter) {
        myRoutePainter = theRoutePainter;
        updatePainters();
    }

    /**
     * Combines all active map painters and refreshes the map display.
     */
    public void updatePainters() {
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
        compoundPainter.addPainter(myWaypointPainter);
        if (myRoutePainter != null) {
            compoundPainter.addPainter(myRoutePainter);
        }
        if (myObstaclePainter != null) {
            compoundPainter.addPainter(myObstaclePainter);
        }
        if (myRouteInfoPainter != null) {
            compoundPainter.addPainter(myRouteInfoPainter);
        }

        myMapViewer.setOverlayPainter(compoundPainter);
        myMapViewer.repaint();
    }

    /**
     * Sets the painter used to draw obstacles on the map.
     *
     * @param thePainter the obstacle painter to display
     */
    public void setObstaclePainter(final ObstaclePainter thePainter) {
        myObstaclePainter = thePainter;
        updatePainters();
    }

    /**
     * Returns the map viewer component.
     *
     * @return the map viewer
     */
    public JXMapViewer getMapViewer() {
        return myMapViewer;
    }


    /**
     * Registers a listener to handle map click events.
     *
     * @param theListener the listener that receives clicked geographic positions
     */
    public void setMapClickListener ( final Consumer<GeoPosition> theListener){
        myMapClickListener = theListener;
    }

    /**
     * Sets the painter used to display route information on the map.
     *
     * @param thePainter the route information painter to display
     */
    public void setRouteInfoPainter(final RouteInfoPainter thePainter) {
        myRouteInfoPainter = thePainter;
        updatePainters();
    }

    /**
     * Returns the starting geographic position.
     *
     * @return the start point of the route
     */
    public GeoPosition getStartPoint() {
        return myStartPoint;
    }

    /**
     * Sets the starting geographic position.
     *
     * @param theStartPoint the geographic position to use as the route start point
     */
    public void setStartPoint(GeoPosition theStartPoint) {
        myStartPoint = theStartPoint;
    }

    /**
     * Returns the ending geographic position.
     *
     * @return the end point of the route
     */
    public GeoPosition getEndPoint() {
        return myEndPoint;
    }

    /**
     * Sets the ending geographic position.
     *
     * @param theEndPoint the geographic position to use as the route end point
     */

    public void setEndPoint(GeoPosition theEndPoint) {
        myEndPoint = theEndPoint;
    }

}


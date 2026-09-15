package model;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Parses an OpenStreetMap (.osm) XML file and populates a MapGraph
 * with intersections and roads. Uses a SAX-based streaming parser for
 * memory-efficient handling of large map files. Only nodes that appear in
 * at least one road way are added to the graph as intersections, avoiding
 * unnecessary object allocation for unreferenced map features.
 * Road speed limits are read from "maxspeed" tags when present, and
 * fall back to defaults based on the OSM highway classification otherwise.
 *
 * @author Sofiia Kabaldina
 * @version Spring 2026
 */
public class MapLoader {

    /**
     * Loads intersections and roads from the given OSM resource file into theGraph.
     * The file is resolved from the classpath using the class loader.
     *
     * @param theGraph - the MapGraph to populate with parsed intersections and roads
     * @param theFilename - the classpath-relative path to the .osm XML file
     * @throws FileNotFoundException if the resource cannot be found on the classpath
     * @throws Exception if the SAX parser encounters a malformed XML document
     */
    public static void loadFromFile(final MapGraph theGraph, final String theFilename) throws Exception {
        InputStream fileStream = MapLoader.class
                .getClassLoader()
                .getResourceAsStream(theFilename);

        if (fileStream == null) {
            throw new FileNotFoundException("Could not find: " + theFilename);
        }

        // Buffer the stream for faster I/O
        InputStream buffered = new BufferedInputStream(fileStream, 1 << 16); // 64KB buffer

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        OsmHandler handler = new OsmHandler();
        parser.parse(buffered, handler);

        // Only add intersections that are actually used by roads
        for (String id : handler.myRoadNodeIds) {
            Intersection intersection = handler.myNodeMap.get(id);
            if (intersection != null) {
                theGraph.addIntersection(intersection);
            }
        }

        for (Road road : handler.myRoads) {
            theGraph.addRoad(road);
        }

        System.out.println("Loaded " + handler.myRoadNodeIds.size()
                + " road intersections from " + theFilename);
    }

    // ── SAX event handler ────────────────────────────────────────────────────

    /**
     * SAX event handler that processes OSM XML node and way elements to build
     * the raw data structures later used to populate the graph.
     * Intersection objects are created lazily — only when a node ID is first
     * referenced by a road way — to avoid allocating objects for the many OSM
     * nodes that represent non-road features such as buildings or POIs.
     */
    private static class OsmHandler extends DefaultHandler {

        // Lean node storage: only id → lat/lon/label (no full objects yet)
        final Map<String, double[]> myRawNodes = new HashMap<>(1 << 20); // id → [lat, lon]
        final Map<String, String> myNodeNames = new HashMap<>();         // id → name tag
        final Map<String, Intersection> myNodeMap = new HashMap<>();
        final Set<String> myRoadNodeIds = new HashSet<>();
        final List<Road> myRoads = new ArrayList<>();

        // State while parsing the current element
        private String myCurNodeId;
        private double myCurLat, myCurLon;
        private boolean myInNode, myInWay, myIsRoad;
        private boolean myIsOneWay;
        private int mySpeedLimit;
        private String myRoadType;
        private final List<String> myCurWayNodes = new ArrayList<>();


        /**
         * Handles the opening tag of an OSM XML element.
         * Processes {@code node}, {@code way}, {@code nd}, and {@code tag} elements,
         * recording coordinates, names, highway classification, one-way status,
         * and speed limits as they are encountered.
         *
         * @param uri - the namespace URI
         * @param localName - the local name without prefix
         * @param qName - the qualified element name (e.g. "node", "way", "tag")
         * @param attrs - the attributes attached to the opening element
         */
        @Override
        public void startElement(final String uri, final String localName,
                                 final String qName, final Attributes attrs) {
            switch (qName) {

                case "node":
                    myInNode = true;
                    myCurNodeId = attrs.getValue("id");
                    myCurLat = Double.parseDouble(attrs.getValue("lat"));
                    myCurLon = Double.parseDouble(attrs.getValue("lon"));
                    // Store raw coords cheaply — skip building Intersection objects for
                    // nodes that will never appear in a road way
                    myRawNodes.put(myCurNodeId, new double[]{myCurLat, myCurLon});
                    break;

                case "way":
                    myInWay = true;
                    myIsRoad = false;
                    myIsOneWay = false;
                    mySpeedLimit = -1;   // -1 = not yet known
                    myRoadType = null;
                    myCurWayNodes.clear();
                    break;

                case "nd":
                    if (myInWay) {
                        myCurWayNodes.add(attrs.getValue("ref"));
                    }
                    break;

                case "tag":
                    String k = attrs.getValue("k");
                    String v = attrs.getValue("v");

                    if (myInNode && "name".equals(k)) {
                        myNodeNames.put(myCurNodeId, v);
                    }

                    if (myInWay) {
                        switch (k) {
                            case "highway":
                                myRoadType = v;
                                myIsRoad = true;
                                break;
                            case "oneway":
                                myIsOneWay = "yes".equals(v);
                                break;
                            case "maxspeed":
                                try {
                                    mySpeedLimit = Integer.parseInt(v.replaceAll("[^0-9]", ""));
                                } catch (NumberFormatException ignored) {}
                                break;
                        }
                    }
                    break;
            }
        }

        /**
         * Handles the closing tag of an OSM XML element.
         * On {@code </node>}, clears the in-node flag.
         * On {@code </way>}, constructs {@link Road} objects for each consecutive
         * pair of node references in the way, applying a default speed if no
         * {@code maxspeed} tag was present. Bidirectional roads are added in both
         * directions unless the way is tagged as one-way.
         *
         * @param uri - the namespace URI (unused for OSM)
         * @param localName - the local name without prefix (unused for OSM)
         * @param qName - the qualified element name (e.g. "node", "way")
         */
        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            switch (qName) {
                case "node":
                    myInNode = false;
                    break;

                case "way":
                    myInWay = false;
                    if (!myIsRoad || myCurWayNodes.size() < 2) break;

                    int speed = (mySpeedLimit > 0)
                            ? mySpeedLimit
                            : getDefaultSpeed(myRoadType);

                    for (int i = 0; i < myCurWayNodes.size() - 1; i++) {
                        String startId = myCurWayNodes.get(i);
                        String endId = myCurWayNodes.get(i + 1);

                        double[] startCoords = myRawNodes.get(startId);
                        double[] endCoords = myRawNodes.get(endId);
                        if (startCoords == null || endCoords == null) continue;

                        // Build Intersection objects lazily — only when first needed
                        Intersection start = myNodeMap.computeIfAbsent(startId, id -> {
                            String label = myNodeNames.getOrDefault(id, id);
                            return new Intersection(id, startCoords[0], startCoords[1], label);
                        });
                        Intersection end = myNodeMap.computeIfAbsent(endId, id -> {
                            String label = myNodeNames.getOrDefault(id, id);
                            return new Intersection(id, endCoords[0], endCoords[1], label);
                        });

                        myRoadNodeIds.add(startId);
                        myRoadNodeIds.add(endId);

                        double dist = calculateDistanceMiles(
                                startCoords[0], startCoords[1],
                                endCoords[0],   endCoords[1]);

                        myRoads.add(new Road(start, end, dist, speed));
                        if (!myIsOneWay) {
                            myRoads.add(new Road(end, start, dist, speed));
                        }
                    }
                    break;
            }
        }
    }

    // ── Helpers (unchanged logic, same as before) ────────────────────────────

    /**
     * Returns a default speed limit in mph for the given OSM highway type.
     * Used when a road way does not include a "maxspeed" tag.
     *
     * @param theRoadType - the OSM highway classification string (e.g. "residential")
     * @return the default speed limit in mph for that road type, or 30 if unknown
     */
    private static int getDefaultSpeed(final String theRoadType) {
        if (theRoadType == null) return 30;
        switch (theRoadType) {
            case "trunk":
                return 65;
            case "primary":
                return 45;
            case "secondary":
                return 35;
            case "residential":
                return 25;
            case "service":
                return 15;
            default:
                return 30;
        }
    }

    /**
     * Calculates the great-circle distance in miles between two geographic coordinates.
     *
     * @param lat1 - the latitude of the first point in decimal degrees
     * @param lon1 - the longitude of the first point in decimal degrees
     * @param lat2 - the latitude of the second point in decimal degrees
     * @param lon2 - the longitude of the second point in decimal degrees
     * @return the distance between the two points in miles
     */
    private static double calculateDistanceMiles(final double lat1, final double lon1,
                                                 final double lat2, final double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        final double KM_TO_MILES = 0.621371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double distKm = EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return distKm * KM_TO_MILES;
    }
}
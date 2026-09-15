package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the SQLite database used to persist and retrieve route records.
 * Opens (or creates) the database on construction, ensures the {@code routes}
 * table exists, and exposes methods to save and load {@link RouteRecord} objects.
 * Fires a {@code "saveRoute"} property-change event after a successful save.
 *
 * @author Abby Hinds, Sofiia Kabaldina
 * @version Spring 2026
 */
public class DatabaseManager extends AbstractModel {

    //Connection is a JDBC object that keeps the connection to SQLite
    private Connection myConnection;

    /** JDBC URL pointing to the SQLite database file. */
    private String myDatabaseURL;

    /**
     * Opens (or creates) the default {@code myRoutes.db} SQLite database and
     * ensures the {@code routes} table exists.
     *
     * @throws SQLException if a database access error occurs
     */
    public DatabaseManager() throws SQLException {
        try {
            //loads JDBC driver into memory
            Class.forName("org.sqlite.JDBC");
            myDatabaseURL = "jdbc:sqlite:myRoutes.db";
            //opens database if it exists or makes a new one named myRoutes
            myConnection = DriverManager.getConnection(myDatabaseURL);

            //create table if it doesn't exist
            try (var stmt = myConnection.createStatement()) {
                stmt.execute(createTable());
            }
        } catch (ClassNotFoundException e) {
            //exception if driver is not installed
            e.printStackTrace();
        }
    }

    /**
     * Opens the SQLite database at the given URL and ensures the {@code routes} table exists.
     * Intended for testing so that tests run against an isolated database instead of
     * the production {@code myRoutes.db} file.
     *
     * @param theURL the JDBC URL of the test database (e.g. {@code "jdbc:sqlite::memory:"})
     * @throws SQLException if the JDBC driver is not found or a database access error occurs
     */
    public DatabaseManager(final String theURL) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        myDatabaseURL = theURL;
        myConnection = DriverManager.getConnection(myDatabaseURL);
        try (var stmt = myConnection.createStatement()) {
            stmt.execute(createTable());
        }
    }

    /**
     * Returns the active JDBC connection, primarily used for running queries in tests.
     *
     * @return the current {@link Connection} to the SQLite database
     */
    public Connection getConnection() {
        return myConnection;
    }

    /**
     * Builds the SQL statement that creates the {@code routes} table if it does not
     * already exist, with columns for ID, Start, End, Distance, Duration, and Hazards.
     *
     * @return the {@code CREATE TABLE IF NOT EXISTS} SQL string
     */
    private static String createTable() {
        //SQL statement for creating a new table
        var sql = "CREATE TABLE IF NOT EXISTS routes ("
                + "	ID INTEGER PRIMARY KEY,"
                + "	Start VARCHAR(20),"
                + "	End VARCHAR(20), "
                + " Distance DECIMAL, "
                + " Duration TIME, "
                + " Hazards TEXT "
                + ");";
        return sql;
    }

    /**
     * Persists a route record to the database and fires a {@code "saveRoute"}
     * property-change event on success.
     *
     * @param theRoute the {@link RouteRecord} to insert into the {@code routes} table
     */
    public void saveRoute(final RouteRecord theRoute) {

        //SQL statement
        var sql = "INSERT INTO routes (ID, Start, End, Distance, Duration, Hazards) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        //sends statement to database
        try (var statement = myConnection.prepareStatement(sql);) {

            //fills in the placeholders
            statement.setInt(1, theRoute.getMyID());
            statement.setString(2, theRoute.getMyStartLabel());
            statement.setString(3, theRoute.getMyEndLabel());
            statement.setDouble(4, theRoute.getMyDistance());
            statement.setDouble(5, theRoute.getMyDuration());
            statement.setString(6, theRoute.getMyHazards().toString());
            //executes statement
            statement.executeUpdate();

            myChanges.firePropertyChange("saveRoute", null, theRoute);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves all route records stored in the database.
     * The hazard strings are parsed from their stored list representation back into
     * individual hazard entries.
     *
     * @return a {@link List} of all {@link RouteRecord} objects in the {@code routes} table,
     *         or an empty list if the table is empty or a database error occurs
     */
    public List<RouteRecord> loadAllRoutes() {
        //SELECT * FROM routes;
        var sql = "SELECT * FROM routes;";
        List<RouteRecord> routes = new ArrayList<>();

        try (var statement = myConnection.createStatement();
             //result set
             var rs = statement.executeQuery(sql)) {

            //goes through each line in the table
            while (rs.next()) {

                String unformatted = rs.getString("Hazards");
                List<String> hazards = new ArrayList<>();

                if (unformatted != null && !unformatted.isEmpty()) {
                    String formatted = unformatted.replace("[", "").replace("]", "").trim();
                    if (!formatted.isEmpty()) {
                        for (String h : formatted.split(",")) {
                            hazards.add(h.trim());
                        }
                    }
                }

                routes.add(new RouteRecord(
                        rs.getInt("ID"),
                        rs.getString("Start"),
                        rs.getString("End"),
                        rs.getDouble("Distance"),
                        rs.getDouble("Duration"),
                        hazards
                ));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return routes;
    }

    /**
     * Deletes the route record with the given ID from the {@code routes} table.
     * Does nothing if no record with that ID exists.
     *
     * @param theId the unique ID of the route record to delete
     */
    public void deleteRoute(final int theId) {
        var sql = "DELETE FROM routes WHERE ID = ?";
        try (var statement = myConnection.prepareStatement(sql)) {
            statement.setInt(1, theId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returns the next available route ID by querying the current maximum ID in the
     * {@code routes} table and incrementing it by one. Returns {@code 1} if the table
     * is empty or a database error occurs.
     *
     * @return the next integer ID to use when inserting a new route record
     */
    public int getNextId() {

        //selects max id from database and sets the next route to id + 1
        var sql = "SELECT MAX(ID) FROM routes;";

        try (var statement = myConnection.createStatement();

             var rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 1;
    }
}
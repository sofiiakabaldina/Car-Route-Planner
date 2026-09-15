package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


class DatabaseManagerTest {

    private DatabaseManager dbm;
    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {

        //database info doesn't need to persist so just putting it in memory
        dbm = new DatabaseManager("jdbc:sqlite::memory:");
        conn = dbm.getConnection();

    }

    @Test
    void loadAllRoutesEmpty() throws SQLException {
        assertTrue(dbm.loadAllRoutes().isEmpty());
    }

    //also tests loadAllRoutes
    @Test
    void saveRoute() throws SQLException {

        dbm.saveRoute(new RouteRecord(1, "1", "2", 1.0, 1.0, List.of()));
        dbm.saveRoute(new RouteRecord(2, "2", "3", 1.0, 1.0, List.of()));

        List<RouteRecord> routes = dbm.loadAllRoutes();

        assertEquals(2, routes.size());
        assertEquals("1", routes.get(0).getMyStartLabel());
        assertEquals("2", routes.get(1).getMyStartLabel());

    }

    @Test
    void saveRouteThenLoadRoute() throws SQLException {

        dbm.saveRoute(new RouteRecord(7, "1", "2", 3.5, 2.0, List.of()));

        RouteRecord r = dbm.loadAllRoutes().get(0);

        assertEquals(7, r.getMyID());
        assertEquals("1", r.getMyStartLabel());
        assertEquals("2", r.getMyEndLabel());
        assertEquals(3.5, r.getMyDistance());
        assertEquals(2.0, r.getMyDuration());
        assertTrue(r.getMyHazards().isEmpty());
    }

    //make sure routes are mapped correctly to database
    @Test
    void saveRouteWithHazards() throws SQLException {

        dbm.saveRoute(new RouteRecord(1, "A", "B", 5.0, 10.0, List.of("ice", "fog")));
        RouteRecord r = dbm.loadAllRoutes().get(0);

        assertEquals(List.of("ice", "fog"), r.getMyHazards());
    }

    @Test
    void getNextIdEmptyDatabase() throws SQLException {

        assertEquals(1, dbm.getNextId());

    }

    @Test
    void getNextIdAfterInserts() throws SQLException {

        dbm.saveRoute(new RouteRecord(3, "1", "2", 1.0, 1.0, List.of()));
        dbm.saveRoute(new RouteRecord(7, "3", "4", 1.0, 1.0, List.of()));

        assertEquals(8, dbm.getNextId());
    }

}
package it.progetto.progetto18.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import it.progetto.progetto18.model.*;

import java.sql.SQLException;

public class DatabaseManager {

    private ConnectionSource connectionSource;

    private Dao<StopEntity, String> stopDao;
    private Dao<RouteEntity, String> routeDao;
    private Dao<TripEntity, String> tripDao;
    private Dao<StopTimeEntity, String> stopTimeDao;

    public DatabaseManager() throws SQLException {
    	// Aggiungi parametri di timeout alla connessione
        String dbUrl = "jdbc:sqlite:gtfs.db?busy_timeout=30000&journal_mode=WAL";
    	connectionSource = new JdbcConnectionSource("jdbc:sqlite:gtfs.db");

        stopDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, StopEntity.class);
        routeDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, RouteEntity.class);
        tripDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, TripEntity.class);
        stopTimeDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, StopTimeEntity.class);

        // Crea tabelle se non esistono
        TableUtils.createTableIfNotExists(connectionSource, StopEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, RouteEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, TripEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, StopTimeEntity.class);
    }

    public Dao<StopEntity, String> getStopDao() { return stopDao; }
    public Dao<RouteEntity, String> getRouteDao() { return routeDao; }
    public Dao<TripEntity, String> getTripDao() { return tripDao; }
    public Dao<StopTimeEntity, String> getStopTimeDao() { return stopTimeDao; }

    public void close() {
        try { connectionSource.close(); } catch (Exception e) { e.printStackTrace(); }
    }
}

package it.progetto.progetto18.manager;

import it.progetto.progetto18.core.*;
import it.progetto.progetto18.app.*;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;
import it.progetto.progetto18.model.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GTFSDataManager {
    private DatabaseManager dbManager;
    private List<StopEntity> stopEntities = new ArrayList<>();
    private List<RouteEntity> routeEntities = new ArrayList<>();
    private GtfsDaoImpl store;
    
    // ---------------- Database ----------------
    public void closeDb() {
        if (dbManager != null) dbManager.close();
    }
    
    public void initDb() throws Exception {
        dbManager = new DatabaseManager();
    }
    
    public DatabaseManager getDbManager() {
        return dbManager;
    }
    
    // ---------------- GTFS parsing ----------------
    public void parseGtfs(String filePath) throws Exception {
        store = new GtfsDaoImpl();
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(new File(filePath));
        reader.setEntityStore(store);
        reader.run();
    }
    
    public List<Stop> getAllStopsRaw() {
        return new ArrayList<>(store.getAllStops());
    }
    public List<Route> getAllRoutesRaw() {
        return new ArrayList<>(store.getAllRoutes());
    }
    public List<Trip> getAllTripsRaw() {
        return new ArrayList<>(store.getAllTrips());
    }
    public List<StopTime> getAllStopTimesRaw() {
        return new ArrayList<>(store.getAllStopTimes());
    }
    
    
    // ---------------- GTFS Load ----------------

    public void loadGtfsDataFast(String filePath) throws Exception {
    	this.dbManager = new DatabaseManager();
    	
    	File dbFile = new File("gtfs.db");
        // Se DB esiste già, esci
        if (dbFile.exists() && !dbManager.getStopDao().queryForAll().isEmpty()) {
            System.out.println("DB già popolato, skip GTFS import");
            return;
        }
        
        
        System.out.println("Parsing GTFS...");
        GtfsDaoImpl store = new GtfsDaoImpl();
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(new File(filePath));
        reader.setEntityStore(store);
        reader.run();
        System.out.println("Inserimento fermate...");
        dbManager.getStopDao().callBatchTasks(() -> {
        	for (Stop stop : store.getAllStops()) {
        	    StopEntity stopEntity = new StopEntity(
        	        "ATAC", // oppure ricava agencyId da agency o dal feed
        	        stop.getId().getId(),                     // stop_id
        	        stop.getCode(),                           // stop_code
        	        stop.getName(),                           // stop_name
        	        stop.getDesc(),                           // stop_desc
        	        stop.getLat(),                            // stop_lat
        	        stop.getLon(),                            // stop_lon
        	        stop.getUrl(),                            // stop_url
        	        stop.getWheelchairBoarding(),             // wheelchair_boarding (int)
        	        stop.getTimezone(),                       // stop_timezone
        	        stop.getLocationType(),                   // location_type (int)
        	        stop.getParentStation()                   // parent_station
        	    );
        	    dbManager.getStopDao().create(stopEntity);
        	}
            return null;
        });
        System.out.println("Inserimento linee...");
        dbManager.getRouteDao().callBatchTasks(() -> {
            for (Route route : store.getAllRoutes()) {
                RouteEntity entity = new RouteEntity(
                    route.getId().getAgencyId(),
                    route.getId().getId(),
                    route.getShortName(),
                    route.getLongName(),
                    String.valueOf(route.getType()),
                    route.getUrl(),
                    route.getColor(),
                    route.getTextColor()
                );
                dbManager.getRouteDao().create(entity);
            }
            return null;
        });

        System.out.println("Inserimento viaggi...");
        dbManager.getTripDao().callBatchTasks(() -> {
            for (Trip trip : store.getAllTrips()) {
                String agencyId = trip.getId().getAgencyId();
                String tripId = trip.getId().getId();
                String routeId = trip.getRoute().getId().getId();
                
                // Conversione AgencyAndId -> String
                String serviceId = null;
                if (trip.getServiceId() != null) {
                    serviceId = trip.getServiceId().getId();
                }
                
                String routeKey = agencyId + ":" + routeId; // chiave primaria routeKey

                TripEntity entity = new TripEntity(
                    agencyId,
                    tripId,
                    routeId,
                    serviceId,
                    routeKey
                );

                dbManager.getTripDao().create(entity);
            }
            return null;
        });


        System.out.println("Inserimento stop_times...");
        dbManager.getStopTimeDao().callBatchTasks(() -> {
            for (StopTime st : store.getAllStopTimes()) {
                // costruisci la tripKey completa
                String tripKey = st.getTrip().getId().getAgencyId() + ":" + st.getTrip().getId().getId();
                StopTimeEntity entity = new StopTimeEntity(
                	st.getTrip().getId().getAgencyId(),
                    st.getTrip().getId().getId(),   // tripId puro
                    st.getStop().getId().getId(),   // stopId
                    st.getArrivalTime(),            // in secondi
                    st.getDepartureTime(),
                    st.getStopSequence()
                    //tripKey                        // chiave completa del trip
                );
                dbManager.getStopTimeDao().create(entity);
            }
            return null;
        });
        System.out.println("Importazione GTFS completata!");
    }
    
    
    // ---------------- Batch Insert ----------------
    public void insertStopsToDbBatch(List<Stop> stops, DatabaseManager dbManager) throws Exception {
        dbManager.getStopDao().callBatchTasks(() -> {
            for (Stop stop : stops) {
            	System.out.println("Inserisco fermata " + stop.getName());
                StopEntity entity = new StopEntity(
                		stop.getId().getAgencyId(),
                        stop.getId().getId(),           // stop_id
                        stop.getCode(),                 // stop_code
                        stop.getName(),                 // stop_name
                        stop.getDesc(),                 // stop_desc
                        stop.getLat(),
                        stop.getLon(),
                        stop.getUrl(),
                        stop.getWheelchairBoarding(),
                        stop.getTimezone(),
                        stop.getLocationType(),
                        stop.getParentStation()
                );
                dbManager.getStopDao().createIfNotExists(entity);
            }
            return null;
        });
    }
    public void insertRoutesToDbBatch(List<Route> routes, DatabaseManager dbManager) throws Exception {
        dbManager.getRouteDao().callBatchTasks(() -> {
            for (Route route : routes) {
                String routeKey = route.getId().getAgencyId() + ":" + route.getId().getId();
                System.out.println("DEBUG Inserisco route: " + route.getLongName() + " con routeKey=" + routeKey);
                RouteEntity entity = new RouteEntity(
                        route.getId().getAgencyId(),
                        route.getId().getId(),
                        route.getShortName(),
                        route.getLongName(),
                        String.valueOf(route.getType()),
                        route.getUrl(),
                        route.getColor(),
                        route.getTextColor()
                    );
                dbManager.getRouteDao().createIfNotExists(entity);
                // Dopo inserimento, stampiamo anche la key dal DB per confermare
                RouteEntity fromDb = dbManager.getRouteDao().queryForId(entity.getKey());
                if (fromDb != null) {
                    System.out.println("DEBUG Route confermata nel DB: " + fromDb.getKey());
                }
            }
            return null;
        });
    }
    public void insertTripsToDbBatch(List<Trip> trips, DatabaseManager dbManager) throws Exception {
        dbManager.getTripDao().callBatchTasks(() -> {
            for (Trip trip : trips) {
                // Prende la route a cui appartiene il trip
                Route route = trip.getRoute(); 
                String agencyId = trip.getId().getAgencyId();
                String tripId = trip.getId().getId();
                String routeId = route.getId().getId();

                // Conversione serviceId da AgencyAndId a String
                String serviceId = null;
                if (trip.getServiceId() != null) {
                    serviceId = trip.getServiceId().getId();
                }

                String routeKey = agencyId + ":" + routeId;

                System.out.println("DEBUG Inserisco trip " + tripId + " con routeKey=" + routeKey);

                // Crea l'entity con i campi obbligatori
                TripEntity entity = new TripEntity(
                    agencyId,
                    tripId,
                    routeId,
                    serviceId,
                    routeKey
                );

                // Imposta campi opzionali con i tipi corretti
                entity.setTripHeadsign(trip.getTripHeadsign());
                entity.setTripShortName(trip.getTripShortName());

                // directionId è Integer, trip.getDirectionId() è String -> parse
                if (trip.getDirectionId() != null) {
                    try {
                        entity.setDirectionId(Integer.parseInt(trip.getDirectionId()));
                    } catch (NumberFormatException e) {
                        System.out.println("WARN: directionId non valido per trip " + tripId);
                    }
                }

                entity.setBlockId(trip.getBlockId());

                // shapeId è String, trip.getShapeId() è AgencyAndId
                if (trip.getShapeId() != null) {
                    entity.setShapeId(trip.getShapeId().getId());
                }

                entity.setWheelchairAccessible(trip.getWheelchairAccessible());
                // eventuale campo exceptional, se presente, altrimenti null

                // Inserimento batch
                dbManager.getTripDao().createIfNotExists(entity);

                // DEBUG: verifica inserimento
                TripEntity fromDb = dbManager.getTripDao().queryForId(entity.getKey());
                if (fromDb != null) {
                    System.out.println("DEBUG Trip confermato nel DB: " + fromDb.getKey() +
                            " -> routeKey=" + fromDb.getRouteKey());
                }
            }
            return null;
        });
    }
    public void insertStopTimesToDbBatch(List<StopTime> stopTimes, DatabaseManager dbManager) throws Exception {
        int batchSize = 5000;
        for (int i = 0; i < stopTimes.size(); i += batchSize) {
            int end = Math.min(stopTimes.size(), i + batchSize);
            List<StopTime> subList = stopTimes.subList(i, end);
            dbManager.getStopTimeDao().callBatchTasks(() -> {
            	for (StopTime st : subList) {
                    String tripKey = st.getTrip().getId().getAgencyId() + ":" + st.getTrip().getId().getId();
                    System.out.println("DEBUG Inserisco Stoptime tripKey=" + tripKey + ", stopId=" + st.getStop().getId().getId());
                    StopTimeEntity entity = new StopTimeEntity(
                        st.getTrip().getId().getAgencyId(),
                    	st.getTrip().getId().getId(),       // tripId puro
                        st.getStop().getId().getId(),       // stopId
                        st.getArrivalTime(),
                        st.getDepartureTime(),
                        st.getStopSequence()
                        //tripKey                            // chiave completa del trip
                    );
                    dbManager.getStopTimeDao().createIfNotExists(entity);
                }
                
                return null;
            });
        }
    }

    
    // ---------------- Get Entities ----------------

    public List<StopEntity> getAllStops() throws Exception {
        if (stopEntities.isEmpty()) {
            stopEntities = dbManager.getStopDao().queryForAll();
        }
        return stopEntities;
    }
    public List<TripEntity> getAllTrips() throws Exception {
        return dbManager.getTripDao().queryForAll();
    }
    public List<StopTimeEntity> getAllStopTimes() throws Exception {
        return dbManager.getStopTimeDao().queryForAll();
    }
    public List<TripEntity> getTripsForStop(String stopId) throws Exception {
        List<StopTimeEntity> stopTimes = dbManager.getStopTimeDao().queryForEq("stop_id", stopId);
        Set<String> tripIds = new HashSet<>();
        for (StopTimeEntity stopTime : stopTimes) {
            tripIds.add(stopTime.getTripId());
        }
        List<TripEntity> trips = new ArrayList<>();
        for (String tripId : tripIds) {
            TripEntity trip = dbManager.getTripDao().queryForId(tripId);
            if (trip != null) {
                trips.add(trip);
            }
        }
        return trips;
    }
    public List<RouteEntity> getAllRoutes() throws Exception {
        if (routeEntities.isEmpty()) {
            routeEntities = dbManager.getRouteDao().queryForAll();
        }
        return routeEntities;
    }
    public List<RouteEntity> getRoutesForStop(String stopId) throws SQLException {
        // Set per evitare duplicati
        Set<String> routeKeys = new HashSet<>();
        List<RouteEntity> routes = new ArrayList<>();
        // 1. Recupera tutti gli StopTime che hanno questo stopId
        List<StopTimeEntity> stopTimes = dbManager.getStopTimeDao()
                .queryBuilder()
                .where().eq("stopId", stopId)
                .query();
        System.out.println("DEBUG StopTimes trovati per fermata " + stopId + ": " + stopTimes.size());
        // 2. Per ogni StopTime, risali al Trip e poi alla Route
        for (StopTimeEntity st : stopTimes) {
            TripEntity trip = dbManager.getTripDao().queryForId(st.getAgencyId() + ":" + st.getTripId());

        	//TripEntity trip = dbManager.getTripDao().queryForId(st.getTripKey());
            if (trip != null) {
                String routeKey = trip.getRouteKey();
                if (!routeKeys.contains(routeKey)) {
                    RouteEntity route = dbManager.getRouteDao().queryForId(routeKey);
                    if (route != null) {
                        routes.add(route);
                        routeKeys.add(routeKey);
                        System.out.println("DEBUG Route aggiunta: " + route.getShortName() + " (" + route.getKey() + ")");
                    } else {
                        System.out.println("DEBUG Route non trovata per routeKey=" + routeKey);
                    }
                }
            } else {
                System.out.println("DEBUG Trip non trovato per tripKey=" + st.getAgencyId() + ":" + st.getTripId());
            }
        }
        // 3. Ordina le route per shortName
        routes.sort(Comparator.comparing(RouteEntity::getShortName));
        System.out.println("DEBUG Totale route uniche trovate per fermata " + stopId + ": " + routes.size());
        return routes;
    }
    

    // ---------------- Arrivi e Trip Attivi ----------------

    /**
     * Restituisce le prossime linee (route) che passano per una fermata e i relativi orari.
     * Ordina i risultati per orario di arrivo.
     */
    /*public List<StopArrival> getNextArrivalsForStop(String stopId) throws SQLException {
    	List<StopArrival> arrivals = new ArrayList<>();
    	
    	//prendo orario corente in secondi dalla mezzanotte
    	LocalTime now = LocalTime.now();
    	int nowInSeconds = now.toSecondOfDay();
    	
        // 1. Recupera tutti gli StopTime della fermata, ordinati per arrivalTime crescente
        List<StopTimeEntity> stopTimes = dbManager.getStopTimeDao()
                .queryBuilder()
                .orderBy("arrivalTime", true)
                .where().eq("stopId", stopId)      
                .query();
        System.out.println("DEBUG StopTimes trovati per fermata " + stopId + ": " + stopTimes.size());
        // 2. Per ogni StopTime, risali al Trip e quindi alla Route
     // 2. Per ogni StopTime, risali al Trip e quindi alla Route
        for (StopTimeEntity st : stopTimes) {
            int arrival = st.getArrivalTime();
            boolean isFutureToday = (arrival < 86400 && arrival >= nowInSeconds);
            boolean isFutureNextDays = (arrival >= 86400); // GTFS permette >24h
            if (isFutureToday || isFutureNextDays) {
                TripEntity trip = dbManager.getTripDao().queryForId(st.getAgencyId() + ":" + st.getTripId());
                if (trip == null) continue;
                RouteEntity route = dbManager.getRouteDao().queryForId(trip.getRouteKey());
                if (route == null) continue;
                arrivals.add(new StopArrival(route, arrival, trip.getTripId(), trip.getTripHeadsign()));

            }
        }
        System.out.println("DEBUG Totale arrivi calcolati per fermata " + stopId + ": " + arrivals.size());
        return arrivals;
    }
    */
 
    public List<StopArrival> getNextArrivalsForStop(String stopId) throws SQLException {
        List<StopArrival> arrivals = new ArrayList<>();
        
        LocalTime now = LocalTime.now();
        int nowInSeconds = now.toSecondOfDay();
        
        try {
            // Query ottimizzata con LIMIT per evitare di processare troppi record
            List<StopTimeEntity> stopTimes = dbManager.getStopTimeDao()
                    .queryBuilder()
                    .limit(100L) // Limita i risultati per performance
                    .orderBy("arrivalTime", true)
                    .where().eq("stopId", stopId)
                    .and().ge("arrivalTime", nowInSeconds)
                    .query();
            
            System.out.println("DEBUG: Query completata, " + stopTimes.size() + " risultati");
            
            for (StopTimeEntity st : stopTimes) {
                try {
                    String tripKey = st.getAgencyId() + ":" + st.getTripId();
                    TripEntity trip = dbManager.getTripDao().queryForId(tripKey);
                    
                    if (trip == null) continue;
                    
                    RouteEntity route = dbManager.getRouteDao().queryForId(trip.getRouteKey());
                    if (route == null) continue;
                    
                    arrivals.add(new StopArrival(route, st.getArrivalTime(), 
                        trip.getTripId(), trip.getTripHeadsign()));
                    
                    // Ferma dopo 10 risultati per evitare sovraccarico
                    if (arrivals.size() >= 10) break;
                    
                } catch (Exception e) {
                    System.err.println("Errore processando stop time: " + e.getMessage());
                    // Continua con il prossimo invece di fermarsi
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore query database: " + e.getMessage());
            throw e;
        }
        
        System.out.println("DEBUG: Totale arrivi calcolati: " + arrivals.size());
        return arrivals;
    }

    
    
    //metodo che restituisce per ogni trip della linea la fermata attuale
    /**
     * Restituisce i trip attivi per una linea e la loro prossima fermata,
     * considerando l'orario attuale.
     */
    
    public List<String> getActiveTripsForLine(String lineShortName) throws SQLException {
        List<String> result = new ArrayList<>();
        int nowInSeconds = LocalTime.now().toSecondOfDay();
        
        // OTTIMIZZAZIONI PRINCIPALI:
        int maxTime = nowInSeconds + 1800; // 30 min invece di 45 (meno dati)
        final int MAX_RESULTS = 5;         // Risultati limitati
        final int MAX_TRIPS = 15;          // Trip limitati
        
        try {
            List<RouteEntity> routes = dbManager.getRouteDao()
                .queryBuilder()
                .limit(1L) // ← DRASTICA RIDUZIONE
                .where().eq("shortName", lineShortName)
                .query();
                
            if (routes.isEmpty()) return result;

            for (RouteEntity route : routes) {
                List<TripEntity> trips = dbManager.getTripDao()
                    .queryBuilder()
                    .limit((long) MAX_TRIPS) // ← MENO TRIP DA PROCESSARE
                    .where().eq("routeKey", route.getKey())
                    .query();

                for (TripEntity trip : trips) {
                    // CHIAVE: queryForFirst invece di query().get(0)
                    StopTimeEntity nextStop = dbManager.getStopTimeDao()
                        .queryBuilder()
                        .orderBy("arrivalTime", true)
                        .where()
                            .eq("tripId", trip.getTripId())
                            .and().eq("agencyId", trip.getAgencyId())
                            .and().ge("arrivalTime", nowInSeconds)
                            .and().le("arrivalTime", maxTime)
                        .queryForFirst(); // ← MOLTO PIÙ VELOCE!

                    if (nextStop != null) {
                        String stopKey = nextStop.getAgencyId() + ":" + nextStop.getStopId();
                        StopEntity stop = dbManager.getStopDao().queryForId(stopKey);
                        
                        if (stop != null) {
                            String headsign = trip.getTripHeadsign() != null ? 
                                trip.getTripHeadsign() : "Direzione sconosciuta";
                            String readableTime = TimeUtils.getReadableArrivalTime(nextStop.getArrivalTime(), nowInSeconds);
                            
                            result.add(String.format("🚌 #%d → %s\n   %s (%s)", 
                                result.size() + 1, headsign, stop.getStopName(), readableTime));
                            
                            // FERMA PRESTO
                            if (result.size() >= MAX_RESULTS) return result;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        
        return result;
    }


    
    // Classe helper
    private static class TripInfo {
        TripEntity trip;
        StopTimeEntity nextStop;
        StopEntity stopEntity;
        int minutesToArrival;
        
        TripInfo(TripEntity trip, StopTimeEntity nextStop, StopEntity stopEntity, int minutesToArrival) {
            this.trip = trip;
            this.nextStop = nextStop;
            this.stopEntity = stopEntity;
            this.minutesToArrival = minutesToArrival;
        }
    }

    
    // METODO DI TEST - Aggiungi questo per debugging
    public void testLineData(String shortName) {
        try {
            System.out.println("=== TEST LINEA " + shortName + " ===");
            
            List<RouteEntity> routes = dbManager.getRouteDao()
                .queryBuilder()
                .where().eq("shortName", shortName)
                .query();
                
            System.out.println("Route trovate: " + routes.size());
            
            for (RouteEntity route : routes) {
                System.out.println("Route: " + route.getKey() + " - " + route.getLongName());
                
                List<TripEntity> trips = dbManager.getTripDao()
                    .queryBuilder()
                    .limit(5L)
                    .where().eq("routeKey", route.getKey())
                    .query();
                    
                System.out.println("  Trip trovati: " + trips.size());
                
                for (TripEntity trip : trips) {
                    System.out.println("    Trip: " + trip.getKey() + " - " + trip.getTripHeadsign());
                    
                    // Test dei stop times
                    List<StopTimeEntity> stopTimes = dbManager.getStopTimeDao()
                        .queryBuilder()
                        .limit(3L)
                        .where()
                            .eq("tripId", trip.getTripId())
                            .and()
                            .eq("agencyId", trip.getAgencyId())
                        .query();
                        
                    System.out.println("      StopTimes trovati: " + stopTimes.size());
                    for (StopTimeEntity st : stopTimes) {
                        System.out.println("        " + st.getStopId() + " alle " + 
                            TimeUtils.formatSecondsToTime(st.getArrivalTime()));
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}
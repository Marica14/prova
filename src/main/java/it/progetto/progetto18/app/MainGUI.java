package it.progetto.progetto18.app;

import it.progetto.progetto18.core.*;
import it.progetto.progetto18.model.*;
import it.progetto.progetto18.manager.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.StopTime;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private GTFSDataManager manager;
    private JList<String> stopsList;
    private JTextField searchField;
    private JButton searchButton;
    private JButton btnRoutes;
    private JButton btnActiveTrips;
    private JTextArea infoArea;
    private JXMapViewer mapViewer;
    private JProgressBar progressBar;
    private JSplitPane leftSplit;

    private Set<Waypoint> allWaypoints = new HashSet<>();
    private WaypointPainter<Waypoint> waypointPainter;
    private Map<String, StopEntity> stopsMap = new HashMap<>();

    public MainGUI() {
        setTitle("Rome Transit Tracker - Demo");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Inizializza pannelli e mapViewer
        initTopPanel();
        initLeftPanel();
        initMapViewer();
        initSplitPane();
        
        initGtfs();

        // Listener pulsanti
        searchButton.addActionListener(e -> onSearchStop());
        btnRoutes.addActionListener(e -> showAllRoutes());
        btnActiveTrips.addActionListener(e -> onSearchLine());

        // LISTENER CORRETTO - gestisce sia fermate che linee
        stopsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = stopsList.getSelectedValue();
                if (selected != null) {
                    if (selected.contains(" - ")) {
                        // È una fermata (formato: "ID - Nome")
                        showStopDetails(selected);
                    } else if (selected.contains(" : ")) {
                        // È una linea (formato: "ShortName : LongName")
                        showLineDetails(selected);
                    }
                }
            }
        });
    }

    // ... [tutti i metodi esistenti rimangono uguali fino a onSearchLine] ...

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchButton = new JButton("Cerca Fermata");
        btnRoutes = new JButton("Mostra tutte le linee");
        btnActiveTrips = new JButton("Cerca Linea");
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        topPanel.add(btnRoutes, BorderLayout.WEST);
        topPanel.add(btnActiveTrips, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);
    }

    private void initLeftPanel() {
        stopsList = new JList<>();
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(stopsList), new JScrollPane(infoArea));
        leftSplit.setDividerLocation(300);
    }

    private void initMapViewer() {
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(8);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        GeoPosition rome = new GeoPosition(41.9028, 12.4964);
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(rome);

        waypointPainter = new WaypointPainter<>();
        mapViewer.setOverlayPainter(waypointPainter);
    }

    private void initSplitPane() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftSplit, mapViewer);
        mainSplit.setDividerLocation(300);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void initGtfs() {
        infoArea.setText("Caricamento dati GTFS in corso...");
        stopsList.setEnabled(false);
        searchButton.setEnabled(false);
        btnRoutes.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                File dbFile = new File("gtfs.db");
                manager = new GTFSDataManager();
                if (dbFile.exists()) {
                    manager.initDb();
                    List<StopEntity> stopsFromDb = manager.getAllStops();
                    int total = stopsFromDb.size();
                    for (int i = 0; i < total; i++) {
                        stopsMap.put(stopsFromDb.get(i).getStopId(), stopsFromDb.get(i));
                        publish((int)(100.0 * i / total));
                    }
                    publish(100);
                    return null;
                }

                publish(5);
                manager.parseGtfs("src/main/resources/rome_static_gtfs.zip");
                publish(10);

                manager.initDb();

                int batchSize = 100;

                // Inserimento stops
                List<Stop> stops = manager.getAllStopsRaw();
                for (int i = 0; i < stops.size(); i += batchSize) {
                    int end = Math.min(stops.size(), i + batchSize);
                    manager.insertStopsToDbBatch(stops.subList(i, end), manager.getDbManager());
                    publish(10 + 20 * end / stops.size());
                }

                // Inserimento routes
                List<Route> routes = manager.getAllRoutesRaw();
                for (int i = 0; i < routes.size(); i += batchSize) {
                    int end = Math.min(routes.size(), i + batchSize);
                    manager.insertRoutesToDbBatch(routes.subList(i, end), manager.getDbManager());
                    publish(30 + 20 * end / routes.size());
                }

                // Inserimento trips
                List<Trip> trips = manager.getAllTripsRaw();
                for (int i = 0; i < trips.size(); i += batchSize) {
                    int end = Math.min(trips.size(), i + batchSize);
                    manager.insertTripsToDbBatch(trips.subList(i, end), manager.getDbManager());
                    publish(50 + 20 * end / trips.size());
                }

                // Inserimento stop_times
                List<StopTime> stopTimes = manager.getAllStopTimesRaw();
                for (int i = 0; i < stopTimes.size(); i += batchSize) {
                    int end = Math.min(stopTimes.size(), i + batchSize);
                    manager.insertStopTimesToDbBatch(stopTimes.subList(i, end), manager.getDbManager());
                    publish(70 + 30 * end / stopTimes.size());
                }

                List<StopEntity> stopsFromDb = manager.getAllStops();
                for (StopEntity stop : stopsFromDb) {
                    stopsMap.put(stop.getStopId(), stop);
                }
                publish(100);
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int val = chunks.get(chunks.size() - 1);
                progressBar.setValue(val);
            }

            @Override
            protected void done() {
                try {
                    get();
                    infoArea.setText("Dati GTFS caricati correttamente!");
                    stopsList.setEnabled(true);
                    searchButton.setEnabled(true);
                    btnRoutes.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainGUI.this,
                        "Errore nel caricamento GTFS: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
                initWaypoints();
            }
        };

        worker.execute();
    }
        
    private void onSearchStop() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci il nome o il codice della fermata.");
            return;
        }

        String normalized = query.toLowerCase();
        DefaultListModel<String> model = new DefaultListModel<>();
        Set<StopEntity> stopToShow = new LinkedHashSet<>();

        boolean isNumeric = query.matches("\\d+");
        
        for (StopEntity stop : stopsMap.values()) {
            boolean matchesCode = false;
            boolean matchesName = false;
            
            if (isNumeric) {
                String stopId = stop.getStopId();
                matchesCode = stopId != null && stopId.startsWith(query);
            } else {
                String stopName = stop.getStopName();
                matchesName = stopName != null && stopName.toLowerCase().contains(query.toLowerCase());
            }
        
            if (matchesCode || matchesName) {
                model.addElement(stop.getStopId() + " - " + stop.getStopName());
                stopToShow.add(stop);
            }
        }

        if (stopToShow.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna fermata trovata per la query: " + query);
            stopsList.setModel(new DefaultListModel<>());
            allWaypoints.clear();
            waypointPainter.setWaypoints(allWaypoints);
            mapViewer.repaint();
            return;
        }
        
        stopsList.setModel(model);
        displayStopsOnMap(stopToShow);
    }

    private void onSearchLine() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci il nome o numero della linea.");
            return;
        }

        // Disabilita UI durante la ricerca
        searchButton.setEnabled(false);
        btnActiveTrips.setEnabled(false);
        infoArea.setText("Ricerca linee in corso...");
        
        SwingWorker<List<RouteEntity>, Void> worker = new SwingWorker<List<RouteEntity>, Void>() {
            @Override
            protected List<RouteEntity> doInBackground() throws Exception {
                System.out.println("doInBackground: inizio ricerca linea " + query);
                List<RouteEntity> results = new ArrayList<>();
                
                try {
                    List<RouteEntity> allRoutes = manager.getAllRoutes();
                    String normalizedQuery = query.toLowerCase();
                    
                    for (RouteEntity route : allRoutes) {
                        boolean matches = false;
                        
                        // Cerca per nome breve (es. "64", "H")
                        if (route.getShortName() != null && 
                            route.getShortName().toLowerCase().contains(normalizedQuery)) {
                            matches = true;
                        }
                        
                        // Cerca per nome lungo (es. "Termini-Vaticano")
                        if (route.getLongName() != null && 
                            route.getLongName().toLowerCase().contains(normalizedQuery)) {
                            matches = true;
                        }
                        
                        if (matches) {
                            results.add(route);
                        }
                    }
                    
                    System.out.println("doInBackground: trovate " + results.size() + " linee");
                    return results;
                    
                } catch (Exception e) {
                    System.err.println("Errore durante la ricerca: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<RouteEntity> routes = get();
                    
                    if (routes.isEmpty()) {
                        JOptionPane.showMessageDialog(MainGUI.this, 
                            "Nessuna linea trovata per: " + query);
                        stopsList.setModel(new DefaultListModel<>());
                    } else {
                        DefaultListModel<String> model = new DefaultListModel<>();
                        for (RouteEntity route : routes) {
                            String shortName = route.getShortName() != null ? route.getShortName() : "N/A";
                            String longName = route.getLongName() != null ? route.getLongName() : "Senza descrizione";
                            model.addElement(shortName + " : " + longName);
                        }
                        stopsList.setModel(model);
                        infoArea.setText("Trovate " + routes.size() + " linee per: " + query + 
                                       "\n\nClicca su una linea per vedere i mezzi attivi!");
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainGUI.this,
                        "Errore nella ricerca linee: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                    infoArea.setText("Errore nella ricerca linee.");
                } finally {
                    searchButton.setEnabled(true);
                    btnActiveTrips.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }

    // NUOVO METODO - Gestisce la selezione di una linea dalla lista
    private void showLineDetails(String lineText) {
        if (!lineText.contains(" : ")) return;
        
        String shortName = lineText.split(" : ")[0];
        
        // Disabilita UI durante il caricamento
        searchButton.setEnabled(false);
        btnRoutes.setEnabled(false);
        btnActiveTrips.setEnabled(false);
        infoArea.setText("Caricamento informazioni per la linea " + shortName + "...");
        
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                System.out.println("doInBackground: caricamento dettagli linea " + shortName);
                try {
                    List<String> activeTrips = manager.getActiveTripsForLine(shortName);
                    System.out.println("doInBackground: trovati " + activeTrips.size() + " trip attivi per linea " + shortName);
                    return activeTrips;
                } catch (Exception e) {
                    System.err.println("Errore nel caricamento trip attivi: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<String> tripInfo = get();
                    displayLineInfo(shortName, tripInfo);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    infoArea.setText("Errore nel caricamento informazioni per la linea " + shortName + ": " + e.getMessage());
                } finally {
                    searchButton.setEnabled(true);
                    btnRoutes.setEnabled(true);
                    btnActiveTrips.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }

    // NUOVO METODO - Mostra le informazioni della linea
    private void displayLineInfo(String shortName, List<String> tripInfo) {
        StringBuilder info = new StringBuilder();
        info.append("=== LINEA ").append(shortName).append(" ===\n\n");
        
        if (tripInfo.isEmpty()) {
            info.append("Nessun viaggio attivo al momento per questa linea.\n\n");
            info.append("Possibili cause:\n");
            info.append("• La linea non è in servizio in questo orario\n");
            info.append("• Tutti i viaggi sono già terminati\n");
            info.append("• Problemi nei dati GTFS\n\n");
            info.append("Orario attuale: ").append(java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else {
            info.append("Mezzi attivi in questo momento (").append(tripInfo.size()).append("):\n\n");
            
            for (int i = 0; i < tripInfo.size(); i++) {
                info.append("🚌 ").append(tripInfo.get(i)).append("\n\n");
            }
            
            info.append("Ultimo aggiornamento: ").append(java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        
        infoArea.setText(info.toString());
        infoArea.setCaretPosition(0); // Scroll to top
    }

    private void displayStopsOnMap(Set<StopEntity> stops) {
        if (stops.isEmpty()) return;

        GeoPosition pos = new GeoPosition(stops.iterator().next().getStopLat(),
                                          stops.iterator().next().getStopLon());
        mapViewer.setAddressLocation(pos);
        mapViewer.setZoom(4);

        allWaypoints.clear();
        for (StopEntity stop : stops) {
            Waypoint wp = new DefaultWaypoint(new GeoPosition(stop.getStopLat(), stop.getStopLon()));
            allWaypoints.add(wp);
        }

        waypointPainter.setWaypoints(allWaypoints);
        mapViewer.repaint();
    }

    private void showAllRoutes() {
        try {
            List<RouteEntity> routes = manager.getAllRoutes();
            routes.sort(Comparator.comparing(r -> r.getShortName() != null ? r.getShortName() : ""));
            DefaultListModel<String> model = new DefaultListModel<>();
            for (RouteEntity r : routes) {
                String shortName = r.getShortName() != null ? r.getShortName() : "N/A";
                String longName = r.getLongName() != null ? r.getLongName() : "Senza descrizione";
                model.addElement(shortName + " : " + longName);
            }
            stopsList.setModel(model);
            infoArea.setText("Elenco completo delle linee disponibili.\n\nClicca su una linea per vedere i mezzi attivi!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWaypoints() {
        allWaypoints.clear();
        for (StopEntity stop : stopsMap.values()) {
            Waypoint wp = new DefaultWaypoint(new GeoPosition(stop.getStopLat(), stop.getStopLon()));
            allWaypoints.add(wp);
        }

        waypointPainter.setWaypoints(allWaypoints);
        mapViewer.setOverlayPainter(waypointPainter);
    }

    private void showStopDetails(String stopText) {
        String stopId = stopText.split(" - ")[0];
        StopEntity stop = stopsMap.get(stopId);
        if (stop == null) return;

        GeoPosition pos = new GeoPosition(stop.getStopLat(), stop.getStopLon());
        mapViewer.setAddressLocation(pos);
        mapViewer.setZoom(7);
        
        allWaypoints.clear();
        Waypoint selectedWaypoint = new DefaultWaypoint(pos);
        allWaypoints.add(selectedWaypoint);
        waypointPainter.setWaypoints(allWaypoints);

        mapViewer.repaint();
        System.out.println("Carico linee per fermata: " + stop.getStopId());

        try {
            List<StopArrival> arrivals = manager.getNextArrivalsForStop(stop.getStopId());

            StringBuilder info = new StringBuilder();
            info.append("Fermata: ").append(stop.getStopName())
                .append("\nID fermata: ").append(stop.getStopId())
                .append("\nCoordinate: ").append(stop.getStopLat()).append(", ").append(stop.getStopLon())
                .append("\n\nProssime linee in arrivo:\n");

            int N = Math.min(arrivals.size(), 5);
            for (int i = 0; i < N; i++) {
                StopArrival arrival = arrivals.get(i);
                String tripId = arrival.getTripId();
                String headsign = arrival.getTripHeadsign() != null ? arrival.getTripHeadsign() : "Sconosciuta";

                try {
                    if (tripId != null) {
                        TripEntity tripEntity = manager.getDbManager().getTripDao().queryForId(tripId);
                        if (tripEntity != null && tripEntity.getTripHeadsign() != null) {
                            headsign = tripEntity.getTripHeadsign();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                info.append("Linea ").append(arrival.getRoute().getShortName())
                    .append(" verso ").append(headsign)
                    .append(" → Arrivo: ").append(arrival.getArrivalTimeReadable())
                    .append("\n");
            }

            infoArea.setText(info.toString());
        } catch (Exception e) {
            e.printStackTrace();
            infoArea.setText("Errore nel caricamento delle linee per questa fermata.");
        }
    }

    private void logDatabaseStats() {
        try {
            long stopCount = manager.getDbManager().getStopDao().countOf();
            long routeCount = manager.getDbManager().getRouteDao().countOf();
            long tripCount = manager.getDbManager().getTripDao().countOf();
            long stopTimeCount = manager.getDbManager().getStopTimeDao().countOf();
            
            System.out.println("=== DATABASE STATS ===");
            System.out.println("Fermate: " + stopCount);
            System.out.println("Linee: " + routeCount);
            System.out.println("Viaggi: " + tripCount);
            System.out.println("Orari: " + stopTimeCount);
            System.out.println("====================");
            
        } catch (Exception e) {
            System.err.println("Errore nel recuperare statistiche DB: " + e.getMessage());
        }
    }
}
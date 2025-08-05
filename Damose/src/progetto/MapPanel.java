package progetto;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class MapPanel {
	
	public JPanel panel;
	private Point lastPoint;
	
	
	// Metodo per creare il pannello della mappa
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Creazione della mappa OpenStreetMap
        JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Imposta la posizione iniziale su Roma
        mapViewer.setAddressLocation(new GeoPosition(41.9028, 12.4964)); 
        mapViewer.setZoom(7); // Zoom iniziale
        
        mapViewer.addMouseWheelListener(e -> {
            int zoom = mapViewer.getZoom();
            if (e.getWheelRotation() < 0) {
                mapViewer.setZoom(Math.max(zoom - 1, 0)); // Zoom in
            } else {
                mapViewer.setZoom(Math.min(zoom + 1, 10)); // Zoom out
            }
        });
        
        mapViewer.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        });

        mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Point newPoint = e.getPoint();
                    GeoPosition lastGeo = mapViewer.convertPointToGeoPosition(lastPoint);
                    GeoPosition newGeo = mapViewer.convertPointToGeoPosition(newPoint);

                    double latDiff = newGeo.getLatitude() - lastGeo.getLatitude();
                    double lonDiff = newGeo.getLongitude() - lastGeo.getLongitude();

                    GeoPosition currentPos = mapViewer.getCenterPosition();
                    mapViewer.setCenterPosition(new GeoPosition(
                        currentPos.getLatitude() - latDiff,
                        currentPos.getLongitude() - lonDiff
                    ));

                    lastPoint = newPoint;
                }
            }
        });
        
     // Pannello per i bottoni di zoom
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        zoomPanel.setOpaque(false); // Trasparente se necessario

        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        zoomInButton.setFocusPainted(false);
        zoomOutButton.setFocusPainted(false);
        

        // Imposta dimensioni quadrate ai bottoni
        Dimension buttonSize = new Dimension(41, 30);
        zoomInButton.setPreferredSize(buttonSize);
        zoomOutButton.setPreferredSize(buttonSize);

        // Aggiunta delle azioni per zoom in e out
        zoomInButton.addActionListener(e -> mapViewer.setZoom(Math.max(mapViewer.getZoom() - 1, 0)));
        zoomOutButton.addActionListener(e -> mapViewer.setZoom(Math.min(mapViewer.getZoom() + 1, 10)));

        // Aggiunta bottoni al pannello
        zoomPanel.add(zoomInButton);
        zoomPanel.add(zoomOutButton);

        // Aggiunta della mappa e dei controlli al pannello
        panel.add(mapViewer, BorderLayout.CENTER);
        panel.add(zoomPanel, BorderLayout.SOUTH); // Ora i bottoni sono in basso a sinistra

        return panel;
    }
    
    public MapPanel() {
		this.panel = createMapPanel();
	}
	
}

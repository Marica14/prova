package progetto;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import javax.swing.*;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.GeoPosition;

public class GUI {
	
	
	
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private HashMap<String, String> info;
    private JMenuBar menubar;
    private JMenu AreaP;
    private JXMapViewer mapViewer;
    
    
    
    
    public GUI(HashMap<String, String> infoOg) {
        this.info = infoOg;

        frame = new JFrame("Damose - Rome Transit Tracker");
        AreaP = new JMenu("Area Personale");
        menubar = new JMenuBar();
        menubar.add(Box.createHorizontalGlue());
        
        JMenuItem accedi = new JMenuItem("Accedi");
        JMenuItem registrati = new JMenuItem("Registrati");
        JMenuItem home = new JMenuItem("Home");
        
        
        //aggiunta dei bottoni e della barra
        AreaP.add(accedi);
        AreaP.add(registrati);
        AreaP.add(home);
        menubar.add(AreaP);
        frame.setJMenuBar(menubar);
        
        
        //aggiunta azioni quando premi i bottoni
        accedi.addActionListener(e -> showScreen("Login"));
        registrati.addActionListener(e -> showScreen("Register"));
        home.addActionListener(e -> showScreen("Home"));

        
        // Creazione del CardLayout
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        
        
        // Creazione delle schermate di login e registrazione e home
        MapPanel hPan = new MapPanel();//aggiunta la mappa alla home
        JPanel homePanel = hPan.panel;
		LoginSystem loginPanel = new LoginSystem(this, info);
		RegisterSystem registerPanel = new RegisterSystem(this, info);
        
        //Aggiunta al panel
        cardPanel.add(homePanel, "Home");
        cardPanel.add(loginPanel, "Login");
        cardPanel.add(registerPanel, "Register");

        
        ImageIcon icon = new ImageIcon("C:/Users/maric/Desktop/Javaa/Damose/icona.png");
        frame.setIconImage(icon.getImage());
        frame.add(cardPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    

    public void showScreen(String name) {
        cardLayout.show(cardPanel, name);
    }
}

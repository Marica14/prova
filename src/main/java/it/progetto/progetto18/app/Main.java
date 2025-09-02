package it.progetto.progetto18.app;

import it.progetto.progetto18.app.*;
import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                MainGUI gui = new MainGUI();
                gui.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Errore durante l'avvio della GUI: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

package progetto;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.*;

public class RegisterSystem extends JPanel implements ActionListener {
    
    private GUI gui;  
    private HashMap<String, String> info;

    JButton loginButton = new JButton("Accesso");
    JButton resetButton = new JButton("Reset");
    JButton registerButton = new JButton("Registrati");
    JButton backButton = new JButton("Indietro");

    JLabel userLabel = new JLabel("Nome utente:");
    JLabel passwordLabel = new JLabel("Password:");
    JTextField userTextField = new JTextField(20);
    JPasswordField passwordField = new JPasswordField(20);
    JLabel message = new JLabel();

    public RegisterSystem(GUI gui, HashMap<String, String> infoOg) {
        this.gui = gui;
        this.info = infoOg;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);
        gbc.gridx = 1;
        add(userTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(registerButton, gbc);

        gbc.gridx = 0;
        add(resetButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(loginButton, gbc);

        gbc.gridx = 0;
        add(backButton, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(message, gbc);

        registerButton.addActionListener(this);
        resetButton.addActionListener(this);
        loginButton.addActionListener(e -> gui.showScreen("Accedi"));
        backButton.addActionListener(e -> gui.showScreen("Home"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String userID = userTextField.getText();
        String password = String.valueOf(passwordField.getPassword());

        if (e.getSource() == resetButton) {
            userTextField.setText("");
            passwordField.setText("");
            message.setText("");
        } else if (e.getSource() == registerButton) {
            if (!userID.isEmpty() && !password.isEmpty()) {
                if (!info.containsKey(userID)) {
                    info.put(userID, password);
                    message.setForeground(Color.GREEN);
                    message.setText("Utente registrato con successo!");
                } else {
                    message.setForeground(Color.RED);
                    message.setText("Username già esistente!");
                }
            } else {
                message.setForeground(Color.RED);
                message.setText("I campi non possono essere vuoti!");
            }
        }
    }
}

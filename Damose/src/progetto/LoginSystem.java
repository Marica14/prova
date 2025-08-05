package progetto;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginSystem extends JPanel implements ActionListener{
	
	private GUI gui;
	private HashMap<String,String> info = new HashMap<String,String>();
	
	JButton loginButton = new JButton("Accedi");
	JButton resetbutton = new JButton("Reset");
	JButton backButton = new JButton("Indietro");
	JLabel userLabel = new JLabel("Nome utente: ");
	JLabel passwordLabel = new JLabel("Password: ");
	JTextField userTextField = new JTextField(20);
	JPasswordField passwordField = new JPasswordField(20);
	JLabel message = new JLabel();
	JButton registerbutton = new JButton("Registrati");
	
	
	public LoginSystem(GUI gui,HashMap<String,String> infoOg){
		this.info = infoOg;
		this.gui = gui;
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		
		loginButton.addActionListener(this);
		resetbutton.addActionListener(this);
        backButton.addActionListener(e -> gui.showScreen("Home"));

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
        add(loginButton, gbc);

        gbc.gridx = 0;
        add(resetbutton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(registerbutton, gbc);

        gbc.gridx = 0;
        add(backButton, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(message,gbc);
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String userID = userTextField.getText();
		String password = String.valueOf(passwordField.getPassword());
		
		if (e.getSource() == resetbutton) {
            // Reset dei campi
            userTextField.setText("");
            passwordField.setText("");
            message.setText("");  // Cancella anche il messaggio di errore
        } else if (e.getSource() == loginButton) {
            userID = userTextField.getText();
            password = new String(passwordField.getPassword());
		
		if (info.containsKey(userID) && info.get(userID).equals(password)) {
            message.setText("Login successful");
            gui.showScreen("Home");
        } else {
            message.setText("Errore login");
        }
	}
	}
}

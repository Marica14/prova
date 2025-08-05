package progetto;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class WelcomePage {
	JFrame frame = new JFrame();
	JLabel welcomeLabel = new JLabel("Hello"); 
	

	public WelcomePage(String userID) {
		
		welcomeLabel.setFont(new Font(null, Font.PLAIN,25));
		welcomeLabel.setText("Welcome "+userID);
		frame.add(welcomeLabel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,600);
		frame.setLocationRelativeTo(null);//va dopo setSize sennò non va
		frame.setVisible(true);
	}

}

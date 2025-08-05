package progetto;
import java.io.*;
import java.util.HashMap;

public class IDandPasswords {
    private HashMap<String, String> info = new HashMap<>();
    private final String FILE_NAME = "users.txt"; // Nome del file
    //private final String FILE_NAME = "C:/Users/maric/Desktop/Javaa/Damose/Utenti.txt"; // Nome del file

    public IDandPasswords() {
        loadUsersFromFile(); // Carica gli utenti salvati nel file
    }

    private void loadUsersFromFile() {
    	/*File file = new File(FILE_NAME);
        System.out.println("Percorso del file: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("Il file non esiste!");
            return;
        }*/
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":"); // Formato: username:password
                if (parts.length == 2) {
                    info.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Nessun file trovato, sarà creato uno nuovo.");
        }
    }

    public void saveUsersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (var entry : info.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio degli utenti.");
        }
    }

    protected HashMap<String, String> getInfo() {
        return info;
    }

    protected void addUser(String username, String password) {
        info.put(username, password);
        saveUsersToFile(); // Salva il nuovo utente nel file
    }
}

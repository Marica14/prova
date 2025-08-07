# Prova
c'ho messo quello che ho fatto su eclipse seguendo il tutorial https://github.com/maxkratz/How-to-Eclipse-with-Github/blob/master/README.md

Installazione EGit:
1- Su Eclipse, vai su: Help/Install new Software
2- In Work with, copiare e incollare questo: http://download.eclipse.org/egit/updates
3- Clicca su Add, seleziona tutte le opzioni e clicca Next
4- Accetta i termini e termina l'installazione

Genera una chiave RSA su Eclipse:
1- Vai su Window --> Preferences/General/Network Connections/SSH2
2- Seleziona Key Management
3- Clicca su Generate RSA Key
4- In fondo, digita una password segreta (salvala da qualche parte) e confermala
5- Clicca su Save Private Key (il nome della chiave è id_rsa)

Aggiungere chiave RSA su GitHub:
1- Apri il file pub id_rsa nel blocco note
2- Copia il contenuto
3- Vai sulle impostazioni del tuo profilo GitHub
4- Vai su Settings/SSH and GPG keys
5- Clicca su New SSH key
6- Dai un titolo a tua scelta e incolla il testo copiato in precedenza
7- Clicca su Add SSH key

Importare la repository su Eclipse:
1- Vai su Window/Show View/Other
2- Seleziona Git/Git Repositories
3- Nella pagina aperta, vai su Clone a Git repository
4- Vai nella repository GitHub (https://github.com/Marica14/prova) vai su Code --> ssh e copia ciò che c'è
5- Dove va inserito l'url, incolla quello copiato prima
6- Se il protocollo non è già impostato su ssh, metterlo su questo (sennò proseguire direttamente)
7- Proseguire su Next due volte e poi su Finish (al termine, su Git Repositories in fondo, questa dovrebbe essere presente)

Come mettere la repository nel Package Explorer:
1- Fare tasto destro su Package Explorer
2- Cliccare su Import --> Git --> Projects from Git --> Existing Local Repository e selezionare quella richiesta

Come aggiornare la repository:
1- Nella scheda in fondo Git Staging (si può aprire su Show View in Window) si possono vedere tutti i cambiamenti eseguiti
2- Quelli che si vogliono confermare vanno spostati da Unstaged Changes(n) a Staged Changes(m)
3- Scrivere un messaggio e cliccare su Commit and Push, clicca su Next e su Finish
4- Al messaggio che appare, cliccare semplicemente Close

Come inserire le librerie:
1- Tasto destro su Damose --> Build Path --> Add External Archives
2- Aggiungi le due librerie scaricate prima

Come rimuovere librerie non necessarie:
1- Tasto destro su Damose --> Build Path --> Configure Build Path
2- Su Libraries, rimuovere le librerie segnate come "missing"
3- Cliccare Apply and Close
4- IMPORTANTE!! --> verrà applicato un cambiamento che sarà segnato negli unstaged changes, QUESTO VA IGNORATO (come indicato prima o andrà ad interferire con le librerie altrui!!!!)

Come ignorare cambiamenti (da applicare alle librerie personali):
1- Nella scheda Unstaged Changes fare tasto destro su quello che si vuole ignorare e premere su Ignore

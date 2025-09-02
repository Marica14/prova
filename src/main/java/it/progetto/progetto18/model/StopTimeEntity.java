package it.progetto.progetto18.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import it.progetto.progetto18.core.*;

@DatabaseTable(tableName = "stop_times")
public class StopTimeEntity {
	@DatabaseField(index = true)
    private String agencyId;
	
    @DatabaseField(id = true)
    private String key;

    @DatabaseField(index = true)
    private String tripId;

    @DatabaseField(index = true)
    private String stopId;

    @DatabaseField
    private int arrivalTime; // in secondi dalla mezzanotte
    
    @DatabaseField
    private int departureTime; // secondi dalla mezzanotte

    @DatabaseField
    private int stopSequence; // ordine della fermata

    @DatabaseField
    private String stopHeadsign; // opzionale, override headsign del trip

    @DatabaseField
    private Integer pickupType; // 0=normale, 1=non disponibile, 2=su richiesta

    @DatabaseField
    private Integer dropOffType; // 0=normale, 1=non disponibile, 2=su richiesta

    @DatabaseField
    private Double shapeDistTraveled; // distanza cumulativa sullo shape (metri)

    @DatabaseField
    private Integer timepoint; // 0=stimato, 1=reale
    /*
    @DatabaseField(index = true)
    private String tripKey; // agencyId:tripId*/

    
    public StopTimeEntity() { }

    

    // Costruttore con arrivo già in secondi
    public StopTimeEntity(String agencyId, String tripId, String stopId, int arrivalTime, int departureTimeStr,
            int stopSequence ) {
    	this.agencyId = agencyId;
        this.tripId = tripId;
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTimeStr;
        this.stopSequence = stopSequence;
        this.key = generateKey();
        //this.tripKey = tripKey; String tripKey
    }

    public String getKey() { return key; }
    public String getTripId() { return tripId; }
    public String getStopId() { return stopId; }
    public int getArrivalTime() { return arrivalTime; }
    public int getDepartureTime() { return departureTime; }
    public int getStopSequence() { return stopSequence; }
    public String getStopHeadsign() { return stopHeadsign; }
    public Integer getPickupType() { return pickupType; }
    public Integer getDropOffType() { return dropOffType; }
    public Double getShapeDistTraveled() { return shapeDistTraveled; }
    public Integer getTimepoint() { return timepoint; }
    //public String getTripKey() { return tripKey; }
    public String getAgencyId() { return agencyId ; }
    
    
    // Restituisce l'orario in formato HH:mm:ss leggibile
    public String getArrivalTimeString() {
        return TimeUtils.formatSecondsToTime(arrivalTime);
    }

    public String getDepartureTimeString() {
        return TimeUtils.formatSecondsToTime(departureTime);
    }
    
    private String generateKey() {
        return tripId + ":" + stopId + ":" + arrivalTime;
    }

    // Converte "HH:mm:ss" in secondi dalla mezzanotte
    public static int parseTimeToSeconds(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return hours * 3600 + minutes * 60 + seconds;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato orario non valido: " + timeStr);
        }
    }

 // Converte secondi dalla mezzanotte in "HH:mm:ss" con gestione >24h
    public static String formatSecondsToTime(int totalSeconds) {
        int days = totalSeconds / 86400; // quanti giorni oltre la mezzanotte
        int remainder = totalSeconds % 86400;

        int hours = remainder / 3600;
        int minutes = (remainder % 3600) / 60;
        int seconds = remainder % 60;

        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        if (days > 0) {
            return String.format("+%dg %s", days, time);
        } else {
            return time;
        }
    }



	

}
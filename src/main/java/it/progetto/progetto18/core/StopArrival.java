package it.progetto.progetto18.core;

import it.progetto.progetto18.model.*;

/**
 * Classe helper per rappresentare un arrivo di un bus in una fermata.
 */
public class StopArrival {
    private final RouteEntity route;
    private final int arrivalTime; // secondi dalla mezzanotte
    private final String tripId;
    private final String tripHeadsign;
    
    public StopArrival(RouteEntity route, int arrivalTime, String tripId, String tripHeadsign) {
        this.route = route;
        this.arrivalTime = arrivalTime;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
    }

    public RouteEntity getRoute() { return route; }
    public int getArrivalTime() { return arrivalTime; }
    public String getTripId() { return tripId; }
    public String getTripHeadsign() { return tripHeadsign; }

    // nuovo metodo
    public String getArrivalTimeReadable() {
        int now = java.time.LocalTime.now().toSecondOfDay();
        return TimeUtils.getReadableArrivalTime(arrivalTime, now);
    }
}

package it.progetto.progetto18.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stops")
public class StopEntity {

    @DatabaseField(id = true)
    private String key;  // agencyId:stopId

    @DatabaseField
    private String agencyId;

    @DatabaseField
    private String stopId;

    @DatabaseField
    private String stopCode;

    @DatabaseField
    private String stopName;

    @DatabaseField
    private String stopDesc;

    @DatabaseField
    private double stopLat;

    @DatabaseField
    private double stopLon;

    @DatabaseField
    private String stopUrl;

    @DatabaseField
    private int wheelchairBoarding; // 0=unknown, 1=accessible, 2=not accessible

    @DatabaseField
    private String stopTimezone;

    @DatabaseField
    private int locationType; // 0=Stop, 1=Station, ecc.

    @DatabaseField
    private String parentStation; // riferimento ad altro stop

    // Costruttore vuoto richiesto da ORMLite
    public StopEntity() {}

    public StopEntity(String agencyId, String stopId, String stopCode, String stopName, String stopDesc,
                      double stopLat, double stopLon, String stopUrl, int wheelchairBoarding,
                      String stopTimezone, int locationType, String parentStation) {
        this.agencyId = agencyId;
        this.stopId = stopId;
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.stopDesc = stopDesc;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopUrl = stopUrl;
        this.wheelchairBoarding = wheelchairBoarding;
        this.stopTimezone = stopTimezone;
        this.locationType = locationType;
        this.parentStation = parentStation;
        this.key = agencyId + ":" + stopId;
    }

    // Getter
    public String getKey() { return key; }
    public String getAgencyId() { return agencyId; }
    public String getStopId() { return stopId; }
    public String getStopCode() { return stopCode; }
    public String getStopName() { return stopName; }
    public String getStopDesc() { return stopDesc; }
    public double getStopLat() { return stopLat; }
    public double getStopLon() { return stopLon; }
    public String getStopUrl() { return stopUrl; }
    public int getWheelchairBoarding() { return wheelchairBoarding; }
    public String getStopTimezone() { return stopTimezone; }
    public int getLocationType() { return locationType; }
    public String getParentStation() { return parentStation; }
}

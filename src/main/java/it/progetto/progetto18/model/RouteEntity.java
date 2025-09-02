package it.progetto.progetto18.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "routes")
public class RouteEntity {

    @DatabaseField(id = true)
    private String key;  // agencyId:routeId

    @DatabaseField
    private String agencyId;

    @DatabaseField
    private String routeId;

    @DatabaseField
    private String shortName;

    @DatabaseField
    private String longName;

    @DatabaseField
    private String type;

    @DatabaseField
    private String url;

    @DatabaseField
    private String color;

    @DatabaseField
    private String textColor;

    // Costruttore vuoto richiesto da ORMLite
    public RouteEntity() {}

    public RouteEntity(String agencyId, String routeId, String shortName, String longName,
                       String type, String url, String color, String textColor) {
        this.agencyId = agencyId;
        this.routeId = routeId;
        this.shortName = shortName;
        this.longName = longName;
        this.type = type;
        this.url = url;
        this.color = color;
        this.textColor = textColor;

        this.key = agencyId + ":" + routeId; // chiave primaria
    }

    // Getter
    public String getKey() { return key; }
    public String getAgencyId() { return agencyId; }
    public String getRouteId() { return routeId; }
    public String getShortName() { return shortName; }
    public String getLongName() { return longName; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public String getColor() { return color; }
    public String getTextColor() { return textColor; }
}

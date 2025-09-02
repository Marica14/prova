package it.progetto.progetto18.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "trips")
public class TripEntity {
    @DatabaseField(id = true)
    private String key; // agencyId:tripId

    @DatabaseField(index = true)
    private String agencyId;

    @DatabaseField(index = true)
    private String tripId;

    @DatabaseField(index = true)
    private String routeId;

    @DatabaseField(index = true)
    private String serviceId;

    @DatabaseField
    private String tripHeadsign;

    @DatabaseField
    private String tripShortName;

    @DatabaseField
    private Integer directionId; // 0 o 1

    @DatabaseField
    private String blockId;

    @DatabaseField
    private String shapeId;

    @DatabaseField
    private Integer wheelchairAccessible; // 0 = no info, 1 = accessible, 2 = not accessible

    @DatabaseField
    private Integer exceptional; // non standard, se presente nei tuoi dati

    
    // riferimento alla route
    @DatabaseField(index = true)
    private String routeKey; // routes.key

    public TripEntity() {}

    public TripEntity(String agencyId, String tripId, String routeId, String serviceId, String routeKey) {
        this.key = agencyId + ":" + tripId;
        this.agencyId = agencyId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.routeKey = routeKey;
    }


    public String getKey() { return key; }
    public String getAgencyId() { return agencyId; }
    public String getTripId() { return tripId; }
    public String getRouteKey() { return routeKey; }
    public String getRouteId() { return routeId; }
    public String getServiceId() { return serviceId; }
    public String getTripHeadsign() { return tripHeadsign; }
    public String getTripShortName() { return tripShortName; }
    public Integer getDirectionId() { return directionId; }
    public String getBlockId() { return blockId; }
    public String getShapeId() { return shapeId; }
    public Integer getWheelchairAccessible() { return wheelchairAccessible; }
    public Integer getExceptional() { return exceptional; }

    public void setTripHeadsign(String tripHeadsign) { this.tripHeadsign = tripHeadsign; }
    public void setTripShortName(String tripShortName) { this.tripShortName = tripShortName; }
    public void setDirectionId(Integer directionId) { this.directionId = directionId; }
    public void setBlockId(String blockId) { this.blockId = blockId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }
    public void setWheelchairAccessible(Integer wheelchairAccessible) { this.wheelchairAccessible = wheelchairAccessible; }
    public void setExceptional(Integer exceptional) { this.exceptional = exceptional; }
}
package edu.nyp.gooddealsnearby;

import java.io.Serializable;
import java.sql.Date;
import com.google.android.gms.maps.model.LatLng;

class LocationObj implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id;
    public String name;
    public String promotion_title;
    public String price;
    public String image;
    public transient LatLng latlon;
    double lat;
    double lon;
    public Date start_date;
    public Date end_date;
    public String location;
    public double distance;
    public String unit;
    public double absdistance;
    public String placeid;
    public String desc;

    public LocationObj(String id, String name, String promotion_title, String price, String image, LatLng latlon, Date start_date, Date end_date, String location,double distance,String unit,double absdistance,String placeid,String desc) {
        this.id = id;
        this.name = name;
        this.promotion_title = promotion_title;
        this.price = price;
        this.image = image;
        this.latlon = latlon;
        this.start_date = start_date;
        this.end_date = end_date;
        this.location = location;
        this.distance = distance;
        this.unit = unit;
        this.absdistance = absdistance;
        this.placeid = placeid;
        this.desc = desc;
        this.lat = latlon.latitude;
        this.lon = latlon.longitude;
    }
}

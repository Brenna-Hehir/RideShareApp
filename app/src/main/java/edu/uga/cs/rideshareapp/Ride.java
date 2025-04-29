package edu.uga.cs.rideshareapp;

public class Ride {

    private String key;
    public String rideType;  // Offer or Request
    public String driverId;  // Initially null for Requests
    public String riderId;   // Initially null for Offers
    public String from;
    public String to;
    public String dateTime;  // "MM-dd-yyyy hh:mm a"
    public boolean accepted; // Initially false
    public boolean driverConfirmed;  // Initially false
    public boolean riderConfirmed;   // Initially false
    public String driverEmail;
    public String riderEmail;

    public Ride() {}

    public Ride(String rideType, String driverId, String riderId, String from, String to, String dateTime,
                 boolean accepted, boolean driverConfirmed, boolean riderConfirmed,
                 String driverEmail, String riderEmail) {
        this.rideType = rideType;
        this.driverId = driverId;
        this.riderId = riderId;
        this.from = from;
        this.to = to;
        this.dateTime = dateTime;
        this.accepted = accepted;
        this.driverConfirmed = driverConfirmed;
        this.riderConfirmed = riderConfirmed;
        this.driverEmail = driverEmail;
        this.riderEmail = riderEmail;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}

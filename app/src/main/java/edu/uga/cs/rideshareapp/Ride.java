package edu.uga.cs.rideshareapp;

/**
 * Represents a ride entry in the RideShare app, which can be either an offer or a request.
 * Contains details about the users involved, ride metadata, confirmation states, and Firebase key.
 */
public class Ride {

    // Firebase key associated with this ride (set manually after retrieval)
    private String key;

    /** Type of ride: either "offer" or "request" */
    public String rideType;  // Offer or Request

    /** UID of the driver (null if ride is a request) */
    public String driverId;  // Initially null for Requests

    /** UID of the rider (null if ride is an offer) */
    public String riderId;   // Initially null for Offers

    /** Starting location of the ride */
    public String from;

    /** Destination location of the ride */
    public String to;

    /** Scheduled date and time of the ride (format: "MM-dd-yyyy hh:mm a") */
    public String dateTime;  // "MM-dd-yyyy hh:mm a"

    /** True if both parties have agreed to the ride */
    public boolean accepted; // Initially false

    /** True if the driver has confirmed ride completion */
    public boolean driverConfirmed;  // Initially

    /** True if the rider has confirmed ride completion */
    public boolean riderConfirmed;   // Initially false

    /** Email of the driver */
    public String driverEmail;

    /** Email of the rider */
    public String riderEmail;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Ride() {}

    /**
     * Constructs a Ride object with full initialization.
     *
     * @param rideType         The type of ride ("offer" or "request")
     * @param driverId         Firebase UID of the driver
     * @param riderId          Firebase UID of the rider
     * @param from             Start location
     * @param to               Destination
     * @param dateTime         Scheduled date/time string
     * @param accepted         Whether the ride has been accepted
     * @param driverConfirmed  Whether the driver confirmed completion
     * @param riderConfirmed   Whether the rider confirmed completion
     * @param driverEmail      Driver's email address
     * @param riderEmail       Rider's email address
     */
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

    /**
     * Returns the Firebase key associated with this ride.
     *
     * @return The Firebase database key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the Firebase key associated with this ride.
     *
     * @param key The Firebase database key
     */
    public void setKey(String key) {
        this.key = key;
    }

}

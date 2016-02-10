package de.flowment.wanderlust;

/**
 * Created by Khaled Reguieg (s57532) <a href="mailto:Khaled.Reguieg@gmail.com">Khaled Reguieg, Khaled.Reguieg@gmail.com</a> on 07.01.2016.
 * <br><br>
 * This data holding class represents a walking trip.
 * It holds all the valuable data as variables and is able to set and get them.
 */
public class Tour {

    /**
     * The unique tripID to identify the trip object.
     */
    private int tripID;

    /**
     * The title of the trip.
     */
    private String title;

    /**
     * The time in seconds the trip took.
     */
    private int timeInSeconds;

    /**
     * The kilometers walked in the trip.
     */
    private double kiloMetersWalked;

    /**
     * Standard Constructor.
     *
     * @param tripID           The id of a trip.
     * @param title            The title of a trip.
     * @param timeInSeconds    The time in seconds a trip took.
     * @param kiloMetersWalked The distance in kilometers walked on a trip.
     */
    public Tour(int tripID, String title, int timeInSeconds, double kiloMetersWalked) {
        this.tripID = tripID;
        this.title = title;
        this.timeInSeconds = timeInSeconds;
        this.kiloMetersWalked = kiloMetersWalked;
    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

    public double getKiloMetersWalked() {
        return kiloMetersWalked;
    }

    public void setKiloMetersWalked(double kiloMetersWalked) {
        this.kiloMetersWalked = kiloMetersWalked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(int timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }
}

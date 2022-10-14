package tourGuide.model;

import gpsUtil.location.Location;

/**
 * @author SimonC.
 */
public class NearByAttraction {

    private String attractionName;
    private Location attractionLocation;
    private double distance;
    private int rewardPoints;

    public NearByAttraction() {
    }

    public NearByAttraction(String attractionName, Location attractionLocation, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(double latitude, double longitude) {
        this.attractionLocation = new Location(latitude, longitude);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    @Override
    public String toString() {
        return "NearByAttraction{" +
                "attractionName='" + attractionName + '\'' +
                '}';
    }
}
